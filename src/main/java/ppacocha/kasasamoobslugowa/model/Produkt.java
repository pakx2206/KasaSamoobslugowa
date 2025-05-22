package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Produkt {
  private String nazwa;
  private BigDecimal cena;
  private String kodKreskowy;
  private String nfcTag;
  private int ilosc;
  private BigDecimal vatRate;
  private boolean requiresAgeVerification;


  public Produkt(String nazwa, BigDecimal cena, String kodKreskowy,
                   String nfcTag, int ilosc, BigDecimal vatRate, boolean requiresAgeVerification) {
        this.nazwa       = nazwa;
        this.cena        = cena;
        this.kodKreskowy = kodKreskowy;
        this.nfcTag      = nfcTag;
        this.ilosc       = ilosc;
        this.vatRate     = vatRate;
        this.requiresAgeVerification = requiresAgeVerification;
  }
  public Produkt(String nazwa, BigDecimal cena, String kodKreskowy, String nfcTag,  boolean requiresAgeVerification) {
        this(nazwa, cena, kodKreskowy, nfcTag, 0, BigDecimal.valueOf(0.23), requiresAgeVerification);
  }
  public boolean isRequiresAgeVerification() {
    return requiresAgeVerification;
  }
  public void setRequiresAgeVerification(boolean requiresAgeVerification) {
    this.requiresAgeVerification = requiresAgeVerification;
  }
  public String getNazwa() {
    return nazwa;
  }
  public BigDecimal getVatRate() {
        return vatRate;
    }
  public void setNazwa(String nazwa) {
    this.nazwa = nazwa;
  }

  public BigDecimal getCena() {
    return cena;
  }

  public void setCena(BigDecimal cena) {
    this.cena = cena;
  }

  public String getKodKreskowy() {
    return kodKreskowy;
  }

  public void setKodKreskowy(String kodKreskowy) {
    this.kodKreskowy = kodKreskowy;
  }

  public String getNfcTag() {
    return nfcTag;
  }

  public void setNfcTag(String nfcTag) {
    this.nfcTag = nfcTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Produkt)) return false;
    Produkt produkt = (Produkt) o;
    return Objects.equals(kodKreskowy, produkt.kodKreskowy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(kodKreskowy);
  }

  @Override
  public String toString() {
      return nazwa + " (" + kodKreskowy + ")";
  }

    public int getIlosc() {
        return ilosc;
    }
    public void setIlosc(int ilosc) {
        this.ilosc = ilosc;
    }   
}