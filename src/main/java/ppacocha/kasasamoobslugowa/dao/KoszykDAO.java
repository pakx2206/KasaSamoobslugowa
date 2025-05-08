package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Produkt;
import java.util.List;

public interface KoszykDAO {
    void add(String kodKreskowy, int amount);
    void update(String kodKreskowy, int amount);
    void delete(String kodKreskowy);
    List<Produkt> findAll();
    void clear();
    public void addProduct(Produkt p);
}
