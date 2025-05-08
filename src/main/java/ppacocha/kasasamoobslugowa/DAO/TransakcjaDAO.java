package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.List;

public interface TransakcjaDAO {
    int save(Transakcja tx);
    Transakcja findById(int id);
    List<Transakcja> findAll();
    void update(Transakcja tx);
    void delete(int id);
    
}