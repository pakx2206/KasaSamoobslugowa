package ppacocha.kasasamoobslugowa.service;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.ArrayList;
import java.util.List;

public class KasaService {
  private List < Produkt > koszyk = new ArrayList < > ();

  public void dodajProdukt(Produkt produkt) {
    koszyk.add(produkt);
  }

  public void usunProdukt(Produkt produkt) {
    koszyk.remove(produkt);
  }

  public void zmienIlosc(Produkt produkt, int ilosc) {
    koszyk.removeIf(p -> p.equals(produkt));
    for (int i = 0; i < ilosc; i++) {
      koszyk.add(produkt);
    }
  }

  public Transakcja finalizujTransakcje() {
    Transakcja transakcja = new Transakcja(new ArrayList < > (koszyk));
    koszyk.clear();
    return transakcja;
  }

  public List < Produkt > getKoszyk() {
    return new ArrayList < > (koszyk);
  }
}