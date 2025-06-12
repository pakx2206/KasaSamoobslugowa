package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Produkt;
import java.util.List;

public interface ProduktDAO {
    Produkt findById(String barCode);
    Produkt findByNfcTag(String nfcTag);
    List<Produkt> findAll();
    List<Produkt> findByPartialCode(String partialCode);
    void reduceStock(String barCode, int amount);
    List<Produkt> findByCodeOrNameContaining(String string);
}
