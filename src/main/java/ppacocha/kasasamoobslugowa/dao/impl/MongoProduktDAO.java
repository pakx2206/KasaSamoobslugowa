package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class MongoProduktDAO implements ProduktDAO {
    private final MongoCollection<Document> coll;

    public MongoProduktDAO() {
        MongoDatabase db = MongoClientProvider.getDatabase();
        coll = db.getCollection("produkt");
    }

    private Produkt mapToProduct(Document d) {
        String kod = d.get("kod_kreskowy").toString();
        String nazwa = d.getString("nazwa");
        boolean requires = d.getBoolean("requiresAgeVerification", false);
        Object priceRaw = d.get("cena");
        BigDecimal cena = priceRaw instanceof Number
                ? BigDecimal.valueOf(((Number) priceRaw).doubleValue())
                : new BigDecimal(priceRaw.toString());

        Object nfcRaw = d.get("nfc_tag");
        String nfc = nfcRaw == null ? null : nfcRaw.toString();

        Object qtyRaw = d.get("ilosc");
        int ilosc = qtyRaw instanceof Number
                ? ((Number) qtyRaw).intValue()
                : d.getInteger("ilosc", 0);

        Object vatRaw = d.get("vat_rate");
        BigDecimal vat = vatRaw instanceof Number
                ? BigDecimal.valueOf(((Number) vatRaw).doubleValue())
                : new BigDecimal(vatRaw.toString());

        return new Produkt(nazwa, cena, kod, nfc, ilosc, vat, requires);
    }

    @Override
    public Produkt findById(String barCode) {
        Document d = coll.find(eq("kod_kreskowy", barCode)).first();
        if (d == null) {
            try {
                long num = Long.parseLong(barCode);
                d = coll.find(eq("kod_kreskowy", num)).first();
            } catch (NumberFormatException ignored) {}
        }
        return d == null ? null : mapToProduct(d);
    }

    @Override
    public Produkt findByNfcTag(String tag) {
        Document d = coll.find(eq("nfc_tag", tag)).first();
        return d == null ? null : mapToProduct(d);
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            out.add(mapToProduct(d));
        }
        return out;
    }

    @Override
    public List<Produkt> findByPartialCode(String fragment) {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            String kod = d.get("kod_kreskowy").toString();
            if (kod.contains(fragment)) {
                out.add(mapToProduct(d));
            }
        }
        return out;
    }

    @Override
    public void reduceStock(String barCode, int amount) {
        Object longValue;
        try {
            longValue = Long.parseLong(barCode);
        } catch (NumberFormatException ex) {
            longValue = barCode;
        }
        coll.updateOne(
                new Document("kod_kreskowy", longValue),
                new Document("$inc", new Document("ilosc", -amount))
        );
    }

    @Override
    public List<Produkt> findByCodeOrNameContaining(String string) {
        List<Produkt> out = new ArrayList<>();
        Bson regexCode = Filters.regex("kod_kreskowy", string, "i");
        Bson regexName = Filters.regex("nazwa", string, "i");
        for (Document d : coll.find(Filters.or(regexCode, regexName))) {
            out.add(mapToProduct(d));
        }
        return out;
    }

}
