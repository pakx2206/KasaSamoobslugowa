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
    public void add(String barCode, int amount) {
        coll.updateOne(eq("kod_kreskowy", barCode),
                inc("ilosc", amount),
                new com.mongodb.client.model.UpdateOptions().upsert(true)
        );
    }

    @Override
    public void update(String barCode, int amount) {
        coll.updateOne(eq("kod_kreskowy", barCode),
                new Document("$set", new Document("ilosc", amount))
        );
    }

    @Override
    public void delete(String barCode) {
        coll.deleteOne(eq("kod_kreskowy", barCode));
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> out = new ArrayList<>();
        for (Document d : coll.find()) {
            String code = d.get("kod_kreskowy").toString();
            int qty = d.getInteger("ilosc", 0);
            Produkt p = produktDao.findById(code);
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
    public void addProduct(Produkt product) {
        add(product.getBarCode(), 1);
    }
}
