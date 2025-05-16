package ppacocha.kasasamoobslugowa;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import javax.swing.SwingUtilities;
import ppacocha.kasasamoobslugowa.ui.AppFrame;

public class App {
    public static void main(String[] args) {
        Path dbPath = Paths.get("kasa.db");
        boolean firstRun = Files.notExists(dbPath);

        if (firstRun) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:kasa.db")) {
                String schema = new String(Files.readAllBytes(Paths.get("src/main/resources/schema.sql")));
                conn.createStatement().executeUpdate(schema);

                String seed = new String(Files.readAllBytes(Paths.get("src/main/resources/seed.sql")));
                conn.createStatement().executeUpdate(seed);
                System.out.println("Baza danych utworzona i zainicjalizowana.");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }

        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}
