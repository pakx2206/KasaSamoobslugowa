package ppacocha.kasasamoobslugowa.dao.impl;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ppacocha.kasasamoobslugowa.util.MongoClientProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MongoKoszykDAOTest {
    private MongoCollection<Document> collection;
    private MongoKoszykDAO dao;
    private MockedStatic<MongoClientProvider> providerMock;

    @BeforeEach
    void setUp() {
        MongoDatabase dbMock = mock(MongoDatabase.class);
        collection = mock(MongoCollection.class);
        when(dbMock.getCollection("koszyk")).thenReturn(collection);
        providerMock = Mockito.mockStatic(MongoClientProvider.class);
        providerMock.when(MongoClientProvider::getDatabase).thenReturn(dbMock);
        dao = new MongoKoszykDAO();
    }

    @AfterEach
    void tearDown() {
        providerMock.close();
    }

    @Test
    void testAdd() {
        dao.add("123", 2);
        verify(collection).updateOne(
                eq(Filters.eq("kod_kreskowy", "123")),
                eq(Updates.inc("ilosc", 2)),
                any(UpdateOptions.class)
        );
    }

    @Test
    void testUpdate() {
        dao.update("456", 5);
        var captor = org.mockito.ArgumentCaptor.forClass(Document.class);
        verify(collection).updateOne(eq(Filters.eq("kod_kreskowy", "456")), captor.capture());
        Document doc = captor.getValue();
        assertTrue(doc.containsKey("$set"));
        assertEquals(5, ((Document) doc.get("$set")).getInteger("ilosc"));
    }

    @Test
    void testDelete() {
        dao.delete("789");
        verify(collection).deleteOne(Filters.eq("kod_kreskowy", "789"));
    }

    @Test
    void testClear() {
        dao.clear();
        verify(collection).deleteMany(any(Document.class));
    }

    @Test
    void testAddProduct() {
        ppacocha.kasasamoobslugowa.model.Produkt p = new ppacocha.kasasamoobslugowa.model.Produkt("Milk", java.math.BigDecimal.ONE, "111", "tag", false);
        dao.addProduct(p);
        verify(collection).updateOne(
                eq(Filters.eq("kod_kreskowy", "111")),
                eq(Updates.inc("ilosc", 1)),
                any(UpdateOptions.class)
        );
    }

}
