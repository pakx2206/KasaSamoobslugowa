package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;

public class Promotion {
    private String productCode;
    private BigDecimal discount;

    public Promotion(String productCode, BigDecimal discount) {
        this.productCode = productCode;
        this.discount = discount;
    }
    public BigDecimal getDiscount() {
        return discount;
    }

}
