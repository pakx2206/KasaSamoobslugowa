package ppacocha.kasasamoobslugowa.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class MongoClientProvider {

    private static final String PROPS = "mongodb.properties";
    private static final String URI;

    static {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(PROPS)) {
            if (in == null) {
                throw new IllegalStateException(PROPS + " not found on classpath");
            }
            Properties p = new Properties();
            p.load(in);
            URI = p.getProperty("MONGO_URL");
            if (URI == null || URI.isBlank()) {
                throw new IllegalStateException("MONGO_URL missing in " + PROPS);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load " + PROPS + ": " + e);
        }
    }

    private static final MongoClient client = MongoClients.create(URI);

    public static MongoDatabase getDatabase() {
        return client.getDatabase("kasa");
    }
    public static void main(String[] args) {
        var db = MongoClientProvider.getDatabase();
    }

}
