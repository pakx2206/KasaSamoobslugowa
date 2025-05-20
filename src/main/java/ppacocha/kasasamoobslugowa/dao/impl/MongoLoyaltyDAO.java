package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ppacocha.kasasamoobslugowa.dao.LoyaltyDAO;
import ppacocha.kasasamoobslugowa.model.Loyalty;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

public class MongoLoyaltyDAO implements LoyaltyDAO {

    private final MongoCollection<Document> collection;

    public MongoLoyaltyDAO() {
        MongoDatabase db = MongoClientProvider.getDatabase();
        this.collection = db.getCollection("loyalty");
    }

    @Override
    public Loyalty findByPhoneOrTag(String phoneOrTag) {
        Document doc = collection.find(
                new Document("$or", java.util.List.of(
                        new Document("phone", phoneOrTag),
                        new Document("nfcTag", phoneOrTag)
                ))
        ).first();

        if (doc == null) return null;

        return new Loyalty(
                doc.getString("phone"),
                null
        );
    }
}
