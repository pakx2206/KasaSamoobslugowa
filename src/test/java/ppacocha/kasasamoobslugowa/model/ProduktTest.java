package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProduktTest {

    @Test
    void gettersReturnConstructorValues() {
        Produkt p = new Produkt("Name", BigDecimal.valueOf(2.5), "BC", "NFCTAG", true);
        assertEquals("Name", p.getName());
        assertEquals(0, BigDecimal.valueOf(2.5).compareTo(p.getPrice()));
        assertEquals("BC", p.getBarCode());
        assertEquals("NFCTAG", p.getNfcTag());
        assertTrue(p.isRequiresAgeVerification());
        assertEquals(0, p.getQuantity());
        assertEquals(0, BigDecimal.valueOf(0.23).compareTo(p.getVatRate()));
    }

    @Test
    void equalsAndHashCodeByBarCode() {
        Produkt p1 = new Produkt("X", BigDecimal.ONE, "CODE", null, false);
        Produkt p2 = new Produkt("Y", BigDecimal.TEN, "CODE", null, false);
        Produkt p3 = new Produkt("Z", BigDecimal.ONE, "DIFF", null, false);
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    void toStringFormatsNameAndCode() {
        Produkt p = new Produkt("Prod", BigDecimal.ZERO, "B123", null, false);
        assertEquals("Prod (B123)", p.toString());
    }

    @Test
    void quantityFieldAccessible() throws Exception {
        Produkt p = new Produkt("A", BigDecimal.ONE, "BC", "T", false);
        var f = Produkt.class.getDeclaredField("quantity");
        f.setAccessible(true);
        assertEquals(0, f.getInt(p));
    }
}
