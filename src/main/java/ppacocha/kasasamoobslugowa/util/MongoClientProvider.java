package ppacocha.kasasamoobslugowa.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;

public class MongoClientProvider {

    private static final String URI = "mongodb+srv://kasaUser:kasaPass123@clusterselfcheckout.wu93pwz.mongodb.net/?retryWrites=true&w=majority&appName=ClusterSelfCheckout";

    private static final MongoClient client = MongoClients.create(URI);

    public static MongoDatabase getDatabase() {
        return client.getDatabase("kasa");
    }
    public static void main(String[] args) {
        var db = MongoClientProvider.getDatabase();
        System.out.println("Collections: " + db.listCollectionNames().into(new ArrayList<>()));
    }

}
