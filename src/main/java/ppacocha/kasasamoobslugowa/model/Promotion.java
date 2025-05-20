package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;

public class Promotion {
    private String productCode;
    private BigDecimal discount;

    public Promotion(String productCode, BigDecimal discount) {
        this.productCode = productCode;
        this.discount = discount;
    }

    public String getProductCode() {
        return productCode;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }
}
