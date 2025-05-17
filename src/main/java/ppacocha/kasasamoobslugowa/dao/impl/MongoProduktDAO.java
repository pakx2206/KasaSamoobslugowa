package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
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

    private Produkt docToProdukt(Document d) {
        String kod = d.get("kod_kreskowy").toString();
        String nazwa = d.getString("nazwa");

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

        return new Produkt(nazwa, cena, kod, nfc, ilosc, vat);
    }

    @Override
    public Produkt findById(String kod) {
        Document d = coll.find(eq("kod_kreskowy", kod)).first();
        if (d == null) {
            try {
                long num = Long.parseLong(kod);
                d = coll.find(eq("kod_kreskowy", num)).first();
            } catch (NumberFormatException ignored) {}
        }
        return d == null ? null : docToProdukt(d);
    }

    @Override
    public Produkt findByNfcTag(String tag) {
        Document d = coll.find(eq("nfc_tag", tag)).first();
        return d == null ? null : docToProdukt(d);
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            out.add(docToProdukt(d));
        }
        return out;
    }

    @Override
    public void save(Produkt p) {
        Document d = new Document()
                .append("kod_kreskowy", p.getKodKreskowy())
                .append("nazwa",        p.getNazwa())
                .append("cena",         p.getCena().doubleValue())
                .append("nfc_tag",      p.getNfcTag())
                .append("ilosc",        p.getIlosc())
                .append("vat_rate",     p.getVatRate().doubleValue());
        coll.insertOne(d);
    }

    @Override
    public void update(Produkt p) {
        coll.updateOne(eq("kod_kreskowy", p.getKodKreskowy()),
                new Document("$set", new Document()
                        .append("nazwa",    p.getNazwa())
                        .append("cena",     p.getCena().doubleValue())
                        .append("nfc_tag",  p.getNfcTag())
                        .append("ilosc",    p.getIlosc())
                        .append("vat_rate", p.getVatRate().doubleValue())
                )
        );
    }

    @Override
    public void delete(String kod) {
        coll.deleteOne(eq("kod_kreskowy", kod));
    }

    @Override
    public List<Produkt> findByPartialCode(String fragment) {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            String kod = d.get("kod_kreskowy").toString();
            if (kod.contains(fragment)) {
                out.add(docToProdukt(d));
            }
        }
        return out;
    }

    @Override
    public void zmniejszStan(String kod, int amount) {
        Object filterVal;
        try {
            filterVal = Long.parseLong(kod);
        } catch (NumberFormatException ex) {
            filterVal = kod;
        }
        coll.updateOne(
                new Document("kod_kreskowy", filterVal),
                new Document("$inc", new Document("ilosc", -amount))
        );
    }
}
