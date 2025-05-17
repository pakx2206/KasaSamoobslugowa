package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Produkt;
import java.util.List;

public interface ProduktDAO {
    Produkt findById(String kodKreskowy);
    Produkt findByNfcTag(String nfcTag);
    List<Produkt> findAll();
    void save(Produkt produkt);
    void update(Produkt produkt);
    void delete(String kodKreskowy);
    List<Produkt> findByPartialCode(String partialCode);
    void zmniejszStan(String kodKreskowy, int amount);
}
