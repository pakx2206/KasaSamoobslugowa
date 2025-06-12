package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class AppFrameTest {

    @Test
    void createFrame_noException() {
        SwingUtilities.invokeLater(() -> {
            AppFrame f = new AppFrame();
            assertNotNull(f);
            assertTrue(f.isDisplayable());
            f.dispose();
        });
    }
    @Test
    void initDoesNotThrow() {
        assertDoesNotThrow(AppFrame::new);
    }

}
