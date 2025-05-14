package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaportServiceTest {

    @Test
    void testGenerujRaportDziennyZListyTransakcji() {
        Produkt p1 = new Produkt("A", new BigDecimal("1.00"), "111", "T1");
        Produkt p2 = new Produkt("B", new BigDecimal("2.00"), "222", "T2");
        Transakcja t1 = new Transakcja(Arrays.asList(p1), "CASH");
        Transakcja t2 = new Transakcja(Arrays.asList(p2), "CARD");
        List<Transakcja> lista = Arrays.asList(t1, t2);

        RaportService rs = new RaportService();
        String raport = rs.generujRaportDzienny(lista);

        assertTrue(raport.startsWith("Raport dzienny"), "Raport powinien zaczynać się od nagłówka");
        assertTrue(raport.contains(p1.toString()), "Raport powinien zawierać opis pierwszej transakcji");
        assertTrue(raport.contains(p2.toString()), "Raport powinien zawierać opis drugiej transakcji");
        assertTrue(raport.contains("Suma: 3.00"), "Raport powinien zawierać sumę 1.00 + 2.00 = 3.00");
    }

    @Test
    void testGenerujRaportDziennyBezParametrow() {
        RaportService rs = new RaportService() {
            @Override
            public String generujRaportDzienny() {
                return generujRaportDzienny(Arrays.asList(
                    new Transakcja(Arrays.asList(new Produkt("X", new BigDecimal("5.00"), "777", "T7")), "CASH")
                ));
            }
        };
        String r = rs.generujRaportDzienny();
        assertTrue(r.contains("Suma: 5.00"), "Raport bezparametrowy powinien zawierać sumę 5.00");
    }
}
