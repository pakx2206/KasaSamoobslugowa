package ppacocha.kasasamoobslugowa.util;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import javax.swing.*;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

public class ReceiptGenerator {

    public static void generateAndSaveReceipt(JFrame parent, Transakcja transakcja, String paymentType){
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== PARAGON NR ").append(String.format("%05d", transakcja.getId())).append(" ===\n");

        Map<String, Integer> quantities = new HashMap<>();
        Map<String, Produkt> products = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Produkt p : transakcja.getProdukty()) {
            String code = p.getKodKreskowy();
            quantities.put(code, quantities.getOrDefault(code, 0) + 1);
            products.putIfAbsent(code, p);
        }

        for (String code : quantities.keySet()) {
            Produkt p = products.get(code);
            int qty = quantities.get(code);
            BigDecimal lineTotal = p.getCena().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);
            receipt.append(String.format("%s x %d = %.2f PLN\n", p.getNazwa(), qty, lineTotal));
        }
        receipt.append("Płatność: ").append(paymentType).append("\n");
        receipt.append("-----------------------\n");
        receipt.append(String.format("SUMA: %.2f PLN\n", total));
        receipt.append("=======================\n");


        JOptionPane.showMessageDialog(parent, receipt.toString(), "Paragon", JOptionPane.INFORMATION_MESSAGE);

        String folderName = "paragony";
        java.io.File folder = new java.io.File(folderName);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = folderName + "/paragon_" + String.format("%05d", transakcja.getId()) + ".txt";
        try (java.io.PrintWriter writer = new java.io.PrintWriter(fileName, "UTF-8")) {
            writer.print(receipt.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
