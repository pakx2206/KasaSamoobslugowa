package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Produkt;
import java.util.List;

public interface KoszykDAO {
    void add(String barCode, int amount);
    void update(String barCode, int amount);
    void delete(String barCode);
    List<Produkt> findAll();
    void clear();
    public void addProduct(Produkt product);

}
