package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Produkt {
  private String name;
  private BigDecimal price;
  private String barCode;
  private String nfcTag;
  private int quantity;
  private BigDecimal vatRate;
  private boolean requiresAgeVerification;


  public Produkt(String name, BigDecimal price, String barCode, String nfcTag, int quantity, BigDecimal vatRate, boolean requiresAgeVerification) {
        this.name = name;
        this.price = price;
        this.barCode = barCode;
        this.nfcTag = nfcTag;
        this.quantity = quantity;
        this.vatRate = vatRate;
        this.requiresAgeVerification = requiresAgeVerification;
  }
  public Produkt(String name, BigDecimal price, String barCode, String nfcTag, boolean requiresAgeVerification) {
        this(name, price, barCode, nfcTag, 0, BigDecimal.valueOf(0.23), requiresAgeVerification);
  }
  public boolean isRequiresAgeVerification() {
    return requiresAgeVerification;
  }

  public String getName() {
    return name;
  }
  public BigDecimal getVatRate() {
        return vatRate;
    }
  public BigDecimal getPrice() {
    return price;
  }

  public String getBarCode() {
    return barCode;
  }

  public String getNfcTag() {
    return nfcTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Produkt))
      return false;
    Produkt produkt = (Produkt) o;
    return Objects.equals(barCode, produkt.barCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(barCode);
  }

  @Override
  public String toString() {
      return name + " (" + barCode + ")";
  }

    public int getQuantity() {
        return quantity;
    }

}