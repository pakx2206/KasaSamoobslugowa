package ppacocha.kasasamoobslugowa.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

public class ProduktTest {
    @Test
    void testConstructorAndGetters() {
    BigDecimal cena = new BigDecimal("12.50");
    Produkt p = new Produkt("Chleb", cena, "1234567890123", "NFC-TAG-001");

    assertEquals("Chleb", p.getName(),    "Nazwa produktu powinna być ustawiona poprawnie");
    assertEquals(cena, p.getPrice(),       "Cena produktu powinna być ustawiona poprawnie");
    assertEquals("1234567890123", p.getBarCode(), "Kod kreskowy powinien być ustawiony poprawnie");
    assertEquals("NFC-TAG-001", p.getNfcTag(),         "Tag NFC powinien być ustawiony poprawnie");
}
    @Test
    void testEqualsAndHashCode() {
    Produkt p1 = new Produkt("Mleko", new BigDecimal("3.20"), "0001112223334", "TAG-A");
    Produkt p2 = new Produkt("Mleko 2%", new BigDecimal("3.20"), "0001112223334", "TAG-B");
    Produkt p3 = new Produkt("Masło", new BigDecimal("5.00"), "9998887776665", "TAG-C");

    assertEquals(p1, p2, "Produkty z tym samym kodem kreskowym powinny być równe");
    assertEquals(p1.hashCode(), p2.hashCode(), "HashCode powinien być taki sam dla produktów równych");

    assertNotEquals(p1, p3, "Produkty o różnych kodach kreskowych nie są równe");
}


}
