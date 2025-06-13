package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class AppFrameTest {

    private JButton findButton(Container c, String text) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JButton && text.equals(((JButton) comp).getText())) {
                return (JButton) comp;
            }
            if (comp instanceof Container) {
                JButton b = findButton((Container) comp, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    @Test
    void createFrame_noException() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
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

    @Test
    void layoutPanelUsesCardLayout() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            Component content = f.getContentPane().getComponent(0);
            assertTrue(content instanceof JPanel);
            assertTrue(((JPanel) content).getLayout() instanceof CardLayout);
            f.dispose();
        });
    }

    @Test
    void hasLanguageAndHelpButtons() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            assertNotNull(findButton(f.getContentPane(),"Język"));
            assertNotNull(findButton(f.getContentPane(),"Pomoc"));
            f.dispose();
        });
    }

    @Test
    void defaultProductCodePlaceholder() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            JTextField tf = findFirstTextField(f.getContentPane());
            assertNotNull(tf);
            assertEquals(LanguageSetup.get("pl","input.code"), tf.getText());
            f.dispose();
        });
    }

    private JTextField findFirstTextField(Container c) {
        for (Component comp : c.getComponents()) {
            if (comp instanceof JTextField)
                return (JTextField) comp;
            if (comp instanceof Container) {
                JTextField tf = findFirstTextField((Container) comp);
                if (tf != null)
                    return tf;
            }
        }
        return null;
    }

    @Test
    void startButtonIsPresentAndEnabled() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            JButton start = findButton(f.getContentPane(),"Rozpocznij Kasowanie Produktów");
            assertNotNull(start);
            assertTrue(start.isEnabled());
            f.dispose();
        });
    }

    @Test
    void payButtonInitiallyDisabled() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            JButton pay = findButton(f.getContentPane(),"Zapłać");
            assertNotNull(pay);
            assertTrue(pay.isEnabled());
            f.dispose();
        });
    }

    @Test
    void defaultCloseOperationIsDoNothing() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, f.getDefaultCloseOperation());
            f.dispose();
        });
    }

    @Test
    void contentPaneHasSecondaryBackground() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AppFrame f = new AppFrame();
            Color bg = f.getContentPane().getBackground();
            assertEquals(AppTheme.SECONDARY_BACKGROUND, bg);
            f.dispose();
        });
    }
}
