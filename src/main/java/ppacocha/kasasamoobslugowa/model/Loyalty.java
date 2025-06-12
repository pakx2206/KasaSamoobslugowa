package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;

public class Loyalty {
    private String id;
    private BigDecimal discount;

    public Loyalty(String id, BigDecimal discount) {
        this.id = id;
        this.discount = discount;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setId(String id) {
        this.id = id;
    }

}
