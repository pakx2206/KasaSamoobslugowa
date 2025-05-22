package ppacocha.kasasamoobslugowa.model;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class Transakcja {
    private String id;
    private final List<Produkt> produkty;
    private LocalDateTime data;
    private BigDecimal suma;
    private final String typPlatnosci;
    private String ageVerifiedBy;
    private LocalDateTime ageVerifiedAt;
    public Transakcja(List<Produkt> produkty, String typPlatnosci) {
        this.produkty = produkty;
        this.data = LocalDateTime.now();
        this.suma = obliczSume();
        this.typPlatnosci = typPlatnosci;
    }

    private BigDecimal obliczSume() {
        return produkty.stream()
                       .map(Produkt::getCena)
                       .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Produkt> getProdukty() { return produkty; }
    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
    public BigDecimal getSuma() { return suma; }
    public void setSuma(BigDecimal suma) { this.suma = suma; }
    public String getTypPlatnosci() { return typPlatnosci; }
    public LocalDateTime getAgeVerifiedAt() {
        return ageVerifiedAt;
    }
    public void setAgeVerifiedAt(LocalDateTime ageVerifiedAt) {
        this.ageVerifiedAt = ageVerifiedAt;
    }
    @Override
    public String toString() {
        return "Transakcja{" +
               "id=" + id +
               ", data=" + data +
               ", suma=" + suma +
               ", typPlatnosci='" + typPlatnosci + '\'' +
               ", iloscProd=" + produkty.size() +
               '}';
    }
}
