package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import ppacocha.kasasamoobslugowa.dao.PromotionDAO;
import ppacocha.kasasamoobslugowa.model.Promotion;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import java.math.BigDecimal;

public class MongoPromotionDAO implements PromotionDAO {

    private final MongoCollection<Document> collection;

    public MongoPromotionDAO() {
        MongoDatabase db = MongoClientProvider.getDatabase();
        this.collection = db.getCollection("promotions");
    }

    @Override
    public Promotion findByProductCode(String productCode) {
        Document doc = collection.find(new Document("productCode", productCode)).first();
        if (doc == null) return null;

        BigDecimal discount = doc.get("discount", Double.class) != null
                ? BigDecimal.valueOf(doc.getDouble("discount"))
                : BigDecimal.ZERO;

        return new Promotion(productCode, discount);
    }
}
