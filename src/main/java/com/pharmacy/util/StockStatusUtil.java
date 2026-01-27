package com.pharmacy.util;

import java.time.LocalDate;
import java.util.Objects;

/**
 * 统一的库存/有效期状态计算工具。
 */
public final class StockStatusUtil {
    private StockStatusUtil() {}

    // 阈值可后续改成读取设置表
    private static final double CRITICAL_RATIO = 0.10; // <=10%
    private static final double LOW_RATIO = 0.30;      // <=30%
    private static final double MEDIUM_RATIO = 0.80;   // <=80%
    private static final int NEAR_EXPIRY_DAYS = 60;    // 近效期天数

    public static String calcStockStatus(Integer currentStock, Integer safetyStock) {
        int cs = currentStock == null ? 0 : currentStock;
        int ss = safetyStock == null || safetyStock <= 0 ? 1 : safetyStock; // 避免除0
        if (cs == 0) return "OUT";
        double ratio = cs * 1.0 / ss;
        if (ratio <= CRITICAL_RATIO) return "CRITICAL";
        if (ratio <= LOW_RATIO) return "LOW";
        if (ratio <= MEDIUM_RATIO) return "MEDIUM";
        return "HIGH";
    }

    public static String calcExpiryStatus(LocalDate expiryDate) {
        if (expiryDate == null) return null;
        LocalDate today = LocalDate.now();
        if (expiryDate.isBefore(today)) return "EXPIRED";
        if (!expiryDate.isBefore(today.plusDays(NEAR_EXPIRY_DAYS))) return "NORMAL";
        return "NEAR_EXPIRY";
    }

    /** 最早未过期日期（如果传入多个批次的日期） */
    public static LocalDate earliestValid(LocalDate a, LocalDate b) {
        if (a == null) return b;
        if (b == null) return a;
        boolean aValid = !a.isBefore(LocalDate.now());
        boolean bValid = !b.isBefore(LocalDate.now());
        if (aValid && bValid) {
            return a.isBefore(b) ? a : b;
        }
        if (aValid) return a;
        if (bValid) return b;
        // 都过期，返回较早的用于展示“最早过期”
        return a.isBefore(b) ? a : b;
    }

    public static LocalDate mergeEarliest(LocalDate currentEarliest, LocalDate candidate) {
        if (candidate == null) return currentEarliest;
        if (currentEarliest == null) return candidate;
        if (candidate.isBefore(currentEarliest)) return candidate;
        return currentEarliest;
    }

    public static StockEnums.StockStatus calcStockStatusEnum(Integer currentStock, Integer safetyStock){
        return StockEnums.toStockStatus(calcStockStatus(currentStock, safetyStock));
    }
    public static StockEnums.ExpiryStatus calcExpiryStatusEnum(java.time.LocalDate expiryDate){
        return StockEnums.toExpiryStatus(calcExpiryStatus(expiryDate));
    }
}
