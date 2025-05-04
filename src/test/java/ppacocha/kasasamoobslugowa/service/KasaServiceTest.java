package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.service.KasaService; 
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
                       
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteProduktDAO;             

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;

public class KasaServiceTest {
    private KasaService kasa;

    @BeforeEach
    void setUp() throws Exception {
        Files.deleteIfExists(Paths.get("kasa.db"));

        String sql = Files.readString(
            Paths.get("src/main/resources/schema.sql"),
            StandardCharsets.UTF_8
        );
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:kasa.db");
             Statement st  = conn.createStatement()) {
            st.executeUpdate(sql);
        }

        ProduktDAO dao = new SQLiteProduktDAO();
        dao.save(new Produkt("X", new BigDecimal("1.00"), "333", "TAG3"));
        dao.save(new Produkt("Y", new BigDecimal("2.00"), "444", "TAG4"));
        dao.save(new Produkt("Z", new BigDecimal("5.00"), "555", "TAG5"));

        kasa = new KasaService();
    }

    @Test
    void testDodajIUsunProdukt() {
        kasa.dodajPoKodzieLubTagu("333");
        List<Produkt> koszyk1 = kasa.getKoszyk();
        assertTrue(
            koszyk1.stream().anyMatch(p -> p.getKodKreskowy().equals("333")),
            "Koszyk powinien zawierać produkt o kodzie 333"
        );

        kasa.usunPoKodzie("333");
        List<Produkt> koszyk2 = kasa.getKoszyk();
        assertFalse(
            koszyk2.stream().anyMatch(p -> p.getKodKreskowy().equals("333")),
            "Koszyk nie powinien zawierać produktu o kodzie 333 po usunięciu"
        );
    }

    @Test
    void testZmienIlosc() {
        kasa.dodajPoKodzieLubTagu("444");
        kasa.zmienIloscPoKodzie("444", 3);
        List<Produkt> koszyk = kasa.getKoszyk();
        assertEquals(
            3, koszyk.size(),
            "Koszyk powinien zawierać 3 sztuki produktu o kodzie 444"
        );
    }

    @Test
    void testFinalizujTransakcje() {
        kasa.dodajPoKodzieLubTagu("555");
        Transakcja t = kasa.finalizujTransakcje();
        assertEquals(
            new BigDecimal("5.00"),
            t.getSuma(),
            "Suma transakcji powinna być równa cenie jednego produktu Z"
        );
        assertTrue(
            kasa.getKoszyk().isEmpty(),
            "Po finalizacji koszyk powinien być pusty"
        );
    }
}
