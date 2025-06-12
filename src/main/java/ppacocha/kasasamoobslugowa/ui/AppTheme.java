package ppacocha.kasasamoobslugowa.ui;

import javax.swing.*;
import java.awt.*;

public class AppTheme {
    public static final Color BACKGROUND = new Color(0xFFFFFF);
    public static final Color SECONDARY_BACKGROUND = new Color(0xF5F5F5);
    public static final Color TEXT_COLOR = new Color(0x2B2B2B);
    public static final Color PRIMARY_BUTTON_BG = new Color(0x005A9C);
    public static final Color PRIMARY_BUTTON_FG = new Color(0xFFFFFF);
    public static final Color SECONDARY_BUTTON_BG = new Color(0x007A33);
    public static final Color SECONDARY_BUTTON_FG = new Color(0xFFFFFF);
    public static final Color BORDER_COLOR = new Color(0xCCCCCC);
    public static final Font FONT_LARGE_BOLD = new Font("Segoe UI", Font.BOLD,36);
    public static final Font FONT_MEDIUM_BOLD = new Font("Segoe UI", Font.BOLD,24);
    public static final Font FONT_MEDIUM = new Font("Segoe UI", Font.PLAIN, 24);
    public static final Dimension BUTTON_SIZE = new Dimension(300, 200);
    public static final int TABLE_ROW_HEIGHT = 80;
    public static void setupDefaults() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("ScrollPane.background", SECONDARY_BACKGROUND);
        UIManager.put("Label.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT_COLOR);
        UIManager.put("Label.font", FONT_MEDIUM);
        UIManager.put("Button.background", PRIMARY_BUTTON_BG);
        UIManager.put("Button.foreground", PRIMARY_BUTTON_FG);
        UIManager.put("Button.font", FONT_MEDIUM_BOLD);
        UIManager.put("Button.focus", PRIMARY_BUTTON_BG.darker());
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_COLOR);
        UIManager.put("TextField.font", FONT_MEDIUM);
        UIManager.put("Table.background", BACKGROUND);
        UIManager.put("Table.foreground", TEXT_COLOR);
        UIManager.put("Table.font", FONT_LARGE_BOLD);
        UIManager.put("Table.rowHeight", TABLE_ROW_HEIGHT);
        UIManager.put("Table.selectionBackground", PRIMARY_BUTTON_BG.darker());
        UIManager.put("Table.selectionForeground", PRIMARY_BUTTON_FG);
        UIManager.put("TableHeader.background", SECONDARY_BACKGROUND);
        UIManager.put("TableHeader.foreground", TEXT_COLOR);
        UIManager.put("TableHeader.font", FONT_MEDIUM_BOLD);
        UIManager.put("SplitPane.background", SECONDARY_BACKGROUND);
    }
}
