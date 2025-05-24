package ppacocha.kasasamoobslugowa.ui;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class AppTheme {
    public static final Color PRIMARY_BG        = new Color(0x003366);
    public static final Color SECONDARY_BG      = new Color(0xE6E6E6);
    public static final Color ACCENT            = new Color(0x009933);
    public static final Color TEXT_ON_PRIMARY   = Color.WHITE;
    public static final Color TEXT_ON_SECONDARY = Color.BLACK;

    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 20);

    public static final Dimension BUTTON_SIZE = new Dimension(120, 120);

    public static final Border FOCUS_BORDER = BorderFactory.createLineBorder(ACCENT, 3);

    private AppTheme() {}

    public static void setupDefaults() {
        UIManager.put("Button.font", FONT_BUTTON);
        UIManager.put("Button.background", PRIMARY_BG);
        UIManager.put("Button.foreground", TEXT_ON_PRIMARY);
        UIManager.put("Button.focus", FOCUS_BORDER);
        UIManager.put("Button.minimumSize", BUTTON_SIZE);
        UIManager.put("Button.preferredSize", BUTTON_SIZE);

        UIManager.put("Label.font", FONT_LABEL);
        UIManager.put("Label.foreground", TEXT_ON_SECONDARY);

        UIManager.put("Panel.background", SECONDARY_BG);

        UIManager.put("Table.font", FONT_LABEL);
        UIManager.put("Table.foreground", TEXT_ON_SECONDARY);
        UIManager.put("Table.background", SECONDARY_BG);
        UIManager.put("Table.gridColor", PRIMARY_BG);

        UIManager.put("TextField.font", FONT_LABEL);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_ON_SECONDARY);
        UIManager.put("TextField.focus", FOCUS_BORDER);
    }
}
