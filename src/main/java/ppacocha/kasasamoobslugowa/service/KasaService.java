package ppacocha.kasasamoobslugowa.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ppacocha.kasasamoobslugowa.dao.KoszykDAO;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.dao.impl.*;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.dao.impl.MongoProduktDAO;

public class KasaService {
    private final MongoProduktDAO produktDao = new MongoProduktDAO();
    private final KoszykDAO koszykDao = new MongoKoszykDAO();
    private final TransakcjaDAO transakcjaDao = new MongoTransakcjaDAO();

    public void dodajPoKodzieLubTagu(String raw) {
        String code = raw.trim();
        Produkt p = produktDao.findById(code);
        if (p == null) {
            p = produktDao.findByNfcTag(code);
        }
        if (p == null) {
            p = produktDao.findByNfcTag(code.toUpperCase());
        }
        if (p == null) {
            String digits = code.replaceAll("\\D+", "");
            if (!digits.isEmpty()) {
                p = produktDao.findByNfcTag("NFC" + digits);
            }
        }

        if (p == null) {
            throw new IllegalArgumentException("Produkt o identyfikatorze '" + raw + "' nie istnieje w bazie");
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
        var items = koszykDao.findAll();
        if (items.isEmpty())
            throw new IllegalStateException("Koszyk jest pusty — nie można finalizować transakcji");

        Transakcja tx = new Transakcja(items, typPlatnosci);
        String id = transakcjaDao.save(tx);
        tx.setId(id);

        Map<String, Integer> ilosci = new HashMap<>();
        for (Produkt p : items)
            ilosci.merge(p.getKodKreskowy(), 1, Integer::sum);
        for (var e : ilosci.entrySet())
            produktDao.zmniejszStan(e.getKey(), e.getValue());

        koszykDao.clear();
        return transakcjaDao.findById(id);
    }

    public List<Produkt> szukajPoFragmencieKodu(String fragment) {
        return produktDao.findByPartialCode(fragment);
    }
}
