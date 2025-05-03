package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.DAO.ProduktDAO;
import ppacocha.kasasamoobslugowa.DAO.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.DAO.impl.SQLiteProduktDAO;
import ppacocha.kasasamoobslugowa.DAO.impl.SQLiteTransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.ArrayList;
import java.util.List;

public class KasaService {
    private final ProduktDAO produktDao = new SQLiteProduktDAO();
    private final TransakcjaDAO transakcjaDao = new SQLiteTransakcjaDAO();
    private List<Produkt> koszyk = new ArrayList<>();

    public void dodajProdukt(Produkt produkt) {
        produktDao.save(produkt);
        koszyk.add(produkt);
    }

    public void usunProdukt(Produkt produkt) {
        produktDao.delete(produkt.getKodKreskowy());
        koszyk.remove(produkt);
    }

    public void zmienIlosc(Produkt produkt, int ilosc) {
        koszyk.removeIf(p -> p.equals(produkt));
        for (int i = 0; i < ilosc; i++) {
            koszyk.add(produkt);
        }
        produktDao.update(produkt);
    }

    public Transakcja finalizujTransakcje() {
        Transakcja transakcja = new Transakcja(new ArrayList<>(koszyk));
        int id = transakcjaDao.save(transakcja);
        koszyk.clear();
        return transakcjaDao.findById(id);
    }

    public List<Produkt> getKoszyk() {
        return new ArrayList<>(koszyk);
    }
}
