package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.util.List;
import ppacocha.kasasamoobslugowa.dao.KoszykDAO;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteKoszykDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteProduktDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteTransakcjaDAO;

public class KasaService {
    private final ProduktDAO produktDao       = new SQLiteProduktDAO();
    private final KoszykDAO koszykDao         = new SQLiteKoszykDAO();
    private final TransakcjaDAO transakcjaDao = new SQLiteTransakcjaDAO();

    public void dodajPoKodzieLubTagu(String kodLubTag) {
        Produkt p = produktDao.findById(kodLubTag);
        if (p == null) {
            p = produktDao.findByNfcTag(kodLubTag);
        }
        if (p == null) {
            throw new IllegalArgumentException(
                "Produkt o identyfikatorze '" + kodLubTag + "' nie istnieje w bazie"
            );
        }
        koszykDao.add(p.getKodKreskowy(), 1);
    }

    public void usunPoKodzie(String kodKreskowy) {
        koszykDao.delete(kodKreskowy);
    }

    public void zmienIloscPoKodzie(String kodKreskowy, int ilosc) {
        if (ilosc <= 0) {
            koszykDao.delete(kodKreskowy);
        } else {
            koszykDao.update(kodKreskowy, ilosc);
        }
    }

    public List<Produkt> getKoszyk() {
        return koszykDao.findAll();
    }

    public Transakcja finalizujTransakcje() {
        List<Produkt> items = koszykDao.findAll();
        if (items.isEmpty()) {
            throw new IllegalStateException("Koszyk jest pusty — nie można finalizować transakcji");
        }
        Transakcja tx = new Transakcja(items);
        int id = transakcjaDao.save(tx);
        koszykDao.clear();
        return transakcjaDao.findById(id);
    }
}
