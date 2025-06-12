package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import ppacocha.kasasamoobslugowa.model.Produkt;

import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KasaServiceTest {

    @TempDir
    Path tempDir;

    private KasaService kasa;

    @BeforeEach
    void setUp() throws Exception {
        Path testDb = tempDir.resolve("kasa.db");
        String url = "jdbc:sqlite:" + testDb.toAbsolutePath();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("PRAGMA foreign_keys = ON;");

            stmt.executeUpdate("DROP TABLE IF EXISTS produkt;");
            stmt.executeUpdate("""
                CREATE TABLE produkt (
                  kod_kreskowy TEXT PRIMARY KEY,
                  nazwa        TEXT NOT NULL,
                  cena         REAL NOT NULL,
                  nfc_tag      TEXT,
                  ilosc        INTEGER NOT NULL DEFAULT 0,
                  vat_rate     REAL    NOT NULL DEFAULT 0.23
                );
            """);
            stmt.executeUpdate("""
                INSERT INTO produkt(kod_kreskowy, nazwa, cena, nfc_tag, ilosc, vat_rate)
                VALUES('P1','TestProd',5.00,'TAG1',5,0.10);
            """);

            stmt.executeUpdate("DROP TABLE IF EXISTS koszyk;");
            stmt.executeUpdate("""
                CREATE TABLE koszyk (
                  kod_kreskowy TEXT PRIMARY KEY,
                  ilosc        INTEGER NOT NULL
                );
            """);

            stmt.executeUpdate("DROP TABLE IF EXISTS transakcja_produkt;");
            stmt.executeUpdate("DROP TABLE IF EXISTS transakcja;");

            stmt.executeUpdate("""
                CREATE TABLE transakcja (
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  suma REAL,
                  typ_platnosci TEXT,
                  data TEXT
                );
            """);

            stmt.executeUpdate("""
                CREATE TABLE transakcja_produkt (
                  transakcja_id     INTEGER,
                  kod_kreskowy      TEXT,
                  ilosc             INTEGER NOT NULL,
                  cena_jednostkowa  REAL NOT NULL
                );
            """);
        }

        Files.copy(testDb, Paths.get("kasa.db"), StandardCopyOption.REPLACE_EXISTING);
        kasa = new KasaService();
    }

    @Test
    void testAddByCodeOrTagNieistniejacyProdukt() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> kasa.addByCodeOrTag("UNKNOWN")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("nie istnieje"));
    }

    @Test
    void testAddByCodeOrTagProduktDostepny() {
        kasa.addByCodeOrTag("P1");
        List<Produkt> koszyk = kasa.getKoszyk();
        assertFalse(koszyk.isEmpty());
        assertEquals("P1", koszyk.get(0).getBarCode());
    }

    @Test
    void testDeleteByCode() {
        kasa.addByCodeOrTag("P1");
        kasa.deleteByCode("P1");
        assertTrue(kasa.getKoszyk().isEmpty());
    }

    @Test
    void testChangeQuantityByCodeNaZeroKasuje() {
        kasa.addByCodeOrTag("P1");
        kasa.changeQuantityByCode("P1", 0);
        assertTrue(kasa.getKoszyk().isEmpty());
    }

    @Test
    void testChangeQuantityByCodeNaWiecej() {
        kasa.addByCodeOrTag("P1");
        kasa.addByCodeOrTag("P1");
        kasa.changeQuantityByCode("P1", 3);
        long count = kasa.getKoszyk().stream()
                         .filter(p -> p.getBarCode().equals("P1"))
                         .count();
        assertEquals(3, count);
    }

    @Test
    void testFinalizeTransactionPustyKoszyk() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> kasa.finalizeTransaction("CASH")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("koszyk jest pusty"));
    }

    @Test
    void testFinalizeTransactionPoprawnie() {
        kasa.addByCodeOrTag("P1");
        var tx = kasa.finalizeTransaction("CARD");
        assertNotNull(tx, "Transakcja nie może być null");
        assertEquals(0, tx.getSuma().compareTo(new BigDecimal("5.00")), "Suma transakcji powinna być 5.00");
        assertTrue(kasa.getKoszyk().isEmpty());
        assertEquals("CARD", tx.getTypeOfPayment());
    }

    @Test
    void testFindByPartialCode() {
        var wyn = kasa.findByPartialCode("P");
        assertEquals(1, wyn.size());
        assertEquals("P1", wyn.get(0).getBarCode());
    }
}
