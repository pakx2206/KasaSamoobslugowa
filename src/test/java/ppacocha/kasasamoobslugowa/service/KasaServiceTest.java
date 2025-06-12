package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.model.Produkt;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class KasaServiceTest {

    @Test
    void testVerifyAgeFlag() {
        KasaService ks = new KasaService();
        assertFalse(ks.isAgeVerified());
        ks.verifyAge();
        assertTrue(ks.isAgeVerified());
    }

    @Test
    void testGetPriceWithDiscountNoPromoOrLoyalty() {
        KasaService ks = new KasaService();
        Produkt p = new Produkt("Test", BigDecimal.valueOf(19.99), "X", null, false);
        BigDecimal price = ks.getPriceWithDiscount(p);
        assertEquals(0, price.compareTo(BigDecimal.valueOf(19.99)));
    }

    @Test
    void testFinalizeTransactionEmptyCartThrows() {
        KasaService ks = new KasaService();
        ks.resetSession();
        assertThrows(IllegalStateException.class, () -> ks.finalizeTransaction("Cash"));
    }

    @Test
    void testAddByCodeOrTagUnknownThrows() {
        KasaService ks = new KasaService();
        assertThrows(IllegalArgumentException.class, () -> ks.addByCodeOrTag("UNKNOWN_CODE"));
    }
}
