package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.dao.impl.SQLiteTransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.List;
import java.math.BigDecimal;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;

public class RaportService {
    private final TransakcjaDAO transakcjaDao = new SQLiteTransakcjaDAO();

    public String generujRaportDzienny() {
        List<Transakcja> transakcje = transakcjaDao.findAll();
        return generujRaportDzienny(transakcje);
    }

    public String generujRaportDzienny(List<Transakcja> transakcje) {
        if (transakcje == null || transakcje.isEmpty()) {
            return "Brak transakcji do raportu dziennego.\n";
        }
        BigDecimal suma = transakcje.stream()
            .map(Transakcja::getSuma)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("Raport dzienny:\n");
        sb.append("Data i czas: ").append(transakcje.get(0).getData()).append("\n");
        sb.append("------------------------\n");

        for (Transakcja t : transakcje) {
            sb.append(t).append("\n");
        }

        sb.append("------------------------\n");
        sb.append("Suma: ").append(suma).append("\n");
        return sb.toString();
    }
}
