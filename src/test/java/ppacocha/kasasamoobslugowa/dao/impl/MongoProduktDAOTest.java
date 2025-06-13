package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MongoProduktDAOTest {
    private MongoCollection<Document> coll;
    private MongoProduktDAO dao;
    private MockedStatic<MongoClientProvider> provider;

    @BeforeEach
    void setUp() {
        MongoDatabase db = mock(MongoDatabase.class);
        coll = mock(MongoCollection.class);
        when(db.getCollection("produkt")).thenReturn(coll);
        provider = Mockito.mockStatic(MongoClientProvider.class);
        provider.when(MongoClientProvider::getDatabase).thenReturn(db);
        dao = new MongoProduktDAO();
    }

    @AfterEach
    void tearDown() {
        provider.close();
    }

    @Test
    void reduceStock_stringKey() {
        dao.reduceStock("XYZ", 3);
        verify(coll).updateOne(
                eq(new Document("kod_kreskowy", "XYZ")),
                eq(new Document("$inc", new Document("ilosc", -3)))
        );
    }

    @Test
    void reduceStock_numericKey() {
        dao.reduceStock("200", 5);
        verify(coll).updateOne(
                eq(new Document("kod_kreskowy", 200L)),
                eq(new Document("$inc", new Document("ilosc", -5)))
        );
    }
}
