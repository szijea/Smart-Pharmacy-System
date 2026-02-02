package com.pharmacy.controller;

import com.pharmacy.entity.Inventory;
import com.pharmacy.dto.InventoryDTO;
import com.pharmacy.dto.CurrentStockDTO;
import com.pharmacy.dto.InventoryTransferRequest;
import com.pharmacy.service.InventoryTransferService;
import com.pharmacy.service.InventoryService;
import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryTransferService inventoryTransferService;

    @Autowired
    private MultiTenantDataSourceConfig multiTenantDataSourceConfig;

    @GetMapping("/tenants")
    public ResponseEntity<?> getAvailableTenants() {
        return ResponseEntity.ok(multiTenantDataSourceConfig.getTenantIds());
    }

    @GetMapping("/expiring-soon")
    public ResponseEntity<?> getExpiringSoon() {
        try {
            List<Inventory> inventories = inventoryService.getExpiringSoon();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", inventories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取即将过期药品失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllInventory() {
        try {
            // 使用 DTO 返回，避免懒加载代理序列化失败
            List<InventoryDTO> inventories = inventoryService.findAllWithMedicineDTO();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", inventories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取库存列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStock() {
        try {
            List<Inventory> inventories = inventoryService.getLowStock();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", inventories);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取低库存药品失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryDetail(@PathVariable("id") Long id) {
        try {
            InventoryDTO dto = inventoryService.findDTOById(id);
            if (dto == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "库存记录不存在: " + id);
                return ResponseEntity.status(404).body(response);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取库存详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/by-medicine/{medicineId}")
    public ResponseEntity<?> getInventoryByMedicine(@PathVariable String medicineId) {
        try {
            List<InventoryDTO> list = inventoryService.findDTOByMedicineId(medicineId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取药品批次库存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/replenish")
    public ResponseEntity<?> replenish(@RequestBody Map<String,Object> body) {
        try {
            String medicineId = String.valueOf(body.get("medicineId"));
            Integer addQuantity = body.get("quantity") != null ? Integer.valueOf(body.get("quantity").toString()) : null;
            String batchNo = body.get("batchNo") != null ? String.valueOf(body.get("batchNo")) : null;
            Integer minStock = body.get("minStock") != null ? Integer.valueOf(body.get("minStock").toString()) : null;
            java.time.LocalDate expiryDate = null;
            if (body.get("expiryDate") != null && !String.valueOf(body.get("expiryDate")).isBlank()) {
                try { expiryDate = java.time.LocalDate.parse(String.valueOf(body.get("expiryDate"))); } catch (Exception ignored) {}
            }
            if (medicineId == null || medicineId.isBlank() || addQuantity == null || addQuantity < 0) {
                return ResponseEntity.badRequest().body(Map.of("code",400,"message","参数不合法"));
            }
            Inventory inv = inventoryService.replenish(medicineId, addQuantity, batchNo, minStock);
            if (inv != null && expiryDate != null) {
                inv.setExpiryDate(expiryDate);
                inventoryService.save(inv);
            }
            // 返回当前该药品所有批次 DTO（含统一状态）
            List<InventoryDTO> dtoList = inventoryService.findDTOByMedicineId(medicineId);
            return ResponseEntity.ok(Map.of("code",200,"message","补货成功","data", dtoList));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","补货失败: "+e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<?> createBatch(@RequestBody Map<String,Object> body) {
        try {
            String medicineId = String.valueOf(body.get("medicineId"));
            String batchNo = String.valueOf(body.getOrDefault("batchNo","AUTO"+System.currentTimeMillis()));
            Integer stockQuantity = body.get("stockQuantity")!=null?Integer.valueOf(body.get("stockQuantity").toString()):0;
            Integer minStock = body.get("minStock")!=null?Integer.valueOf(body.get("minStock").toString()):null;
            Integer maxStock = body.get("maxStock")!=null?Integer.valueOf(body.get("maxStock").toString()):null;
            java.math.BigDecimal purchasePrice = body.get("purchasePrice")!=null?new java.math.BigDecimal(body.get("purchasePrice").toString()):null;
            java.time.LocalDate expiryDate = null;
            if (body.get("expiryDate")!=null) {
                expiryDate = java.time.LocalDate.parse(body.get("expiryDate").toString());
            }
            String supplier = body.get("supplier")!=null?String.valueOf(body.get("supplier")) : null;
            if (medicineId == null || medicineId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("code",400,"message","缺少 medicineId"));
            }
            Inventory inv = inventoryService.createBatch(medicineId,batchNo,stockQuantity,minStock,maxStock,purchasePrice,expiryDate,supplier);
            return ResponseEntity.ok(Map.of("code",200,"message","新建批次成功","data",inv.getId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","新建批次失败: "+e.getMessage()));
        }
    }

    @GetMapping("/search-batch")
    public ResponseEntity<?> searchByBatch(@RequestParam String batchNo){
        try {
            java.util.List<com.pharmacy.dto.InventoryDTO> list = inventoryService.findDTOByBatchNo(batchNo);
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",200);
            resp.put("message","success");
            resp.put("data", list);
            resp.put("total", list.size());
            return ResponseEntity.ok(resp);
        } catch (Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","按批号查询失败: "+e.getMessage(),"data", java.util.List.of()));
        }
    }

    @GetMapping("/current-stock")
    public ResponseEntity<?> getCurrentStock(@RequestParam String medicineId){
        if(medicineId==null || medicineId.isBlank()){
            return ResponseEntity.badRequest().body(Map.of("code",400,"message","缺少 medicineId"));
        }
        try {
            Integer stock = inventoryService.getCurrentStock(medicineId.trim());
            return ResponseEntity.ok(Map.of("code",200,"message","success","medicineId", medicineId.trim(), "currentStock", stock));
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","查询当前库存失败: "+e.getMessage()));
        }
    }

    @GetMapping("/current-stocks")
    public ResponseEntity<?> getCurrentStocks(@RequestParam String ids){
        if(ids==null || ids.isBlank()){
            return ResponseEntity.badRequest().body(Map.of("code",400,"message","缺少 ids"));
        }
        try {
            // medicine IDs are strings (medicine.medicineId). Keep them as String list.
            List<String> list = java.util.Arrays.stream(ids.split(",")).map(String::trim).filter(s->!s.isEmpty()).distinct().toList();
            List<CurrentStockDTO> dtos = inventoryService.getCurrentStocks(list);
            return ResponseEntity.ok(Map.of("code",200,"message","success","results", dtos, "total", dtos.size()));
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","批量查询库存失败: "+e.getMessage(),"results", java.util.List.of()));
        }
    }

    @GetMapping("/page-current-stocks")
    public ResponseEntity<?> pageCurrentStocks(@RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size){
        try {
            Page<InventoryDTO> p = inventoryService.pageCurrentStocks(page, size);
            Map<String,Object> resp = new HashMap<>();
            resp.put("code",200);
            resp.put("message","success");
            resp.put("page", page);
            resp.put("size", size);
            resp.put("totalElements", p.getTotalElements());
            resp.put("totalPages", p.getTotalPages());
            resp.put("content", p.getContent());
            return ResponseEntity.ok(resp);
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","分页查询库存失败: "+e.getMessage()));
        }
    }

    @PostMapping("/stock-in")
    public ResponseEntity<?> stockIn(@RequestBody Map<String,Object> body){
        try {
            // medicineId is a String (medicine.medicineId is String in entity)
            String medicineId = body.get("medicineId")!=null?String.valueOf(body.get("medicineId")).trim():null;
            Integer quantity = body.get("quantity")!=null?Integer.valueOf(String.valueOf(body.get("quantity"))):null;
            String batchNo = body.get("batchNo")!=null?String.valueOf(body.get("batchNo")).trim():null;
            java.math.BigDecimal unitPrice = null;
            if(body.get("unitPrice")!=null){
                try{ unitPrice = new java.math.BigDecimal(String.valueOf(body.get("unitPrice"))); }catch(Exception ignore){}
            }
            // java.time.LocalDate productionDate = null; // Unused
            java.time.LocalDate expiryDate = null;
            if(body.get("expiryDate")!=null){
                try{ expiryDate = java.time.LocalDate.parse(String.valueOf(body.get("expiryDate"))); }catch(Exception ignore){}
            }
            if(medicineId==null || medicineId.isBlank() || quantity==null || quantity<=0){
                return ResponseEntity.badRequest().body(Map.of("code",400,"message","参数不合法: 需要 medicineId 与 正整数 quantity"));
            }
            Inventory inv = inventoryService.replenish(medicineId, quantity, batchNo);
            if(inv!=null){
                if(unitPrice!=null) inv.setPurchasePrice(unitPrice);
                if(expiryDate!=null) inv.setExpiryDate(expiryDate);
                inventoryService.save(inv);
            }
            Integer currentStock = inventoryService.getCurrentStock(medicineId);
            return ResponseEntity.ok(Map.of("code",200,"message","入库成功","medicineId",medicineId,"added",quantity,"currentStock",currentStock));
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","入库失败: "+e.getMessage()));
        }
    }

    @PostMapping("/stock-out")
    public ResponseEntity<?> stockOut(@RequestBody Map<String,Object> body){
        try {
            String medicineId = body.get("medicineId")!=null?String.valueOf(body.get("medicineId")).trim():null;
            Integer quantity = body.get("quantity")!=null?Integer.valueOf(String.valueOf(body.get("quantity"))):null;
            if(medicineId==null || quantity==null || quantity<=0){
                return ResponseEntity.badRequest().body(Map.of("code",400,"message","参数不合法: 需要 medicineId 与 正整数 quantity"));
            }
            boolean ok = inventoryService.updateStockForOrder(medicineId, quantity, "MANUAL_STOCK_OUT");
            Integer currentStock = inventoryService.getCurrentStock(medicineId);
            if(!ok){
                return ResponseEntity.status(409).body(Map.of("code",409,"message","库存不足","medicineId",medicineId,"attemptDeduct",quantity,"currentStock",currentStock));
            }
            return ResponseEntity.ok(Map.of("code",200,"message","出库成功","medicineId",medicineId,"deduct",quantity,"currentStock",currentStock));
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","出库失败: "+e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchInventory(@RequestParam(required=false) String keyword) {
        try {
            String kw = keyword == null ? "" : keyword.trim();
            List<InventoryDTO> all = inventoryService.findAllWithMedicineDTO();
            if (kw.isEmpty()) {
                return ResponseEntity.ok(Map.of("code",200,"message","success","data", all, "total", all.size()));
            }
            String low = kw.toLowerCase();
            List<InventoryDTO> filtered = all.stream().filter(dto -> {
                String bno = dto.getBatchNo();
                String gn = dto.getMedicineGenericName();
                String tn = dto.getMedicineTradeName();
                String spec = dto.getMedicineSpec();
                return (bno!=null && bno.toLowerCase().contains(low))
                        || (gn!=null && gn.toLowerCase().contains(low))
                        || (tn!=null && tn.toLowerCase().contains(low))
                        || (spec!=null && spec.toLowerCase().contains(low));
            }).toList();
            return ResponseEntity.ok(Map.of("code",200,"message","success","data", filtered, "total", filtered.size()));
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","库存关键字搜索失败: "+e.getMessage(), "data", java.util.List.of()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferStock(@RequestBody InventoryTransferRequest request) {
        try {
            if (request.getFromTenant() == null || request.getFromTenant().isBlank() ||
                request.getToTenant() == null || request.getToTenant().isBlank() ||
                request.getMedicineId() == null || request.getMedicineId().isBlank() ||
                request.getBatchNo() == null || request.getBatchNo().isBlank() ||
                request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("code", 400, "message", "Invalid transfer request parameters."));
            }

            inventoryTransferService.transferStock(
                request.getFromTenant(),
                request.getToTenant(),
                request.getMedicineId(),
                request.getBatchNo(),
                request.getQuantity(),
                request.getOperatorId(),
                request.getRemark()
            );

            return ResponseEntity.ok(Map.of("code", 200, "message", "Stock transfer initiated successfully."));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code", 500, "message", "Stock transfer failed: " + e.getMessage()));
        }
    }
}
