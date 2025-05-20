package ppacocha.kasasamoobslugowa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ppacocha.kasasamoobslugowa.dao.*;
import ppacocha.kasasamoobslugowa.dao.impl.*;
import ppacocha.kasasamoobslugowa.model.Loyalty;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Promotion;
import ppacocha.kasasamoobslugowa.model.Transakcja;

public class KasaService {
    private final PromotionDAO    promotionDao    = new MongoPromotionDAO();
    private final LoyaltyDAO      loyaltyDao      = new MongoLoyaltyDAO();
    private final MongoProduktDAO produktDao      = new MongoProduktDAO();
    private final KoszykDAO       koszykDao       = new MongoKoszykDAO();
    private final TransakcjaDAO   transakcjaDao   = new MongoTransakcjaDAO();

    private String     loyaltyCustomerId = null;
    private BigDecimal loyaltyDiscount   = BigDecimal.ZERO;

    public void dodajPoKodzieLubTagu(String raw) {
        String code = raw.trim();
        Produkt p = produktDao.findById(code);
        if (p == null) p = produktDao.findByNfcTag(code);
        if (p == null) p = produktDao.findByNfcTag(code.toUpperCase());
        if (p == null) {
            String digits = code.replaceAll("\\D+", "");
            if (!digits.isEmpty()) p = produktDao.findByNfcTag("NFC" + digits);
        }
        if (p == null)
            throw new IllegalArgumentException("Produkt o identyfikatorze '" + raw + "' nie istnieje w bazie");
        if (p.getIlosc() <= 0)
            throw new IllegalArgumentException("Produkt '" + p.getNazwa() + "' jest niedostępny w magazynie.");
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

    public List<Produkt> szukajPoFragmencieKodu(String fragment) {
        return produktDao.findByPartialCode(fragment);
    }

    public void applyLoyaltyCard(String phoneOrTag) {
        Loyalty loyalty = loyaltyDao.findByPhoneOrTag(phoneOrTag);
        if (loyalty == null)
            throw new IllegalArgumentException("Nie znaleziono karty lojalnościowej: " + phoneOrTag);
        this.loyaltyCustomerId = loyalty.getId();
        BigDecimal d = loyalty.getDiscount();
        this.loyaltyDiscount = (d != null ? d : BigDecimal.ZERO);
    }

    public String getLoyaltyCustomerId() {
        return loyaltyCustomerId;
    }

    public List<Produkt> getKoszyk() {
        return koszykDao.findAll();
    }

    public Transakcja finalizujTransakcje(String typPlatnosci) {
        List<Produkt> items = koszykDao.findAll();
        if (items.isEmpty())
            throw new IllegalStateException("Koszyk jest pusty — nie można finalizować transakcji");

        BigDecimal total = BigDecimal.ZERO;
        Map<String,Integer> ilosci = new HashMap<>();
        for (Produkt p : items)
            ilosci.merge(p.getKodKreskowy(), 1, Integer::sum);
        for (var e : ilosci.entrySet()) {
            Produkt p = items.stream()
                    .filter(x -> x.getKodKreskowy().equals(e.getKey()))
                    .findFirst().get();
            BigDecimal unit = getPriceWithDiscount(p);
            total = total.add(unit.multiply(BigDecimal.valueOf(e.getValue())));
        }

        Transakcja tx = new Transakcja(items, typPlatnosci);
        tx.setSuma(total);
        String id = transakcjaDao.save(tx);
        tx.setId(id);

        ilosci.forEach((kod, qty) -> produktDao.zmniejszStan(kod, qty));

        koszykDao.clear();
        loyaltyCustomerId = null;
        loyaltyDiscount   = BigDecimal.ZERO;

        return tx;
    }

    public BigDecimal getPriceWithDiscount(Produkt product) {
        BigDecimal price = product.getCena();

        if (loyaltyCustomerId != null) {
            var promo = promotionDao.findByProductCode(product.getKodKreskowy());
            if (promo != null && promo.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
                price = price.multiply(BigDecimal.ONE.subtract(promo.getDiscount()));
            }
            if (loyaltyDiscount.compareTo(BigDecimal.ZERO) > 0) {
                price = price.multiply(BigDecimal.ONE.subtract(loyaltyDiscount));
            }
        }

        return price
                .setScale(2, RoundingMode.HALF_UP);
    }
}
