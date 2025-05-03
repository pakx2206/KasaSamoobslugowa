package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.math.BigDecimal;
import java.util.List;

public class KasaServiceTest {
  private KasaService kasa;

  @BeforeEach
  void setUp() {
    kasa = new KasaService();
  }

  @Test
  void testDodajIUsunProdukt() {
    Produkt p = new Produkt("X", new BigDecimal("1.00"), "333", "TAG3");
    kasa.dodajProdukt(p);
    assertTrue(kasa.getKoszyk().contains(p));

    kasa.usunProdukt(p);
    assertFalse(kasa.getKoszyk().contains(p));
  }

  @Test
  void testZmienIlosc() {
    Produkt p = new Produkt("Y", new BigDecimal("2.00"), "444", "TAG4");
    kasa.dodajProdukt(p);
    kasa.zmienIlosc(p, 3);
    List < Produkt > koszyk = kasa.getKoszyk();
    assertEquals(3, koszyk.size(), "Koszyk powinien zawierać 3 sztuki produktu Y");
  }

  @Test
  void testFinalizujTransakcje() {
    Produkt p = new Produkt("Z", new BigDecimal("5.00"), "555", "TAG5");
    kasa.dodajProdukt(p);
    Transakcja t = kasa.finalizujTransakcje();

    assertEquals(new BigDecimal("5.00"), t.getSuma());
    assertTrue(kasa.getKoszyk().isEmpty(), "Koszyk po finalizacji powinien być pusty");
  }
}