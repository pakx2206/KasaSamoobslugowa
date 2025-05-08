package ppacocha.kasasamoobslugowa.dao.impl;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.dao.ProduktDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteProduktDAO implements ProduktDAO {
    private static final String URL = "jdbc:sqlite:kasa.db";

    @Override
    public Produkt findById(String kodKreskowy) {
        String sql = "SELECT nazwa, cena, nfc_tag, ilosc FROM produkt WHERE kod_kreskowy = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodKreskowy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Produkt(
                        rs.getString("nazwa"),
                        rs.getBigDecimal("cena"),
                        kodKreskowy,
                        rs.getString("nfc_tag"),
                        rs.getInt("ilosc")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Produkt> findAll() {
        List<Produkt> list = new ArrayList<>();
        String sql = "SELECT kod_kreskowy, nazwa, cena, nfc_tag, ilosc FROM produkt";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Produkt(
                    rs.getString("nazwa"),
                    rs.getBigDecimal("cena"),
                    rs.getString("kod_kreskowy"),
                    rs.getString("nfc_tag"),
                    rs.getInt("ilosc")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void save(Produkt produkt) {
        String sql = "INSERT INTO produkt(kod_kreskowy, nazwa, cena, nfc_tag, ilosc) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produkt.getKodKreskowy());
            ps.setString(2, produkt.getNazwa());
            ps.setBigDecimal(3, produkt.getCena());
            ps.setString(4, produkt.getNfcTag());
            ps.setInt(5, produkt.getIlosc());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Produkt produkt) {
        String sql = "UPDATE produkt SET nazwa = ?, cena = ?, nfc_tag = ?, ilosc = ? WHERE kod_kreskowy = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, produkt.getNazwa());
            ps.setBigDecimal(2, produkt.getCena());
            ps.setString(3, produkt.getNfcTag());
            ps.setInt(4, produkt.getIlosc());
            ps.setString(5, produkt.getKodKreskowy());
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

    @Override
    public Produkt findByNfcTag(String nfcTag) {
        String sql = "SELECT kod_kreskowy, nazwa, cena, ilosc FROM produkt WHERE nfc_tag = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nfcTag);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Produkt(
                        rs.getString("nazwa"),
                        rs.getBigDecimal("cena"),
                        rs.getString("kod_kreskowy"),
                        nfcTag,
                        rs.getInt("ilosc")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Produkt> findByPartialCode(String partialCode) {
        List<Produkt> list = new ArrayList<>();
        String sql = "SELECT kod_kreskowy, nazwa, cena, nfc_tag, ilosc FROM produkt WHERE kod_kreskowy LIKE ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + partialCode + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Produkt(
                        rs.getString("nazwa"),
                        rs.getBigDecimal("cena"),
                        rs.getString("kod_kreskowy"),
                        rs.getString("nfc_tag"),
                        rs.getInt("ilosc")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void zmniejszStan(String kodKreskowy, int iloscDoOdjecia) {
        String sql = "UPDATE produkt SET ilosc = ilosc - ? WHERE kod_kreskowy = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, iloscDoOdjecia);
            ps.setString(2, kodKreskowy);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
