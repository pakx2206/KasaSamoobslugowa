package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.math.BigDecimal;
import java.util.List;

public class RaportService {
    public String generujRaportDzienny(List<Transakcja> transakcje) {
        BigDecimal suma = transakcje.stream()
            .map(Transakcja::getSuma)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        StringBuilder sb = new StringBuilder();
        sb.append("Raport dzienny:");
        sb.append("Data i czas: ").append(transakcje.get(0).getData()).append("");
        sb.append("------------------------");
        for (Transakcja t : transakcje) {
            sb.append(t).append("");
        }
        sb.append("------------------------");
        sb.append("Suma: ").append(suma).append("");
        return sb.toString();
    }
}