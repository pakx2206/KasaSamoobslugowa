package ppacocha.kasasamoobslugowa.util;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.time.format.DateTimeFormatter;

public class ParagonGenerator {
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String generujParagon(Transakcja transakcja) {
        StringBuilder sb = new StringBuilder();
        sb.append("PARAGON");
        sb.append("Data: ")
          .append(transakcja.getData().format(FORMATTER))
          .append("");
        sb.append("------------------------");
        transakcja.getProdukty().forEach(p ->
            sb.append(p.getNazwa())
              .append(" ")
              .append(p.getCena())
              .append("")
        );
        sb.append("------------------------");
        sb.append("Suma: ")
          .append(transakcja.getSuma())
          .append("");
        return sb.toString();
    }
}