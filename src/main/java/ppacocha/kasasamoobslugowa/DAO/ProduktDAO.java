package ppacocha.kasasamoobslugowa.DAO;

import ppacocha.kasasamoobslugowa.model.Produkt;
import java.util.List;

public interface ProduktDAO {
    Produkt findById(String kodKreskowy);
    List<Produkt> findAll();
    void save(Produkt produkt);
    void update(Produkt produkt);
    void delete(String kodKreskowy);
}