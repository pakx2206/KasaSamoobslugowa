package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class LoyaltyTest {

    @Test
    void discountIsStored() {
        Loyalty l = new Loyalty("L1", BigDecimal.valueOf(0.20));
        assertEquals(0, BigDecimal.valueOf(0.20).compareTo(l.getDiscount()));
    }

    @Test
    void idIsStored() throws Exception {
        Loyalty l = new Loyalty("LID", BigDecimal.ZERO);
        var f = Loyalty.class.getDeclaredField("id");
        f.setAccessible(true);
        assertEquals("LID", f.get(l));
    }

}
