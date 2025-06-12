package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.Test;

import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.*;

class AppThemeTest {
    @Test
    void constants_areCorrect() {
        assertEquals(new Color(0xFFFFFF), AppTheme.BACKGROUND);
        assertEquals(new Font("Segoe UI", Font.BOLD, 36), AppTheme.FONT_LARGE_BOLD);
        assertEquals(new Dimension(300,200), AppTheme.BUTTON_SIZE);
    }

    @Test
    void setupDefaults_registersUIManagerValues() {
        AppTheme.setupDefaults();
        assertEquals(AppTheme.BACKGROUND, UIManager.get("Panel.background"));
        assertEquals(AppTheme.PRIMARY_BUTTON_BG, UIManager.get("Button.background"));
        assertEquals(AppTheme.TABLE_ROW_HEIGHT, UIManager.get("Table.rowHeight"));
    }
}
