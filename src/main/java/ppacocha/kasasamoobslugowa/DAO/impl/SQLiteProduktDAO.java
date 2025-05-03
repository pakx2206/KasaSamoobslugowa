package ppacocha.kasasamoobslugowa.DAO.impl;

import ppacocha.kasasamoobslugowa.DAO.ProduktDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import java.sql.*;
import java.util.*;

public class SQLiteProduktDAO implements ProduktDAO {
    private static final String URL = "jdbc:sqlite:kasa.db";
    
    @Override
    public Produkt findById(String kodKreskowy) {
        String sql = "SELECT nazwa, cena, nfc_tag FROM produkt WHERE kod_kreskowy = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodKreskowy);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Produkt(rs.getString("nazwa"), rs.getBigDecimal("cena"), kodKreskowy, rs.getString("nfc_tag"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public List<Produkt> findAll() {
        List<Produkt> list = new ArrayList<>();
        String sql = "SELECT kod_kreskowy, nazwa, cena, nfc_tag FROM produkt";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produkt(rs.getString("nazwa"), rs.getBigDecimal("cena"), rs.getString("kod_kreskowy"), rs.getString("nfc_tag")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    @Override
    public void save(Produkt produkt) {
        String sql = "INSERT INTO produkt(kod_kreskowy,nazwa,cena,nfc_tag) VALUES(?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produkt.getKodKreskowy());
            ps.setString(2, produkt.getNazwa());
            ps.setBigDecimal(3, produkt.getCena());
            ps.setString(4, produkt.getNfcTag());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void update(Produkt produkt) {
        String sql = "UPDATE produkt SET nazwa=?, cena=?, nfc_tag=? WHERE kod_kreskowy=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produkt.getNazwa());
            ps.setBigDecimal(2, produkt.getCena());
            ps.setString(3, produkt.getNfcTag());
            ps.setString(4, produkt.getKodKreskowy());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void delete(String kodKreskowy) {
        String sql = "DELETE FROM produkt WHERE kod_kreskowy = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodKreskowy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}