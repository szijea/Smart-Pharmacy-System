package com.pharmacy.controller;

import com.pharmacy.entity.StockIn;
import com.pharmacy.entity.StockInItem;
import com.pharmacy.repository.InventoryRepository;
import com.pharmacy.repository.MedicineRepository;
import com.pharmacy.repository.StockInItemRepository;
import com.pharmacy.repository.StockInRepository;
import com.pharmacy.repository.SupplierRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stock-ins")
public class StockInController {

    @Autowired
    private StockInRepository stockInRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private StockInItemRepository stockInItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<Page<StockIn>> getStockIns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findAll(pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockIn> getStockInById(@PathVariable Long id) {
        Optional<StockIn> stockIn = stockInRepository.findById(id);
        return stockIn.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createStockIn(@RequestBody StockIn stockIn) {
        try {
            System.out.println("[StockInController] 接收到入库单数据: stockInNo=" + stockIn.getStockInNo() + ", items=" + (stockIn.getItems()==null?0:stockIn.getItems().size()));
            if (stockIn.getItems()==null || stockIn.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("入库单必须包含至少一个药品");
            }
            // 供应商处理
            if (stockIn.getSupplier() != null && stockIn.getSupplier().getSupplierId() != null) {
                if (!supplierRepository.existsById(stockIn.getSupplier().getSupplierId())) {
                    System.out.println("供应商不存在, 改用默认供应商 ID=1");
                    Optional<com.pharmacy.entity.Supplier> defaultSupplier = supplierRepository.findById(1);
                    if (defaultSupplier.isPresent()) {
                        stockIn.setSupplier(defaultSupplier.get());
                    } else {
                        return ResponseEntity.badRequest().body("没有可用的供应商, 请先创建供应商");
                    }
                }
            } else {
                Optional<com.pharmacy.entity.Supplier> defaultSupplier = supplierRepository.findById(1);
                if (defaultSupplier.isPresent()) {
                    stockIn.setSupplier(defaultSupplier.get());
                } else {
                    return ResponseEntity.badRequest().body("请先创建供应商");
                }
            }
            // Items 校验与默认值
            for (StockInItem item : stockIn.getItems()) {
                // 支持两种前端传参：1) 嵌套 medicine 对象；2) 直接传 medicineId 字符串到 stockInItem.medicineId
                String medId = null;
                if (item.getMedicine() != null && item.getMedicine().getMedicineId() != null) {
                    medId = item.getMedicine().getMedicineId();
                } else if (item.getMedicineId() != null) {
                    medId = item.getMedicineId();
                }
                if (medId == null || medId.isBlank()) {
                    return ResponseEntity.badRequest().body("药品信息不完整");
                }
                var medOpt = medicineRepository.findById(medId);
                if (medOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("药品不存在: " + medId);
                }
                var med = medOpt.get();
                // set medicine reference and ensure medicineId column is populated
                item.setMedicine(med);
                item.setMedicineId(med.getMedicineId());

                if (item.getBatchNumber()==null || item.getBatchNumber().isBlank()) {
                    item.setBatchNumber("DEFAULT_BATCH");
                }
                if (item.getQuantity()==null) {
                    item.setQuantity(0);
                }
                if (item.getUnitPrice()==null) { // 前端可能传 cost 字段映射为 unitPrice
                    if (med.getRetailPrice()!=null) {
                        item.setUnitPrice(med.getRetailPrice().doubleValue());
                    } else {
                        item.setUnitPrice(0.0);
                    }
                }
                // 关联反向引用
                item.setStockIn(stockIn);
            }
            // 设置日期与编号
            if (stockIn.getStockInDate()==null) stockIn.setStockInDate(LocalDateTime.now());
            if (stockIn.getStockInNo()==null) stockIn.setStockInNo(generateStockInNo());
            if (stockIn.getStatus()==null) stockIn.setStatus(1); // 已入库状态
            stockIn.calculateTotalAmount();
            StockIn saved = stockInRepository.save(stockIn);
            System.out.println("[StockInController] 入库单保存成功 ID="+saved.getStockInId()+" 总金额="+saved.getTotalAmount());
            // 更新库存
            for (StockInItem item : stockIn.getItems()) {
                try {
                    // 使用前面已设置到 item 的药品，避免再次查库和 Optional.get()
                    var med = item.getMedicine();
                    String batch = item.getBatchNumber();
                    Integer qty = item.getQuantity();
                    java.time.LocalDate expiry = item.getExpiryDate();
                    var invs = inventoryRepository.findByMedicineId(med.getMedicineId());
                    com.pharmacy.entity.Inventory matched = null;
                    for (com.pharmacy.entity.Inventory inv : invs) {
                        if (batch.equals(inv.getBatchNo())) { matched = inv; break; }
                    }
                    if (matched != null) {
                        matched.setStockQuantity(matched.getStockQuantity() + qty);
                        inventoryRepository.save(matched);
                    } else {
                        com.pharmacy.entity.Inventory newInv = new com.pharmacy.entity.Inventory(med.getMedicineId(), batch, qty, expiry);
                        newInv.setPurchasePrice(item.getUnitPrice()!=null? java.math.BigDecimal.valueOf(item.getUnitPrice()) : null);
                        inventoryRepository.save(newInv);
                    }
                } catch (Exception updEx) {
                    System.err.println("[StockInController] 更新库存失败: "+updEx.getMessage());
                }
            }
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("[StockInController] 创建入库单失败: "+e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("创建入库单失败: "+e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStockIn(@PathVariable Long id, @RequestBody StockIn stockInDetails) {
        try {
            Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
            if (optionalStockIn.isPresent()) {
                StockIn stockIn = optionalStockIn.get();

                // 更新基本信息
                if (stockInDetails.getSupplier() != null) {
                    stockIn.setSupplier(stockInDetails.getSupplier());
                }
                if (stockInDetails.getStockInDate() != null) {
                    stockIn.setStockInDate(stockInDetails.getStockInDate());
                }
                if (stockInDetails.getRemark() != null) {
                    stockIn.setRemark(stockInDetails.getRemark());
                }
                if (stockInDetails.getStatus() != null) {
                    stockIn.setStatus(stockInDetails.getStatus());
                }

                // 更新明细项
                if (stockInDetails.getItems() != null) {
                    // 先清除原有明细
                    stockIn.getItems().clear();

                    // 添加新的明细
                    for (StockInItem item : stockInDetails.getItems()) {
                        item.setStockIn(stockIn);
                        stockIn.getItems().add(item);
                    }
                }

                // 重新计算总金额
                stockIn.calculateTotalAmount();

                StockIn updatedStockIn = stockInRepository.save(stockIn);
                return ResponseEntity.ok(updatedStockIn);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("更新入库单失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("更新入库单失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStockIn(@PathVariable Long id) {
        if (stockInRepository.existsById(id)) {
            stockInRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<?> approveStockIn(@PathVariable Long id) {
        Optional<StockIn> optionalStockIn = stockInRepository.findById(id);
        if (optionalStockIn.isPresent()) {
            StockIn stockIn = optionalStockIn.get();
            stockIn.setStatus(1); // 已入库
            stockInRepository.save(stockIn);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Page<StockIn>> searchStockIns(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<StockIn> stockIns = stockInRepository.findByKeyword(keyword, pageable);
            return ResponseEntity.ok(stockIns);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 生成入库单号
    private String generateStockInNo() {
        return "SI" + System.currentTimeMillis();
    }

    @PostMapping("/bulk-import")
    @Transactional
    public ResponseEntity<?> bulkImport(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Integer supplierId) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要导入的文件");
        }
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        List<SimpleImportRow> rows = new ArrayList<>();
        try {
            if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
                try (InputStream is = file.getInputStream(); Workbook wb = WorkbookFactory.create(is)) {
                    Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
                    if (sheet == null) {
                        return ResponseEntity.badRequest().body("Excel 文件没有工作表");
                    }
                    int headerIdx = sheet.getFirstRowNum();
                    Row headerRow = sheet.getRow(headerIdx);
                    java.util.Map<String, Integer> hdrMap = new java.util.HashMap<>();
                    if (headerRow != null) {
                        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                            String name = cellToString(headerRow.getCell(c)).toLowerCase();
                            hdrMap.put(name, c);
                        }
                    }
                    int dataStart = headerIdx + 1;
                    for (int r = dataStart; r <= sheet.getLastRowNum(); r++) {
                        Row row = sheet.getRow(r);
                        if (row == null) continue;
                        // 允许空行跳过
                        String medId = getByAliasesExcel(row, hdrMap, 0, "medicineid","条形码","barcode","approvalno");
                        String qtyS = getByAliasesExcel(row, hdrMap, 1, "数量","qty","quantity");
                        String priceS = getByAliasesExcel(row, hdrMap, 2, "进货价","unitprice","price","cost");
                        String batch = getByAliasesExcel(row, hdrMap, 3, "批号","batch");
                        String expiry = getByAliasesExcel(row, hdrMap, 4, "到期日期","expiry","expirydate");
                        String generic = getByAliasesExcel(row, hdrMap, -1, "通用名","generic","genericname");
                        String trade = getByAliasesExcel(row, hdrMap, -1, "商品名","trade","tradename");
                        String spec = getByAliasesExcel(row, hdrMap, -1, "规格","spec");
                        String manu = getByAliasesExcel(row, hdrMap, -1, "生产厂家","manufacturer");
                        String approval = getByAliasesExcel(row, hdrMap, -1, "批准文号","approvalno");
                        String barcode = getByAliasesExcel(row, hdrMap, -1, "条形码","barcode");
                        String retailS = getByAliasesExcel(row, hdrMap, -1, "零售价","retail","retailprice");
                        String memberS = getByAliasesExcel(row, hdrMap, -1, "会员价","memberprice");
                        String unit = getByAliasesExcel(row, hdrMap, -1, "单位","unit");
                        int qty = 0; double price = 0.0; double retail = 0.0; double memberPrice = 0.0;
                        try { if (qtyS != null && !qtyS.isBlank()) qty = (int)Math.round(Double.parseDouble(qtyS)); } catch (Exception ignored) {}
                        try { if (priceS != null && !priceS.isBlank()) price = Double.parseDouble(priceS); } catch (Exception ignored) {}
                        try { if (retailS != null && !retailS.isBlank()) retail = Double.parseDouble(retailS); } catch (Exception ignored) {}
                        try { if (memberS != null && !memberS.isBlank()) memberPrice = Double.parseDouble(memberS); } catch (Exception ignored) {}
                        rows.add(new SimpleImportRow(medId, generic, trade, spec, manu, approval, barcode, qty, price, retail, batch, expiry));
                        rows.get(rows.size()-1).memberPrice = memberPrice;
                        rows.get(rows.size()-1).unit = (unit != null && !unit.isBlank()) ? unit : "盒";
                    }
                }
            } else {
                // plain text / CSV with optional header mapping
                String text = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
                String[] lines = text.split("\r?\n");
                List<String> lineList = new ArrayList<>();
                for (String l : lines) { if (l != null && !l.trim().isEmpty()) lineList.add(l); }
                if (lineList.isEmpty()) {
                    return ResponseEntity.badRequest().body("上传文件为空或不包含有效数据");
                }
                // detect header row
                int headerIdx = -1;
                String[] headerCandidates = new String[]{"通用名","generic","genericname","商品名","trade","tradeName","数量","qty","quantity","进货价","unitprice","price","批号","batch","到期日期","expiry","expirydate","条形码","barcode","批准文号","approvalno","规格","spec","生产厂家","manufacturer"};
                for (int i = 0; i < Math.min(5, lineList.size()); i++){
                    String low = lineList.get(i).toLowerCase();
                    int hits=0; for(String c: headerCandidates){ if(low.contains(c.toLowerCase())) hits++; }
                    if(hits>=2){ headerIdx = i; break; }
                }
                String[] header = null;
                java.util.Map<String,Integer> hdrMap = new java.util.HashMap<>();
                int dataStart = 0;
                if(headerIdx>=0){ header = splitCsvLine(lineList.get(headerIdx)); dataStart = headerIdx+1; for(int j=0;j<header.length;j++){ hdrMap.put(header[j].trim().toLowerCase(), j); } }
                for (int i = dataStart; i < lineList.size(); i++){
                    String line = lineList.get(i);
                    String[] parts = splitCsvLine(line);
                    if(parts.length==0) continue;
                    // helper to get by header aliases or fallback to index
                    String id = getByAliases(parts, header, hdrMap, "medicineid","medicine id","medicine","medid","条码","barcode","approvalno");
                    if(id==null || id.isBlank()) { if(parts.length>0) id = parts[0].trim(); }
                    String qtyS = getByAliases(parts, header, hdrMap, "数量","qty","quantity"); if(qtyS==null && parts.length>1) qtyS = parts[1].trim();
                    String priceS = getByAliases(parts, header, hdrMap, "进货价","unitprice","price","cost"); if(priceS==null && parts.length>2) priceS = parts[2].trim();
                    String batch = getByAliases(parts, header, hdrMap, "批号","batch"); if(batch==null && parts.length>3) batch = parts[3].trim();
                    String expiry = getByAliases(parts, header, hdrMap, "到期日期","expiry","expirydate"); if(expiry==null && parts.length>4) expiry = parts[4].trim();
                    String generic = getByAliases(parts, header, hdrMap, "通用名","generic","genericname");
                    String trade = getByAliases(parts, header, hdrMap, "商品名","trade","tradename");
                    String spec = getByAliases(parts, header, hdrMap, "规格","spec");
                    String manu = getByAliases(parts, header, hdrMap, "生产厂家","manufacturer");
                    String approval = getByAliases(parts, header, hdrMap, "批准文号","approvalno");
                    String barcode = getByAliases(parts, header, hdrMap, "条形码","barcode");
                    String retailS = getByAliases(parts, header, hdrMap, "零售价","retail","retailprice");
                    int qty = 0; double price = 0.0; double retail = 0.0;
                    try{ if(qtyS!=null && !qtyS.isBlank()) qty = (int)Math.round(Double.parseDouble(qtyS)); }catch(Exception ex){ qty=0; }
                    try{ if(priceS!=null && !priceS.isBlank()) price = Double.parseDouble(priceS); }catch(Exception ex){ price=0.0; }
                    try{ if(retailS!=null && !retailS.isBlank()) retail = Double.parseDouble(retailS); }catch(Exception ex){ retail=0.0; }
                    rows.add(new SimpleImportRow(id, generic, trade, spec, manu, approval, barcode, qty, price, retail, batch, expiry));
                }
             }
         } catch (Exception e) {
             e.printStackTrace();
             return ResponseEntity.internalServerError().body("解析文件失败: " + e.getMessage());
         }

        if (rows.isEmpty()) return ResponseEntity.badRequest().body("未解析到有效数据");

        try {
            // build StockIn
            StockIn stockIn = new StockIn();
            if (supplierId != null) {
                Optional<com.pharmacy.entity.Supplier> s = supplierRepository.findById(supplierId);
                s.ifPresent(stockIn::setSupplier);
            } else {
                Optional<com.pharmacy.entity.Supplier> s = supplierRepository.findById(1);
                s.ifPresent(stockIn::setSupplier);
            }
            stockIn.setStockInDate(LocalDateTime.now());
            stockIn.setStockInNo(generateStockInNo());
            stockIn.setStatus(1);
            // create items
            for (SimpleImportRow r : rows) {
                String medId = r.medicineId!=null? r.medicineId.trim() : "";
                com.pharmacy.entity.Medicine med = null;
                if (!medId.isBlank()) {
                    Optional<com.pharmacy.entity.Medicine> medOpt = medicineRepository.findById(medId);
                    if (medOpt.isPresent()) med = medOpt.get();
                }
                // Try by barcode
                if (med == null && r.barcode != null && !r.barcode.isBlank()){
                    List<com.pharmacy.entity.Medicine> byBc = medicineRepository.searchByKeyword(r.barcode);
                    if(byBc != null && !byBc.isEmpty()) med = byBc.get(0);
                }
                // Try by generic+spec+manufacturer
                if (med == null && r.genericName != null && !r.genericName.isBlank()){
                    com.pharmacy.entity.Medicine found = medicineRepository.findByGenericNameAndSpecAndManufacturer(r.genericName, r.spec==null?"":r.spec, r.manufacturer==null?"":r.manufacturer);
                    if (found != null) med = found;
                }
                // If not found, auto-create medicine if we have a genericName or tradeName
                if (med == null) {
                    if ((r.genericName==null || r.genericName.isBlank()) && (r.medicineId==null || r.medicineId.isBlank())){
                        return ResponseEntity.badRequest().body("导入行药品不存在且缺少可用的通用名或ID: " + r.medicineId);
                    }
                    // create new medicine
                    com.pharmacy.entity.Medicine newMed = new com.pharmacy.entity.Medicine();
                    String newId = "M" + System.currentTimeMillis() + (int)(Math.random()*900+100);
                    newMed.setMedicineId(newId);
                    newMed.setGenericName(r.genericName!=null && !r.genericName.isBlank()? r.genericName : (r.tradeName!=null? r.tradeName : newId));
                    newMed.setTradeName(r.tradeName!=null? r.tradeName : null);
                    newMed.setSpec(r.spec!=null? r.spec : null);
                    newMed.setManufacturer(r.manufacturer!=null? r.manufacturer : null);
                    // approvalNo is non-nullable; generate if missing
                    String apr = (r.approvalNo!=null && !r.approvalNo.isBlank())? r.approvalNo : ("AUTO-"+System.currentTimeMillis());
                    newMed.setApprovalNo(apr);
                    newMed.setCategoryId( (r.categoryId!=null)? r.categoryId : 1 );
                    newMed.setRetailPrice(java.math.BigDecimal.valueOf(r.retailPrice>0? r.retailPrice : 0.0));
                    if (r.memberPrice>0) newMed.setMemberPrice(java.math.BigDecimal.valueOf(r.memberPrice));
                    newMed.setIsRx(false);
                    newMed.setUnit((r.unit!=null && !r.unit.isBlank())? r.unit : "盒");
                    newMed.setBarcode((r.barcode!=null && !r.barcode.isBlank())? r.barcode : null);
                    newMed.setStatus("ACTIVE");
                    medicineRepository.save(newMed);
                    med = newMed;
                }
                StockInItem item = new StockInItem();
                item.setMedicine(med);
                item.setMedicineId(med.getMedicineId());
                item.setQuantity(r.quantity);
                item.setUnitPrice(r.unitPrice);
                item.setBatchNumber(r.batchNumber==null || r.batchNumber.isBlank()? "DEFAULT_BATCH" : r.batchNumber);
                 if (r.expiryDate != null && !r.expiryDate.isBlank()){
                     try{ item.setExpiryDate(LocalDate.parse(r.expiryDate)); }catch(Exception ex){ /* ignore parse, leave null */ }
                 }
                 item.setStockIn(stockIn);
                 stockIn.getItems().add(item);
            }
            stockIn.calculateTotalAmount();
            StockIn saved = stockInRepository.save(stockIn);
            // update inventory same as createStockIn
            for (StockInItem item : stockIn.getItems()) {
                try {
                    var med = item.getMedicine();
                    String batch = item.getBatchNumber();
                    Integer qty = item.getQuantity();
                    java.time.LocalDate expiry = item.getExpiryDate();
                    var invs = inventoryRepository.findByMedicineId(med.getMedicineId());
                    com.pharmacy.entity.Inventory matched = null;
                    for (com.pharmacy.entity.Inventory inv : invs) {
                        if (batch.equals(inv.getBatchNo())) { matched = inv; break; }
                    }
                    if (matched != null) {
                        matched.setStockQuantity(matched.getStockQuantity() + qty);
                        inventoryRepository.save(matched);
                    } else {
                        com.pharmacy.entity.Inventory newInv = new com.pharmacy.entity.Inventory(med.getMedicineId(), batch, qty, expiry);
                        newInv.setPurchasePrice(item.getUnitPrice()!=null? java.math.BigDecimal.valueOf(item.getUnitPrice()) : null);
                        inventoryRepository.save(newInv);
                    }
                } catch (Exception updEx) {
                    System.err.println("[bulk-import] 更新库存失败: "+updEx.getMessage());
                }
            }
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("批量导入失败: " + e.getMessage());
        }
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case FORMULA -> {
                try { yield cell.getStringCellValue(); } catch (Exception ex) { yield String.valueOf(cell.getNumericCellValue()); }
            }
            default -> "";
        };
    }

    private static String getByAliasesExcel(Row row, java.util.Map<String,Integer> hdrMap, int defaultIdx, String... aliases){
        for(String a: aliases){
            Integer idx = hdrMap.get(a.toLowerCase());
            if(idx!=null){ return cellToString(row.getCell(idx)).trim(); }
        }
        if(defaultIdx>=0){ Cell c = row.getCell(defaultIdx); if(c!=null) return cellToString(c).trim(); }
        return null;
    }

    private static class SimpleImportRow {
        public String medicineId; public String genericName; public String tradeName; public String spec; public String manufacturer; public String approvalNo; public String barcode; public Integer categoryId; public String unit; public int quantity; public double unitPrice; public double retailPrice; public double memberPrice; public String batchNumber; public String expiryDate;
        public SimpleImportRow(String medicineId, String genericName, String tradeName, String spec, String manufacturer, String approvalNo, String barcode, int quantity, double unitPrice, double retailPrice, String batchNumber, String expiryDate){
            this.medicineId=medicineId; this.genericName=genericName; this.tradeName=tradeName; this.spec=spec; this.manufacturer=manufacturer; this.approvalNo=approvalNo; this.barcode=barcode; this.quantity=quantity; this.unitPrice=unitPrice; this.retailPrice=retailPrice; this.batchNumber=batchNumber; this.expiryDate=expiryDate; this.memberPrice = 0.0; this.categoryId = 1; this.unit = "盒"; }
    }

    // 简单 CSV 行分割，支持用双引号包裹含逗号字段
    private static String[] splitCsvLine(String line){
        if(line==null) return new String[0];
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes=false;
        for(int i=0;i<line.length();i++){
            char ch = line.charAt(i);
            if(ch=='"'){
                if(inQuotes && i+1<line.length() && line.charAt(i+1)=='"'){
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if(ch==',' && !inQuotes){
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // 从 parts/header/hdrMap 按别名查值的辅助方法（避免 lambda 捕获非 final 局部变量）
    private static String getByAliases(String[] parts, String[] header, java.util.Map<String,Integer> hdrMap, String... aliases){
        if(header!=null){
            for(String a: aliases){ Integer idx = hdrMap.get(a.toLowerCase()); if(idx!=null && idx < parts.length) return parts[idx].trim(); }
        }
        return null;
    }

}
