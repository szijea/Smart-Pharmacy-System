package com.pharmacy.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StockInDTO {
    private Long stockInId;
    private String stockInNo;
    private Integer supplierId;
    private String supplierName;
    private LocalDateTime stockInDate;
    private Double totalAmount;
    private Integer operatorId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<StockInItemDTO> items;

    // 构造器、getter、setter
    public StockInDTO() {}

    // 省略getter和setter...
}