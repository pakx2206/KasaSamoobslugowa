package ppacocha.kasasamoobslugowa.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.*;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFReportGeneratorTest {
    private static final String REPORTS_DIR = "reports";
    private static final String DATE = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    private static final Path OUTPUT = Path.of(REPORTS_DIR, "report-" + DATE + ".pdf");

    @BeforeEach
    void cleanUp() throws IOException {
        Files.createDirectories(Path.of(REPORTS_DIR));
        if (Files.exists(OUTPUT)) {
            Files.delete(OUTPUT);
        }
    }

    @AfterAll
    static void tearDown() throws IOException {
        File dir = new File(REPORTS_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".pdf"));
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    @Test
    void generateDailyReportPDF_emptyList_createsPdf() throws IOException {
        PDFReportGenerator.generateDailyReportPDF(Collections.emptyList());
        assertTrue(Files.exists(OUTPUT));
        try (PDDocument doc = PDDocument.load(OUTPUT.toFile())) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }

    @Test
    void generateDailyReportPDF_withOneTransaction_containsText() throws IOException {
        Transakcja tx = new Transakcja(List.of(), "Cash");
        tx.setId("TX1");
        tx.setSuma(BigDecimal.valueOf(42));
        tx.setData(LocalDateTime.now());
        PDFReportGenerator.generateDailyReportPDF(List.of(tx));
        assertTrue(Files.exists(OUTPUT));
        try (PDDocument doc = PDDocument.load(OUTPUT.toFile())) {
            assertEquals(1, doc.getNumberOfPages());
        }
    }
}
