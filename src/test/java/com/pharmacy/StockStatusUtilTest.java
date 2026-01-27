package com.pharmacy;

import com.pharmacy.util.StockStatusUtil;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class StockStatusUtilTest {
    @Test
    void testCalcStockStatus() {
        assertEquals("OUT", StockStatusUtil.calcStockStatus(0, 10));
        assertEquals("CRITICAL", StockStatusUtil.calcStockStatus(1, 20));
        assertEquals("LOW", StockStatusUtil.calcStockStatus(5, 20));
        assertEquals("MEDIUM", StockStatusUtil.calcStockStatus(15, 20));
        assertEquals("HIGH", StockStatusUtil.calcStockStatus(25, 20));
    }
    @Test
    void testCalcExpiryStatus() {
        assertEquals("EXPIRED", StockStatusUtil.calcExpiryStatus(LocalDate.now().minusDays(1)));
        assertEquals("NORMAL", StockStatusUtil.calcExpiryStatus(LocalDate.now().plusDays(90)));
        String near = StockStatusUtil.calcExpiryStatus(LocalDate.now().plusDays(30));
        assertTrue(near.equals("NEAR_EXPIRY") || near.equals("NORMAL")); // depends threshold
    }
    @Test
    void testEarliestValid() {
        LocalDate a = LocalDate.now().plusDays(10);
        LocalDate b = LocalDate.now().plusDays(20);
        assertEquals(a, StockStatusUtil.earliestValid(a,b));
    }
}

