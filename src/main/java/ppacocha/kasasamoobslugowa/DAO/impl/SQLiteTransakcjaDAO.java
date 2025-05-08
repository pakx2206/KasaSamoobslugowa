package ppacocha.kasasamoobslugowa.dao.impl;
import java.math.RoundingMode;
import ppacocha.kasasamoobslugowa.dao.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.model.Produkt;
import java.sql.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;

public class SQLiteTransakcjaDAO implements TransakcjaDAO {
    private static final String URL = "jdbc:sqlite:kasa.db";

    @Override
    public int save(Transakcja tx) {
        String hdrSql = """
            INSERT INTO transakcja(data, suma, typ_platnosci)
            VALUES(?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement psHdr = conn.prepareStatement(hdrSql, Statement.RETURN_GENERATED_KEYS)) {

            psHdr.setString(1, tx.getData().toString());
            psHdr.setBigDecimal(2, tx.getSuma());
            psHdr.setString(3, tx.getTypPlatnosci());
            psHdr.executeUpdate();

            int txnId;
            try (ResultSet rs = psHdr.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Brak wygenerowanego klucza");
                txnId = rs.getInt(1);
            }

            Map<String, Integer> quantities = new HashMap<>();
            tx.getProdukty().forEach(p ->
                quantities.merge(p.getKodKreskowy(), 1, Integer::sum)
            );

            String detSql = """
                INSERT INTO transakcja_produkt
                  (transakcja_id, kod_kreskowy, ilosc, cena_jednostkowa)
                VALUES (?, ?, ?, ?)
                """;
            try (PreparedStatement psDet = conn.prepareStatement(detSql)) {
                for (var entry : quantities.entrySet()) {
                    String code = entry.getKey();
                    int     qty  = entry.getValue();
                    BigDecimal price = tx.getProdukty().stream()
                        .filter(p -> p.getKodKreskowy().equals(code))
                        .findFirst()
                        .get()
                        .getCena();

                    psDet.setInt(1, txnId);
                    psDet.setString(2, code);
                    psDet.setInt(3, qty);
                    psDet.setBigDecimal(4, price);
                    psDet.addBatch();
                }
                psDet.executeBatch();
            }

            return txnId;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    
    @Override
    public Transakcja findById(int id) {
        String sql0 = "SELECT data, suma, typ_platnosci FROM transakcja WHERE id=?";
        String sql1 = "SELECT kod_kreskowy, ilosc, cena_jednostkowa FROM transakcja_produkt WHERE transakcja_id=?";
        try (Connection conn = DriverManager.getConnection(URL)) {
            Transakcja tx = null;
            try (PreparedStatement ps = conn.prepareStatement(sql0)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String typ = rs.getString("typ_platnosci");
                        tx = new Transakcja(new ArrayList<>(), typ);
                        tx.setId(id);
                        tx.setData(LocalDateTime.parse(rs.getString("data")));
                        tx.setSuma(rs.getBigDecimal("suma"));
                    }
                }
            }
            if (tx == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Produkt p = new SQLiteProduktDAO()
                              .findById(rs.getString("kod_kreskowy"));
                        int qty = rs.getInt("ilosc");
                        for (int i = 0; i < qty; i++) {
                            tx.getProdukty().add(p);
                        }
                    }
                }
            }
            return tx;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Transakcja> findAll() {
        List<Transakcja> list = new ArrayList<>();
        String sql = "SELECT id FROM transakcja";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transakcja txn = findById(rs.getInt("id"));
                if (txn != null) list.add(txn);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    @Override
    public void update(Transakcja transakcja) {
    }

    @Override
    public void delete(int id) {
        String delItems = "DELETE FROM transakcja_produkt WHERE transakcja_id = ?";
        String delTxn   = "DELETE FROM transakcj  a WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps1 = conn.prepareStatement(delItems);
             PreparedStatement ps2 = conn.prepareStatement(delTxn)) {
            ps1.setInt(1, id);
            ps1.executeUpdate();
            ps2.setInt(1, id);
            ps2.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public static void main(String[] args) {
        ProduktDAO dao = new SQLiteProduktDAO();
        List<Produkt> all = dao.findAll();
        System.out.println("Mamy w bazie: " + all.size() + " produktów");
        all.forEach(System.out::println);
    }
}