package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Promotion;

public interface PromotionDAO {
    Promotion findByProductCode(String productCode);
}
