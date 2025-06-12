package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.dao.impl.MongoTransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RaportService {
    private final TransakcjaDAO transakcjaDao = new MongoTransakcjaDAO();

    public String generateDailyReport() {
        LocalDate today = LocalDate.now();
        List<Transakcja> wszystkie = transakcjaDao.findAll();
        List<Transakcja> dzisiejsze = wszystkie.stream()
                .filter(tx -> tx.getData().toLocalDate().equals(today))
                .collect(Collectors.toList());
        return generateDailyReport(dzisiejsze);
    }

    public String generateDailyReport(List<Transakcja> transakcje) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        sb.append("Raport dzienny: ").append(today).append("\n");
        sb.append("================================\n\n");

        if (transakcje.isEmpty()) {
            sb.append("Brak transakcji dla dzisiejszego dnia.\n");
            return sb.toString();
        }

        BigDecimal sumAll = BigDecimal.ZERO;
        for (Transakcja tx : transakcje) {
            sb.append("Transakcja\n");
            sb.append("  ID:\t\t").append(tx.getId()).append("\n");
            sb.append("  Data:\t\t").append(tx.getData()).append("\n");
            sb.append("  Typ płatności:\t").append(tx.getTypeOfPayment()).append("\n");
            sb.append("  Produkty:\n");

            Map<String, Long> counts = tx.getProduct().stream()
                    .collect(Collectors.groupingBy(Produkt::getBarCode, Collectors.counting()));

            for (Map.Entry<String, Long> e : counts.entrySet()) {
                Produkt p = tx.getProduct().stream()
                        .filter(q -> q.getBarCode().equals(e.getKey()))
                        .findFirst().get();

                sb.append("    • ")
                        .append(p.getName())
                        .append(" (").append(p.getBarCode()).append(")")
                        .append(" x").append(e.getValue())
                        .append(" @ ").append(p.getPrice())
                        .append("\n");
            }

            sb.append("  Suma transakcji:\t").append(tx.getSuma()).append("\n");
            sb.append("--------------------------------\n\n");

            sumAll = sumAll.add(tx.getSuma());
        }

        sb.append("SUMA WSZYSTKICH TRANSAKCJI:\t").append(sumAll).append("\n");
        return sb.toString();
    }
    public List<Transakcja> getTodaysTransactions() {
        return transakcjaDao.findAll()
                .stream()
                .filter(t -> t.getData().toLocalDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
    }

}
