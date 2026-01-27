package com.pharmacy.controller;

import com.pharmacy.entity.Medicine;
import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 兼容前端旧路径 /api/medicine/search （注意原控制器使用 /api/medicines）。
 * 返回简化数组结构，避免前端无法解析包装对象。
 */
@RestController
@RequestMapping("/api/medicine")
public class MedicineAliasController {
    @Autowired
    private MedicineRepository medicineRepository;

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(required=false) String keyword){
        try {
            if(keyword==null || keyword.isBlank()){
                List<Medicine> all = medicineRepository.findAllActive(org.springframework.data.domain.PageRequest.of(0,100)).getContent();
                return ResponseEntity.ok(all.stream().map(this::simple).toList());
            }
            List<Medicine> list = medicineRepository.searchActiveByKeyword(keyword.trim());
            return ResponseEntity.ok(list.stream().map(this::simple).toList());
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(Map.of("code",500,"message","搜索失败: "+e.getMessage()));
        }
    }

    private Map<String,Object> simple(Medicine m){
        return Map.of(
                "medicineId", m.getMedicineId(),
                "genericName", m.getGenericName(),
                "tradeName", m.getTradeName()
        );
    }
}

