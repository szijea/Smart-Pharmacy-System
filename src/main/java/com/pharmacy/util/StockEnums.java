package com.pharmacy.util;

/**
 * 统一库存与效期状态枚举及文本映射，供后端/前端复用，减少重复计算与魔法字符串。
 */
public final class StockEnums {
    private StockEnums() {}

    public enum StockStatus {
        OUT, CRITICAL, LOW, MEDIUM, HIGH;
        public String label(){
            return switch (this){
                case OUT -> "缺货";
                case CRITICAL -> "严重不足";
                case LOW -> "库存不足";
                case MEDIUM -> "库存一般";
                case HIGH -> "库存充足";
            };}
    }
    public enum ExpiryStatus {
        EXPIRED, NEAR_EXPIRY, NORMAL, UNKNOWN;
        public String label(){
            return switch (this){
                case EXPIRED -> "已过期";
                case NEAR_EXPIRY -> "近效期";
                case NORMAL -> "正常";
                case UNKNOWN -> "未知";
            };}
    }

    public static StockStatus toStockStatus(String code){
        if(code==null) return null;
        try { return StockStatus.valueOf(code); } catch (IllegalArgumentException e){ return null; }
    }
    public static ExpiryStatus toExpiryStatus(String code){
        if(code==null) return ExpiryStatus.UNKNOWN;
        try { return ExpiryStatus.valueOf(code); } catch (IllegalArgumentException e){ return ExpiryStatus.UNKNOWN; }
    }
}

