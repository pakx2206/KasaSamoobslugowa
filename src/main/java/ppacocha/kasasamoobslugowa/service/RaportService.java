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

    public static List<Transakcja> getTodaysTransactions() {
        return new MongoTransakcjaDAO().findAll()
                .stream()
                .filter(t -> t.getData().toLocalDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
    }

}
