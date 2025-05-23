package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

public class MongoTransakcjaDAO implements TransakcjaDAO {
    private final MongoCollection<Document> coll;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public MongoTransakcjaDAO() {
        MongoDatabase db = MongoClientProvider.getDatabase();
        coll = db.getCollection("transakcja");
    }

    @Override
    public String save(Transakcja tx) {
        List<Document> items = tx.getProdukty().stream()
                .collect(Collectors.groupingBy(Produkt::getKodKreskowy, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new Document()
                        .append("kod_kreskowy", e.getKey())
                        .append("ilosc", e.getValue().intValue())
                        .append("cena_jednostkowa",
                                tx.getProdukty().stream()
                                        .filter(p -> p.getKodKreskowy().equals(e.getKey()))
                                        .findFirst().get()
                                        .getCena().doubleValue()
                        )
                )
                .collect(Collectors.toList());

        Document doc = new Document()
                .append("data",     FMT.format(tx.getData()))
                .append("suma",     tx.getSuma().doubleValue())
                .append("typ_platnosci", tx.getTypPlatnosci())
                .append("produkty", items);

        InsertOneResult res = coll.insertOne(doc);
        ObjectId oid = res.getInsertedId().asObjectId().getValue();
        return oid.toHexString();
    }

    @Override
    public Transakcja findById(String id) {
        Document d = coll.find(eq("_id", new ObjectId(id))).first();
        if (d == null) return null;

        List<Produkt> products = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Document> items = (List<Document>) d.get("produkty");
        for (Document it : items) {
            String name     = it.getString("nazwa");
            String code     = it.getString("kod_kreskowy");
            String nfcTag   = it.getString("nfc_tag");
            int qty         = it.getInteger("ilosc", 1);
            Number unitNum = it.get("cena_jednostkowa", Number.class);
            BigDecimal unit = (unitNum != null) ? new BigDecimal(unitNum.toString()) : BigDecimal.ZERO;
            Number vatNum = it.get("vat_rate", Number.class);
            BigDecimal vat = (vatNum != null) ? new BigDecimal(vatNum.toString()) : BigDecimal.ZERO;
            boolean requiresAge = it.getBoolean("requiresAgeVerification", false);

            for (int i = 0; i < qty; i++) {
                products.add(new Produkt(
                        name,
                        unit,
                        code,
                        nfcTag,
                        1,
                        vat,
                        requiresAge
                ));
            }
        }

        Transakcja tx = new Transakcja(products, d.getString("typ_platnosci"));
        tx.setId(id);
        tx.setData(LocalDateTime.parse(d.getString("data")));
        Number sumValue = d.get("suma", Number.class);
        BigDecimal suma = (sumValue != null) ? new BigDecimal(sumValue.toString()) : BigDecimal.ZERO;
        tx.setSuma(suma);
        return tx;
    }


    @Override
    public List<Transakcja> findAll() {
        List<Transakcja> out = new ArrayList<>();
        for (Document d : coll.find()) {
            String hexId = d.getObjectId("_id").toHexString();
            out.add(findById(hexId));
        }
        return out;
    }

    @Override
    public void update(Transakcja transakcja) {}

    @Override
    public void delete(String id) {
        coll.deleteOne(eq("_id", new ObjectId(id)));
    }
}
