package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.math.BigDecimal;
import java.util.Arrays;
import ppacocha.kasasamoobslugowa.model.Produkt;

public class ReportServiceTest {
  @Test
  void testGenerujRaportDzienny() {
    Transakcja t1 = new Transakcja(Arrays.asList(new Produkt("A", new BigDecimal("1.00"), "111", "T1")));
    Transakcja t2 = new Transakcja(Arrays.asList(new Produkt("B", new BigDecimal("2.00"), "222", "T2")));
    RaportService rs = new RaportService();

    String raport = rs.generujRaportDzienny(Arrays.asList(t1, t2));
    assertTrue(raport.contains("Suma: 3.00"), "Raport powinien zawierać sumę 3.00");
    assertTrue(raport.contains("Transakcja"), "Raport powinien zawierać wpisy transakcji");
  }

}