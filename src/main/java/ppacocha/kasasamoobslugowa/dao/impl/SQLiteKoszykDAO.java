package ppacocha.kasasamoobslugowa.dao.impl;

import ppacocha.kasasamoobslugowa.dao.KoszykDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;

public class SQLiteKoszykDAO implements KoszykDAO {
    private static final String URL = "jdbc:sqlite:kasa.db";
    private final ProduktDAO produktDao = new SQLiteProduktDAO();

    @Override
    public void add(String kodKreskowy, int amount) {
        String sql = """
            INSERT INTO koszyk(kod_kreskowy, ilosc) VALUES(?, ?)
            ON CONFLICT(kod_kreskowy) DO UPDATE SET ilosc = ilosc + excluded.ilosc
            """;
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kodKreskowy);
            ps.setInt(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(String kodKreskowy, int amount) {
        String sql = "UPDATE koszyk SET ilosc = ? WHERE kod_kreskowy = ?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, amount);
            ps.setString(2, kodKreskowy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(String kodKreskowy) {
        String sql = "DELETE FROM koszyk WHERE kod_kreskowy = ?";
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, kodKreskowy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> list = new ArrayList<>();
        String sql = "SELECT k.kod_kreskowy, k.ilosc, p.nazwa, p.cena, p.nfc_tag "
                   + "FROM koszyk k JOIN produkt p ON k.kod_kreskowy = p.kod_kreskowy";
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String code = rs.getString("kod_kreskowy");
                int qty    = rs.getInt("ilosc");
                Produkt p = produktDao.findById(code);
                for (int i = 0; i < qty; i++) {
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void clear() {
        String sql = "DELETE FROM koszyk";
        try (Connection c = DriverManager.getConnection(URL);
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
