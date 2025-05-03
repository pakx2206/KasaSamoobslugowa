package ppacocha.kasasamoobslugowa.DAO.impl;
import java.math.RoundingMode;
import ppacocha.kasasamoobslugowa.DAO.TransakcjaDAO;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.model.Produkt;
import java.sql.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class SQLiteTransakcjaDAO implements TransakcjaDAO {
    private static final String URL = "jdbc:sqlite:kasa.db";

    @Override
    public int save(Transakcja transakcja) {
        String insertTxn = "INSERT INTO transakcja(data, suma) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement psTxn = conn.prepareStatement(insertTxn, Statement.RETURN_GENERATED_KEYS)) {
            psTxn.setString(1, transakcja.getData().toString());
            psTxn.setBigDecimal(2, transakcja.getSuma());
            psTxn.executeUpdate();

            ResultSet keys = psTxn.getGeneratedKeys();
            int id = keys.next() ? keys.getInt(1) : -1;

            String insertItem = "INSERT INTO transakcja_produkt(transakcja_id, kod_kreskowy, ilosc) VALUES (?, ?, ?)";
            try (PreparedStatement psItem = conn.prepareStatement(insertItem)) {
                Map<String, Long> counts = transakcja.getProdukty().stream()
                    .collect(Collectors.groupingBy(Produkt::getKodKreskowy, Collectors.counting()));
                for (Map.Entry<String, Long> e : counts.entrySet()) {
                    psItem.setInt(1, id);
                    psItem.setString(2, e.getKey());
                    psItem.setInt(3, e.getValue().intValue());
                    psItem.executeUpdate();
                }
            }
            return id;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    @Override
    public Transakcja findById(int id) {
    String sqlTxn   = "SELECT data, suma FROM transakcja WHERE id = ?";
    String sqlItems = "SELECT kod_kreskowy, ilosc FROM transakcja_produkt WHERE transakcja_id = ?";
    try (Connection conn = DriverManager.getConnection(URL);
         PreparedStatement psTxn   = conn.prepareStatement(sqlTxn);
         PreparedStatement psItems = conn.prepareStatement(sqlItems)) {

        psTxn.setInt(1, id);
        ResultSet rsTxn = psTxn.executeQuery();
        if (!rsTxn.next()) return null;

        LocalDateTime date = LocalDateTime.parse(rsTxn.getString("data"));
        BigDecimal rawSum = rsTxn.getBigDecimal("suma");
        BigDecimal sum = rawSum.setScale(2, RoundingMode.UNNECESSARY);

        List<Produkt> products = new ArrayList<>();
        psItems.setInt(1, id);
        ResultSet rsItems = psItems.executeQuery();
        while (rsItems.next()) {
            String code   = rsItems.getString("kod_kreskowy");
            int quantity  = rsItems.getInt("ilosc");
            Produkt p     = new SQLiteProduktDAO().findById(code);
            for (int i = 0; i < quantity; i++) products.add(p);
        }

        Transakcja txn = new Transakcja(products);
        txn.setData(date);
        txn.setSuma(sum);
        return txn;

    } catch (SQLException ex) {
        ex.printStackTrace();
        return null;
    }
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
}