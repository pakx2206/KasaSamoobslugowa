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
        // Użyjmy kolekcji "transakcja" (tak, jak w Twojej bazie)
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
        // zwróć hex String ObjectId
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
            String code = it.getString("kod_kreskowy");
            int qty     = it.getInteger("ilosc", 0);
            BigDecimal price = BigDecimal.valueOf(it.getDouble("cena_jednostkowa"));
            for (int i = 0; i < qty; i++) {
                products.add(new Produkt(code, price, code, null, 0, BigDecimal.ZERO));
            }
        }

        Transakcja tx = new Transakcja(products, d.getString("typ_platnosci"));
        tx.setId(id);
        tx.setData(java.time.LocalDateTime.parse(d.getString("data")));
        tx.setSuma(BigDecimal.valueOf(d.getDouble("suma")));
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
    public void update(Transakcja transakcja) { /* opcjonalnie */ }

    @Override
    public void delete(String id) {
        coll.deleteOne(eq("_id", new ObjectId(id)));
    }
}
