package ppacocha.kasasamoobslugowa.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Produkt {
  private String nazwa;
  private BigDecimal cena;
  private String kodKreskowy;
  private String nfcTag;

  public Produkt(String nazwa, BigDecimal cena, String kodKreskowy, String nfcTag) {
    this.nazwa = nazwa;
    this.cena = cena;
    this.kodKreskowy = kodKreskowy;
    this.nfcTag = nfcTag;
  }

  public String getNazwa() {
    return nazwa;
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
    return nazwa + " (" + cena + ")";
  }
}