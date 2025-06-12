package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.service.KasaService;

import javax.swing.JButton;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ProductSearchPanelTest {

    @Test
    void constructsWithoutException() {
        assertDoesNotThrow(() ->
                new ProductSearchPanel("pl", new KasaService(), ()-> {}, code-> {})
        );
    }
    @Test
    void backButtonInvokesOnBack() throws Exception {
        AtomicBoolean called = new AtomicBoolean(false);
        ProductSearchPanel panel =
                new ProductSearchPanel("pl", new KasaService(), () -> called.set(true), code -> {});
        Field f = ProductSearchPanel.class.getDeclaredField("backButton");
        f.setAccessible(true);
        JButton back = (JButton) f.get(panel);
        assertNotNull(back);
        back.doClick();
        assertTrue(called.get(), "callback onBack powinien zostać wywołany");
    }
}
