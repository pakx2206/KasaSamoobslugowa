package ppacocha.kasasamoobslugowa.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PDFReportGenerator {

    public static void generateDailyReportPDF(List<Transakcja> allTransactions) throws IOException {
        LocalDate today = LocalDate.now();
        List<Transakcja> todays = allTransactions.stream()
                .filter(t -> t.getData().toLocalDate().equals(today))
                .collect(Collectors.toList());

        Path reportsDir = Paths.get("reports");
        if (Files.notExists(reportsDir)) {
            Files.createDirectories(reportsDir);
        }

        String fileName = "report-" + today.format(DateTimeFormatter.ISO_DATE) + ".pdf";
        Path outFile = reportsDir.resolve(fileName);
        if (Files.exists(outFile)) {
            Files.delete(outFile);
        }

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (InputStream fontStream = new FileInputStream("C:/Windows/Fonts/arialuni.ttf");
                 PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                PDType0Font font = PDType0Font.load(doc, fontStream, true);
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(50, 750);

                cs.showText("Raport dzienny: " + today.format(DateTimeFormatter.ISO_DATE));
                cs.newLineAtOffset(0, -18);
                cs.showText("------------------------------");
                cs.newLineAtOffset(0, -20);

                for (Transakcja t : todays) {
                    cs.showText("Transakcja ID:   " + t.getId());
                    cs.newLineAtOffset(0, -15);
                    cs.showText("Data:            " + t.getData());
                    cs.newLineAtOffset(0, -15);
                    cs.showText("Suma:            " + t.getSuma());
                    cs.newLineAtOffset(0, -15);
                    cs.showText("Typ płatności:   " + t.getTypPlatnosci());
                    cs.newLineAtOffset(0, -20);
                }

                cs.showText("------------------------------");
                cs.newLineAtOffset(0, -18);

                BigDecimal total = todays.stream()
                        .map(Transakcja::getSuma)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                cs.showText("Suma całkowita:   " + total);

                cs.endText();
            }

            doc.save(outFile.toFile());
        }
    }
}
