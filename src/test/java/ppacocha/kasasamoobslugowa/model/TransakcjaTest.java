package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransakcjaTest {

    @Test
    void sumCalculatedFromProducts() {
        Produkt p1 = new Produkt("A", BigDecimal.valueOf(10), "B1", "T1", false);
        Produkt p2 = new Produkt("B", BigDecimal.valueOf(5.50), "B2", "T2", false);
        Transakcja tx = new Transakcja(List.of(p1, p2), "Cash");
        assertEquals(0, BigDecimal.valueOf(15.50).compareTo(tx.getSuma()));
    }

    @Test
    void idSetterGetter() {
        Transakcja tx = new Transakcja(List.of(), "Card");
        tx.setId("XYZ");
        assertEquals("XYZ", tx.getId());
    }

    @Test
    void typeOfPaymentIsStored() {
        Transakcja tx = new Transakcja(List.of(), "Online");
        assertEquals("Online", tx.getTypeOfPayment());
    }

    @Test
    void nipSetterGetter() {
        Transakcja tx = new Transakcja(List.of(), "Cash");
        tx.setNip("1234567890");
        assertEquals("1234567890", tx.getNip());
    }

    @Test
    void toStringContainsKeyFields() {
        Produkt p = new Produkt("X", BigDecimal.ONE, "C", "T", false);
        Transakcja tx = new Transakcja(List.of(p), "Cash");
        tx.setId("ID1");
        tx.setNip("NIP");
        String s = tx.toString();
        assertTrue(s.contains("ID1"));
        assertTrue(s.contains("suma="));
        assertTrue(s.contains("typPlatnosci='Cash'"));
        assertTrue(s.contains("nip='NIP'"));
        assertTrue(s.contains("iloscProd=1"));
    }
    @Test
    void basicPropertiesAndMutators() {
        Produkt p = new Produkt("Prod", BigDecimal.ONE, "C1", null, 1, BigDecimal.ZERO, false);
        Transakcja tx = new Transakcja(List.of(p,p), "cash");
        assertEquals("cash", tx.getTypeOfPayment());
        tx.setId("ABC");
        assertEquals("ABC", tx.getId());
        LocalDateTime now = LocalDateTime.now();
        tx.setData(now);
        assertEquals(now, tx.getData());
        tx.setSuma(BigDecimal.valueOf(9.99));
        assertEquals(BigDecimal.valueOf(9.99), tx.getSuma());
    }
}
