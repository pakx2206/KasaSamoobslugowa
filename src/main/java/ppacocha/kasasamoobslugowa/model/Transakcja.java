package ppacocha.kasasamoobslugowa.model;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class Transakcja {
  private List < Produkt > produkty;
  private LocalDateTime data;
  private BigDecimal suma;

  public Transakcja(List < Produkt > produkty) {
    this.produkty = produkty;
    this.data = LocalDateTime.now();
    this.suma = obliczSume();
  }

  private BigDecimal obliczSume() {
    return produkty.stream()
      .map(Produkt::getCena)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public List < Produkt > getProdukty() {
    return produkty;
  }

  public LocalDateTime getData() {
    return data;
  }

  public BigDecimal getSuma() {
    return suma;
  }

  @Override
  public String toString() {
    return "Transakcja{" +
      "produkty=" + produkty +
      ", data=" + data +
      ", suma=" + suma +
      '}';
  }
}