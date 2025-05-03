package ppacocha.kasasamoobslugowa.DAO;

import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.List;

public interface TransakcjaDAO {
    Transakcja findById(int id);
    List<Transakcja> findAll();
    int save(Transakcja transakcja);
    void update(Transakcja transakcja);
    void delete(int id);
    
}