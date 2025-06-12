package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.List;

public interface TransakcjaDAO {
    String save(Transakcja tx);
    Transakcja findById(String id);
    List<Transakcja> findAll();
}
