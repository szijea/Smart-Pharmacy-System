package com.pharmacy.dto;

import java.util.List;
import java.math.BigDecimal; // 添加此导入语句

public class SalesTrendDTO {
    private List<String> labels;
    private List<BigDecimal> values;

    // getter 和 setter 方法
    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<BigDecimal> getValues() {
        return values;
    }

    public void setValues(List<BigDecimal> values) {
        this.values = values;
    }
}