package ppacocha.kasasamoobslugowa.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.model.Produkt;
import java.math.BigDecimal;
import java.util.Arrays;

public class ParagonGeneratorTest {
  @Test
  void testGenerujParagon() {
    Transakcja t = new Transakcja(Arrays.asList(new Produkt("Chleb", new BigDecimal("2.50"), "123", "N1")));
    String paragon = ParagonGenerator.generujParagon(t);

    assertTrue(paragon.startsWith("PARAGON"), "Paragon powinien zaczynać się od nagłówka");
    assertTrue(paragon.contains("Chleb"), "Paragon powinien zawierać nazwę produktu");
    assertTrue(paragon.contains("2.50"), "Paragon powinien zawierać cenę produktu");
    assertTrue(paragon.contains("Suma: 2.50"), "Paragon powinien zawierać sumę");
  }

}