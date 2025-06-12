package ppacocha.kasasamoobslugowa.model;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class Transakcja {
    private String id;
    private final List<Produkt> product;
    private LocalDateTime data;
    private BigDecimal suma;
    private final String typeOfPayment;
    private String ageVerifiedBy;
    private LocalDateTime ageVerifiedAt;
    private String nip;

    public Transakcja(List<Produkt> product, String typeOfPayment) {
        this.product = product;
        this.data = LocalDateTime.now();
        this.suma = calculateSum();
        this.typeOfPayment = typeOfPayment;
    }
    private BigDecimal calculateSum() {
        return product.stream().map(Produkt::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public List<Produkt> getProduct() {
        return product;
    }
    public LocalDateTime getData() {
        return data;
    }
    public void setData(LocalDateTime data) {
        this.data = data;
    }
    public BigDecimal getSuma() {
        return suma;
    }
    public void setSuma(BigDecimal suma) {
        this.suma = suma;
    }
    public String getTypeOfPayment() {
        return typeOfPayment;
    }
    public LocalDateTime getAgeVerifiedAt() {
        return ageVerifiedAt;
    }
    public void setAgeVerifiedAt(LocalDateTime ageVerifiedAt) {
        this.ageVerifiedAt = ageVerifiedAt;
    }
    public String getNip() {
        return nip;
    }
    public void setNip(String nip) {
        this.nip = nip;
    }

    @Override
    public String toString() {
        return "Transakcja{" +
                "id=" + id +
                ", data=" + data +
                ", suma=" + suma +
                ", typPlatnosci='" + typeOfPayment + '\'' +
                (nip != null ? ", nip='" + nip + '\'' : "") +
                ", iloscProd=" + product.size() +
                '}';
    }
}
