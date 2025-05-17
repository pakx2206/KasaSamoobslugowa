package ppacocha.kasasamoobslugowa.util;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ReceiptGenerator {
    private static final boolean AUTO_PRINT = false;

    private static final String STORE_NAME    = "Sklep Spożywczy";
    private static final String STORE_ADDRESS = "ul. Handlowa 1, 33-100 Tarnów";
    private static final String TAXPAYER_NIP  = "123-456-32-18";
    private static final String POS_ID        = "KASA-01";
    private static final String CASHIER_ID    = "KASJER-05";
    private static final String FISCAL_UID    = "PL-FISC-00012345";
    private static final String CURRENCY      = "PLN";
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void generateAndSaveReceipt(JFrame parent, Transakcja tx, String currentLanguage) {
        String buyerNip = JOptionPane.showInputDialog(
            parent,
            LanguageSetup.get(currentLanguage, "nip"),
            "Paragon fiskalny",
            JOptionPane.QUESTION_MESSAGE
        );

        List<String> lines = buildReceiptLines(tx, buyerNip);
        BufferedImage img = renderLinesToImage(lines);

        try {
            Path folder = Path.of("paragony");
            if (!Files.exists(folder)) Files.createDirectories(folder);
            Path file = folder.resolve("paragon_" + String.format("%s", tx.getId()) + ".png");
            ImageIO.write(img, "png", file.toFile());

            JOptionPane.showMessageDialog(parent, new JLabel(new ImageIcon(img)), "Paragon fiskalny", JOptionPane.PLAIN_MESSAGE);

            if (AUTO_PRINT) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable((graphics, pageFormat, pageIndex) -> {
                    if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
                    graphics.drawImage(img, 0, 0, null);
                    return Printable.PAGE_EXISTS;
                });
                if (job.printDialog()) job.print();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static List<String> buildReceiptLines(Transakcja tx, String buyerNip) {
        List<String> out = new ArrayList<>();
        out.add("PARAGON FISKALNY");
        out.add(String.format("Nr: %s   %s", tx.getId(), DT_FMT.format(tx.getData())));
        out.add(String.format("Sprzedawca: %s", STORE_NAME));
        out.add(String.format("Adres: %s", STORE_ADDRESS));
        out.add(String.format("NIP sprzedawcy: %s", TAXPAYER_NIP));
        out.add(String.format("Kasjer: %s  POS: %s", CASHIER_ID, POS_ID));
        out.add(repeat('-', 40));
        out.add(String.format("%-20s %3s %7s", "Towar", "Il", "Razem"));
        out.add(repeat('-', 40));

        Map<String, Long> qty = tx.getProdukty().stream()
            .collect(Collectors.groupingBy(Produkt::getNazwa, Collectors.counting()));
        Map<String, BigDecimal> price = tx.getProdukty().stream()
            .collect(Collectors.toMap(Produkt::getNazwa, Produkt::getCena, (a,b)->a));

        BigDecimal total = BigDecimal.ZERO;
        for (var e : qty.entrySet()) {
            String n = e.getKey(); int q = e.getValue().intValue();
            BigDecimal unit = price.get(n);
            BigDecimal sum = unit.multiply(BigDecimal.valueOf(q));
            total = total.add(sum);
            out.add(String.format("%-20.20s %3dx%6.2f %7.2f", n, q, unit, sum));
        }
        out.add(repeat('-', 40));
        out.add(String.format("SUMA BRUTTO:%20.2f %s", total, CURRENCY));
        out.add(repeat('=', 40));

        Map<BigDecimal, BigDecimal> taxSum = new TreeMap<>();
        for (Produkt p : tx.getProdukty())
            taxSum.merge(p.getVatRate(), p.getCena(), BigDecimal::add);
        BigDecimal totalTax = BigDecimal.ZERO;
        for (var tr : taxSum.entrySet()) {
            BigDecimal rate = tr.getKey(); BigDecimal sumBr = tr.getValue();
            BigDecimal base = sumBr.divide(rate.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
            BigDecimal tax = sumBr.subtract(base);
            totalTax = totalTax.add(tax);
            char c = rate.equals(new BigDecimal("0.23"))?'A': rate.equals(new BigDecimal("0.08"))?'B':'D';
            out.add(String.format("PTU %c %5.2f%% %7.2f", c, rate.multiply(BigDecimal.valueOf(100)), tax));
        }
        out.add(String.format("SUMA PTU:%29.2f", totalTax));
        out.add(repeat('=', 40));

        out.add(String.format("ID transakcji: %s", tx.getId()));
        if (buyerNip!=null && !buyerNip.isBlank())
            out.add("NIP nabywcy: " + buyerNip);
        out.add("UID: " + FISCAL_UID);
        out.add("Dziękujemy za zakupy!");
        return out;
    }

    private static BufferedImage renderLinesToImage(List<String> lines) {
        Font regular = new Font(Font.MONOSPACED, Font.PLAIN, 12);
        Font bold    = new Font(Font.MONOSPACED, Font.BOLD, 14);
        int width = 300;
        int lineHeight = regular.getSize() + 8;
        int height = lineHeight * lines.size() + 20;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0,0,width,height);
        g.setColor(Color.BLACK);

        FontMetrics fmReg = g.getFontMetrics(regular);
        FontMetrics fmBold= g.getFontMetrics(bold);
        int y = lineHeight;
        int headerCount = 6;
        int footerThankIndex = lines.size() - 1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            g.setFont(regular);
            FontMetrics fm = fmReg;
            boolean center = false;
            if (i < headerCount) center = true;
            else if (line.startsWith("SUMA BRUTTO")) {
                g.setFont(bold);
                fm = fmBold;
                center = true;
            }
            else if (line.startsWith("PTU ")) {
                String[] parts = line.split(" (?=[^ ]+$)");
                String label = parts[0];
                String value = parts[1];
                g.setFont(regular);
                g.drawString(label, 5, y);
                g.drawString(value, width - 5 - fm.stringWidth(value), y);
                y += lineHeight;
                continue;
            }
            else if (line.startsWith("SUMA PTU")) {
                String[] parts = line.split(" (?=[^ ]+$)");
                String label = parts[0];
                String value = parts[1];
                g.drawString(label, 5, y);
                g.drawString(value, width - 5 - fm.stringWidth(value), y);
                y += lineHeight;
                continue;
            }
            else if (i == footerThankIndex) center = true;

            int strW = fm.stringWidth(line);
            int x = center ? (width - strW) / 2 : 5;
            g.setFont(g.getFont());
            g.drawString(line, x, y);
            y += lineHeight;
        }
        g.dispose();
        return img;
    }

    private static String repeat(char c, int count) {
        char[] arr = new char[count]; Arrays.fill(arr, c);
        return new String(arr);
    }
}
