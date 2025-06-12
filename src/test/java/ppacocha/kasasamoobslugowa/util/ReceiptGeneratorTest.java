package ppacocha.kasasamoobslugowa.util;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.service.KasaService;

import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReceiptGeneratorTest {

    static class NoDiscountKasaService extends KasaService {
        @Override
        public BigDecimal getPriceWithDiscount(Produkt product) {
            return product.getPrice();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T call(String name, Class<?>[] types, Object[] args) throws Exception {
        Method m = ReceiptGenerator.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return (T) m.invoke(null, args);
    }

    @Test
    void buildReceiptLines_basicTransaction_containsExpected() throws Exception {
        Produkt cola = new Produkt("Cola", BigDecimal.valueOf(5.00), "123", "NFC123", false);
        Transakcja tx = new Transakcja(List.of(cola, cola), "Cash");
        tx.setId("TX1");
        tx.setData(LocalDateTime.of(2025,1,2,15,30));
        tx.setNip("NIP-123");

        List<String> lines = call(
                "buildReceiptLines",
                new Class[]{Transakcja.class, String.class, KasaService.class},
                new Object[]{tx, "NIP-123", new NoDiscountKasaService()}
        );

        assertTrue(lines.get(0).contains("PARAGON FISKALNY"));
        assertTrue(lines.stream().anyMatch(l -> l.startsWith("Towar")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Cola")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("SUMA BRUTTO")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("NIP nabywcy: NIP-123")));
        assertTrue(lines.get(lines.size()-1).contains("Dziękujemy za zakupy!"));
    }

    @Test
    void renderLinesToImage_nonEmptyLines_returnsWhiteBackgroundImage() throws Exception {
        List<String> sample = List.of(
                "LINIA1",
                "LINIA2 długość",
                "SUMA BRUTTO:       10.00 PLN",
                "Dziękujemy!"
        );

        BufferedImage img = call(
                "renderLinesToImage",
                new Class[]{List.class},
                new Object[]{sample}
        );

        assertNotNull(img);
        assertTrue(img.getWidth()  >= 300);
        assertTrue(img.getHeight() >= sample.size()*12);
        int white = 0xFFFFFF;
        int rgb = img.getRGB(0,0) & 0xFFFFFF;
        assertEquals(white, rgb);
    }
}
