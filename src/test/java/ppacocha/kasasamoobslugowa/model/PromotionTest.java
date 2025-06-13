package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PromotionTest {

    @Test
    void discountIsStored() {
        Promotion p = new Promotion("X", BigDecimal.valueOf(0.15));
        assertEquals(0, BigDecimal.valueOf(0.15).compareTo(p.getDiscount()));
    }

    @Test
    void productCodeIsStored() throws Exception {
        Promotion p = new Promotion("CODE123", BigDecimal.ZERO);
        Field f = Promotion.class.getDeclaredField("productCode");
        f.setAccessible(true);
        assertEquals("CODE123", f.get(p));
    }

}
