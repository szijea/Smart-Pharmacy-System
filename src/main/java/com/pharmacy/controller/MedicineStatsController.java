package com.pharmacy.controller;

import com.pharmacy.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/medicines")
public class MedicineStatsController {

    @Autowired
    private MedicineRepository medicineRepository;

    @GetMapping("/category-stats")
    public ResponseEntity<Map<String,Object>> categoryStats(){
        List<Map<String,Object>> data = new ArrayList<>();
        try{
            // 优先使用带 Category 名称的分组
            List<Object[]> rows = medicineRepository.getCategoryDistribution();
            for(Object[] r: rows){
                if(r == null) continue;
                Object name = (r.length>0? r[0]: "未分类");
                Object cnt  = (r.length>1? r[1]: 0);
                Map<String,Object> m = new HashMap<>();
                m.put("name", String.valueOf(name));
                m.put("count", toInt(cnt));
                data.add(m);
            }
        }catch(Exception e){
            // 回退：按 category_id 分组计数（需要 nativeQuery 方法）
            try{
                List<Object[]> rows2 = medicineRepository.countGroupByCategory();
                for(Object[] r: rows2){
                    Object catId = (r != null && r.length>0)? r[0]: null;
                    Object cnt   = (r != null && r.length>1)? r[1]: 0;
                    Map<String,Object> m = new HashMap<>();
                    m.put("name", catId == null? "未分类" : String.valueOf(catId));
                    m.put("count", toInt(cnt));
                    data.add(m);
                }
            }catch(Exception ignored){ /* 返回空 */ }
        }
        return ResponseEntity.ok(Map.of("data", data));
    }

    private int toInt(Object o){
        if(o instanceof Number) return ((Number)o).intValue();
        try{ return Integer.parseInt(String.valueOf(o)); }catch(Exception e){ return 0; }
    }
}
