package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class TransakcjaTest {

    @Test
    void testCalculateSumDlaDwochProduktow() {
        Produkt p1 = new Produkt("A", new BigDecimal("2.50"), "111", "TAG1");
        Produkt p2 = new Produkt("B", new BigDecimal("3.75"), "222", "TAG2");
        Transakcja t = new Transakcja(Arrays.asList(p1, p2), "CASH");

        assertEquals(new BigDecimal("6.25"), t.getSuma(), "Suma powinna być 2.50 + 3.75 = 6.25");
    }

    @Test
    void testCalculateSumDlaPustejListy() {
        Transakcja t = new Transakcja(Collections.emptyList(), "CARD");
        assertEquals(BigDecimal.ZERO, t.getSuma(), "Dla pustej listy suma powinna być 0");
    }

    @Test
    void testGetTypPlatnosci() {
        Transakcja t = new Transakcja(Collections.emptyList(), "KARTA");
        assertEquals("KARTA", t.getTypeOfPayment(), "Typ płatności powinien się zgadzać");
    }

    @Test
    void testGetDataNieJestNull() {
        Transakcja t = new Transakcja(Collections.emptyList(), "CASH");
        assertNotNull(t.getData(), "Data transakcji nie może być null");
        assertTrue(t.getData().isBefore(LocalDateTime.now().plusSeconds(1)),
                   "Data transakcji powinna być ustawiona na teraz");
    }

    @Test
    void testToStringZawieraKluczoweInformacje() {
        Produkt p = new Produkt("X", new BigDecimal("1.00"), "999", "TAGX");
        Transakcja t = new Transakcja(Arrays.asList(p), "CASH");
        String s = t.toString();

        assertTrue(s.contains("id=" + t.getId()),       "toString powinien zawierać id");
        assertTrue(s.contains("suma=" + t.getSuma()),    "toString powinien zawierać sumę");
        assertTrue(s.contains("typPlatnosci='CASH'"),    "toString powinien zawierać typ płatności");
        assertTrue(s.contains("iloscProd=1"),            "toString powinien zawierać liczbę produktów");
    }
}
