package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;

public class TransakcjaTest {
    @Test
    void testObliczSume() {
    Produkt p1 = new Produkt("A", new BigDecimal("2.50"), "111", "TAG1");
    Produkt p2 = new Produkt("B", new BigDecimal("3.75"), "222", "TAG2");
    Transakcja t = new Transakcja(Arrays.asList(p1, p2));

    assertEquals(new BigDecimal("6.25"), t.getSuma(), "Suma cen powinna być poprawnie obliczona");
    assertNotNull(t.getData(), "Data transakcji nie powinna być nullem");
    }

}
