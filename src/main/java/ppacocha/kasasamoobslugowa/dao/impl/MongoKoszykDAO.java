package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ppacocha.kasasamoobslugowa.dao.KoszykDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;

public class MongoKoszykDAO implements KoszykDAO {
    private final MongoCollection<Document> coll;
    private final MongoProduktDAO produktDao = new MongoProduktDAO();

    public MongoKoszykDAO() {
        MongoDatabase db = MongoClientProvider.getDatabase();
        coll = db.getCollection("koszyk");
    }

    @Override
    public void add(String kodKreskowy, int amount) {
        coll.updateOne(eq("kod_kreskowy", kodKreskowy),
                inc("ilosc", amount),
                new com.mongodb.client.model.UpdateOptions().upsert(true)
        );
    }

    @Override
    public void update(String kodKreskowy, int amount) {
        coll.updateOne(eq("kod_kreskowy", kodKreskowy),
                new Document("$set", new Document("ilosc", amount))
        );
    }

    @Override
    public void delete(String kodKreskowy) {
        coll.deleteOne(eq("kod_kreskowy", kodKreskowy));
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            String code = d.get("kod_kreskowy").toString();
            int qty = d.getInteger("ilosc", 0);
            Produkt p = produktDao.findById(code);
            if (p == null) {
                // wpis w koszyku nie ma odpowiadającego produktu → usuń go i pomiń
                coll.deleteOne(eq("kod_kreskowy", code));
                continue;
            }
            for (int i = 0; i < qty; i++) {
                out.add(p);
            }
        }
        return out;
    }

    @Override
    public void clear() {
        coll.deleteMany(new Document());
    }

    @Override
    public void addProduct(Produkt produkt) {
        add(produkt.getKodKreskowy(), 1);
    }
}
