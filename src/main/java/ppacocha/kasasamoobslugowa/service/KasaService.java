package ppacocha.kasasamoobslugowa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ppacocha.kasasamoobslugowa.dao.KoszykDAO;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteKoszykDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteProduktDAO;
import ppacocha.kasasamoobslugowa.dao.impl.SQLiteTransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;

public class KasaService {
    private final SQLiteProduktDAO produktDao = new SQLiteProduktDAO();
    private final KoszykDAO koszykDao = new SQLiteKoszykDAO();
    private final TransakcjaDAO transakcjaDao = new SQLiteTransakcjaDAO();

    public void dodajPoKodzieLubTagu(String kodLubTag) {
        Produkt p = produktDao.findById(kodLubTag);
        if (p == null) {
            p = produktDao.findByNfcTag(kodLubTag);
        }
        if (p == null) {
            throw new IllegalArgumentException("Produkt o identyfikatorze '" + kodLubTag + "' nie istnieje w bazie");
        }
        if (p.getIlosc() <= 0) {
            throw new IllegalArgumentException("Produkt '" + p.getNazwa() + "' jest niedostępny w magazynie.");
        }
        koszykDao.addProduct(p);
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

    public Transakcja finalizujTransakcje(String typPlatnosci) {
        List<Produkt> items = koszykDao.findAll();
        if (items.isEmpty()) {
            throw new IllegalStateException("Koszyk jest pusty — nie można finalizować transakcji");
        }

        Transakcja tx = new Transakcja(items, typPlatnosci);
        int id = transakcjaDao.save(tx);
        tx.setId(id);

        zmniejszStanPoTransakcji(items);
        koszykDao.clear();

        return transakcjaDao.findById(id);
    }

    private void zmniejszStanPoTransakcji(List<Produkt> produkty) {
        Map<String, Integer> ilosci = new HashMap<>();
        for (Produkt p : produkty) {
            ilosci.put(p.getKodKreskowy(), ilosci.getOrDefault(p.getKodKreskowy(), 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : ilosci.entrySet()) {
            produktDao.zmniejszStan(entry.getKey(), entry.getValue());
        }
    }

    public List<Produkt> szukajPoFragmencieKodu(String fragment) {
        return produktDao.findByPartialCode(fragment);
    }
}
