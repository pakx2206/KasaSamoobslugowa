package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MongoTransakcjaDAOTest {
    private MongoCollection<Document> coll;
    private MongoTransakcjaDAO dao;
    private MockedStatic<MongoClientProvider> provider;

    @BeforeEach
    void setUp() {
        MongoDatabase db = mock(MongoDatabase.class);
        coll = mock(MongoCollection.class);
        when(db.getCollection("transakcja")).thenReturn(coll);
        provider = Mockito.mockStatic(MongoClientProvider.class);
        provider.when(MongoClientProvider::getDatabase).thenReturn(db);
        dao = new MongoTransakcjaDAO();
    }

    @AfterEach
    void tearDown() {
        provider.close();
    }

    @Test
    void saveReturnsInsertedId() {
        Transakcja tx = new Transakcja(List.of(new Produkt("X", BigDecimal.ONE, "code", "tag", false)), "CASH");
        tx.setData(LocalDateTime.now());
        tx.setSuma(BigDecimal.ONE);

        ObjectId oid = new ObjectId();
        var acknowledged = InsertOneResult.acknowledged(new BsonObjectId(oid));
        when(coll.insertOne(any(Document.class))).thenReturn(acknowledged);

        String savedId = dao.save(tx);

        assertEquals(oid.toHexString(), savedId);
        verify(coll).insertOne(any(Document.class));
    }
}
