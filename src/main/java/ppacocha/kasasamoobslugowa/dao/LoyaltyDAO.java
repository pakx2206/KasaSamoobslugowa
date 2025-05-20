package ppacocha.kasasamoobslugowa.dao;

import ppacocha.kasasamoobslugowa.model.Loyalty;

public interface LoyaltyDAO {
    Loyalty findByPhoneOrTag(String phoneOrTag);
}
