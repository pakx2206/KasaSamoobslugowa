package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import static org.mockito.Mockito.*;

class MongoPromotionDAOTest {
    private MockedStatic<MongoClientProvider> provider;
    private MongoPromotionDAO dao;

    @BeforeEach
    void setUp() {
        provider = Mockito.mockStatic(MongoClientProvider.class);
    }

    @AfterEach
    void tearDown() {
        provider.close();
    }

    @Test
    void initCollection() {
        MongoDatabase db = mock(MongoDatabase.class);
        MongoCollection<Document> col = mock(MongoCollection.class);
        when(MongoClientProvider.getDatabase()).thenReturn(db);
        when(db.getCollection("promotions")).thenReturn(col);

        dao = new MongoPromotionDAO();

        verify(db).getCollection("promotions");
    }
}
