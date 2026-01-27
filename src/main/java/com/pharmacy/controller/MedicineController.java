// MedicineController.java - 修复版本
package com.pharmacy.controller;

import com.pharmacy.dto.MedicineWithStockDTO;
import com.pharmacy.entity.Medicine;
import com.pharmacy.service.MedicineService;
import com.pharmacy.service.InventoryService;
import com.pharmacy.util.StockStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicines")
// 使用全局 CORS 配置，移除局部 @CrossOrigin
public class MedicineController {

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private com.pharmacy.repository.OrderItemRepository orderItemRepository;

    // 注意：这里只有药品相关的服务，没有memberService

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Medicine Service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("port", 8080);
        return ResponseEntity.ok(response);
    }

    // 测试端点
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Medicine Service is working! - " + java.time.LocalDateTime.now());
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMedicines(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        System.out.println("=== 接收药品搜索请求(标准DTO) ===");
        System.out.println("keyword="+keyword+", category="+category+", page="+page+", size="+size);
        try {
            Page<com.pharmacy.dto.MedicineWithStockDTO> dtoPage = medicineService.searchMedicinesWithStock(keyword, category, page, size);
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",200);
            resp.put("message","success");
            resp.put("data", dtoPage.getContent());
            resp.put("total", dtoPage.getTotalElements());
            resp.put("currentPage", page);
            resp.put("totalPages", dtoPage.getTotalPages());
            return ResponseEntity.ok(resp);
        } catch (Exception e){
            e.printStackTrace();
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",500);
            resp.put("message","药品搜索失败: "+e.getMessage());
            resp.put("data", List.of());
            return ResponseEntity.internalServerError().body(resp);
        }
    }

    // 其他药品相关的方法保持不变...
    @GetMapping
    public ResponseEntity<Page<Medicine>> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Medicine> medicines = medicineService.getAllMedicines(pageable);
        return ResponseEntity.ok(medicines);
    }

    // 新增：创建药品（与前端 manualAddMedicine 对应）
    @PostMapping
    public ResponseEntity<?> createMedicine(@RequestBody Medicine medicine) {
        try {
            String rawApproval = medicine.getApprovalNo();
            if (rawApproval == null || rawApproval.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "message", "批准文号不能为空"
                ));
            }
            medicine.setApprovalNo(rawApproval.trim());
            if (medicine.getStatus() == null || medicine.getStatus().isBlank()) {
                medicine.setStatus("ACTIVE");
            }
            // 去重：优先按 approvalNo 去重
            Medicine existingByApproval = medicineService.findByApprovalNo(medicine.getApprovalNo());
            if (existingByApproval != null) {
                return ResponseEntity.ok(existingByApproval); // 已存在直接返回现有记录
            }
            // 次要去重：generic+spec+manufacturer
            if ((medicine.getGenericName() != null) && (medicine.getSpec() != null) && (medicine.getManufacturer() != null)) {
                Medicine dup = medicineService.findByGenericSpecManufacturer(medicine.getGenericName(), medicine.getSpec(), medicine.getManufacturer());
                if (dup != null) {
                    return ResponseEntity.ok(dup);
                }
            }
            // 补必填字段
            if (medicine.getMedicineId() == null || medicine.getMedicineId().trim().isEmpty()) {
                medicine.setMedicineId("M" + System.currentTimeMillis());
            }
            if (medicine.getGenericName() == null || medicine.getGenericName().trim().isEmpty()) {
                medicine.setGenericName(medicine.getTradeName() != null ? medicine.getTradeName() : "手动添加药品");
            }
            if (medicine.getSpec() == null || medicine.getSpec().trim().isEmpty()) {
                medicine.setSpec("未填写规格");
            }
            if (medicine.getCategoryId() == null) {
                medicine.setCategoryId(2);
            }
            if (medicine.getRetailPrice() == null) {
                medicine.setRetailPrice(new java.math.BigDecimal("0.00"));
            }
            Medicine created = medicineService.createMedicine(medicine);
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",200);
            resp.put("message","创建成功");
            resp.put("data", created);
            resp.put("medicineStatus", created.getStatus());
            java.time.LocalDate expiry = created.getExpiryDate();
            resp.put("expiryStatus", StockStatusUtil.calcExpiryStatus(expiry));
            Integer currentStock = inventoryService.getCurrentStock(created.getMedicineId());
            resp.put("currentStock", currentStock);
            return ResponseEntity.ok(resp);
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "创建药品失败(数据完整性): " + dive.getMostSpecificCause().getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", 500,
                    "message", "创建药品失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable String id) {
        Medicine medicine = medicineService.getMedicineById(id);
        if (medicine != null) {
            return ResponseEntity.ok(medicine);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search-with-stock")
    public ResponseEntity<Map<String, Object>> searchMedicinesWithStock(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {

        System.out.println("=== 接收药品搜索请求（包含库存） ===");
        System.out.println("keyword: " + keyword);
        System.out.println("category: " + category);
        System.out.println("page: " + page);
        System.out.println("size: " + size);

        try {
            Page<MedicineWithStockDTO> medicinePage = medicineService.searchMedicinesWithStock(keyword, category, page, size);
            List<MedicineWithStockDTO> medicines = medicinePage.getContent();

            System.out.println("药品搜索结果数量: " + medicines.size());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", medicines);
            response.put("total", medicinePage.getTotalElements());
            response.put("currentPage", page);
            response.put("totalPages", medicinePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("药品搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "药���搜索失败: " + e.getMessage());
            response.put("data", List.of());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMedicine(@PathVariable String id, @RequestBody Medicine medicine) {
        try {
            Medicine existing = medicineService.getMedicineById(id);
            if (existing == null) {
                return ResponseEntity.status(404).body(Map.of("code",404,"message","药品不存在"));
            }
            // 批准文号处理：如果前端传入非空且不同，验证唯一
            String incomingApproval = medicine.getApprovalNo();
            if (incomingApproval != null) {
                String trimmed = incomingApproval.trim();
                if (trimmed.isEmpty()) {
                    // 空字符串不覆盖原值
                    medicine.setApprovalNo(existing.getApprovalNo());
                } else if (!trimmed.equals(existing.getApprovalNo())) {
                    Medicine conflict = medicineService.findByApprovalNo(trimmed);
                    if (conflict != null && !conflict.getMedicineId().equals(id)) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "code", 400,
                                "message", "批准文号已存在，不能重复: " + trimmed
                        ));
                    }
                    medicine.setApprovalNo(trimmed);
                } else {
                    medicine.setApprovalNo(existing.getApprovalNo());
                }
            }
            if (medicine.getStatus() == null || medicine.getStatus().isBlank()) {
                medicine.setStatus(existing.getStatus());
            }
            Medicine updated = medicineService.updateMedicine(id, medicine);
            return ResponseEntity.ok(Map.of("code",200,"data",updated));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code",500,"message","更新药品失败: "+e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMedicine(@PathVariable String id) {
        try {
            Medicine med = medicineService.getMedicineById(id);
            if (med == null) {
                return ResponseEntity.status(404).body(Map.of("code",404,"message","药品不存在"));
            }
            medicineService.deleteMedicine(id);
            return ResponseEntity.ok(Map.of("code",200,"message","删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code",500,"message","删除药品失败: "+e.getMessage()));
        }
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreMedicine(@PathVariable String id) {
        try {
            Medicine restored = medicineService.getMedicineById(id);
            if (restored == null) {
                return ResponseEntity.status(404).body(Map.of("code",404,"message","药品不存在"));
            }
            medicineService.restoreMedicine(id);
            return ResponseEntity.ok(Map.of("code",200,"message","恢复成功"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("code",500,"message","恢复药品失败: "+e.getMessage()));
        }
    }

    // 删除所有与memberService相关的方法和引用

    @PostMapping("/create-with-stock")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> createMedicineWithInitialStock(@RequestBody Map<String,Object> body) {
        try {
            // 解析 medicine 字段（可能是嵌套对象或直接平铺）
            Object medObj = body.get("medicine");
            com.pharmacy.entity.Medicine medicine;
            if (medObj instanceof Map<?,?> m) {
                medicine = new com.pharmacy.entity.Medicine();
                medicine.setMedicineId(stringField(m.get("medicineId")));
                medicine.setGenericName(stringField(m.get("genericName")));
                medicine.setTradeName(stringField(m.get("tradeName")));
                medicine.setSpec(stringField(m.get("spec")));
                medicine.setApprovalNo(stringField(m.get("approvalNo")));
                medicine.setCategoryId(intField(m.get("categoryId"), 1));
                medicine.setManufacturer(stringField(m.get("manufacturer")));
                medicine.setRetailPrice(bigDecimalField(m.get("retailPrice"), "0.00"));
                medicine.setMemberPrice(bigDecimalField(m.get("memberPrice"), null));
                medicine.setIsRx(boolField(m.get("isRx"), false));
                medicine.setUnit(stringField(m.get("unit")));
                medicine.setDescription(stringField(m.get("description")));
                medicine.setBarcode(stringField(m.get("barcode")));
                medicine.setProductionDate(dateField(m.get("productionDate")));
                medicine.setExpiryDate(dateField(m.get("expiryDate")));
                medicine.setStatus(stringField(m.get("status")));
            } else {
                // 平铺模式
                medicine = new com.pharmacy.entity.Medicine();
                medicine.setMedicineId(stringField(body.get("medicineId")));
                medicine.setGenericName(stringField(body.get("genericName")));
                medicine.setTradeName(stringField(body.get("tradeName")));
                medicine.setSpec(stringField(body.get("spec")));
                medicine.setApprovalNo(stringField(body.get("approvalNo")));
                medicine.setCategoryId(intField(body.get("categoryId"), 1));
                medicine.setManufacturer(stringField(body.get("manufacturer")));
                medicine.setRetailPrice(bigDecimalField(body.get("retailPrice"), "0.00"));
                medicine.setMemberPrice(bigDecimalField(body.get("memberPrice"), null));
                medicine.setIsRx(boolField(body.get("isRx"), false));
                medicine.setUnit(stringField(body.get("unit")));
                medicine.setDescription(stringField(body.get("description")));
                medicine.setBarcode(stringField(body.get("barcode")));
                medicine.setProductionDate(dateField(body.get("productionDate")));
                medicine.setExpiryDate(dateField(body.get("expiryDate")));
                medicine.setStatus(stringField(body.get("status")));
            }
            if (medicine.getStatus()==null || medicine.getStatus().isBlank()) medicine.setStatus("ACTIVE");
            // 去重校验批准文号
            if (medicine.getApprovalNo()!=null && !medicine.getApprovalNo().isBlank()) {
                com.pharmacy.entity.Medicine dup = medicineService.findByApprovalNo(medicine.getApprovalNo().trim());
                if (dup!=null) {
                    return ResponseEntity.ok(Map.of("code",200,"message","已存在的药品","data",dup));
                }
            }
            com.pharmacy.entity.Medicine created = medicineService.createMedicine(medicine);
            // 初始库存参数
            String batchNo = stringField(body.get("batchNo"));
            Integer quantity = intField(body.get("quantity"), 0);
            Integer minStock = intField(body.get("minStock"), null);
            java.time.LocalDate expiryDate = dateField(body.get("batchExpiryDate"));
            if (batchNo == null || batchNo.isBlank()) batchNo = "INIT"+System.currentTimeMillis();
            com.pharmacy.entity.Inventory inv = inventoryService.createBatch(created.getMedicineId(), batchNo, quantity, minStock, null, null, expiryDate, null);
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",200);
            resp.put("message","创建药品并初始化库存成功");
            resp.put("medicine", created);
            resp.put("inventoryId", inv.getId());
            resp.put("batchNo", batchNo);
            resp.put("stockQuantity", inv.getStockQuantity());
            resp.put("minStock", inv.getMinStock());
            return ResponseEntity.ok(resp);
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("code",500,"message","创建药品并入库失败: "+e.getMessage()));
        }
    }

    private String stringField(Object v){ return v==null?null:String.valueOf(v).trim(); }
    private Long longField(Object v){ try { return v==null?null:Long.valueOf(String.valueOf(v)); } catch(Exception e){ return null; } }
    private Integer intField(Object v, Integer def){ try { return v==null?def:Integer.valueOf(String.valueOf(v)); } catch(Exception e){ return def; } }
    private java.math.BigDecimal bigDecimalField(Object v, String def){ try { return v==null?(def==null?null:new java.math.BigDecimal(def)):new java.math.BigDecimal(String.valueOf(v)); } catch(Exception e){ return def==null?null:new java.math.BigDecimal(def); } }
    private Boolean boolField(Object v, Boolean def){ if (v==null) return def; if (v instanceof Boolean b) return b; return "true".equalsIgnoreCase(String.valueOf(v)); }
    private java.time.LocalDate dateField(Object v){ try { return v==null?null:java.time.LocalDate.parse(String.valueOf(v)); } catch(Exception e){ return null; } }
}
