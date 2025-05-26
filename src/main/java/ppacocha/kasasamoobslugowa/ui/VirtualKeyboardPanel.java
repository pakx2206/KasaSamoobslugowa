package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VirtualKeyboardPanel extends JPanel {
    private final JTextField target;
    private final String langKey;

    public VirtualKeyboardPanel(JTextField target, boolean fullLayout, String langKey) {
        this.target  = target;
        this.langKey = langKey;

        if (fullLayout) initFull();
        else           initNumeric();
    }

    private void initNumeric() {
        setLayout(new GridLayout(4, 3, 2, 2));
        for (String k : new String[]{"1","2","3","4","5","6","7","8","9","0","←","OK"})
            add(makeButton(k));
    }

    private void initFull() {
        setLayout(new GridLayout(4, 10, 2, 2));
        String[] keys = {
                "1","2","3","4","5","6","7","8","9","0",
                "Q","W","E","R","T","Y","U","I","O","P",
                "A","S","D","F","G","H","J","K","L","←",
                "Z","X","C","V","B","N","M","SP","CLR","OK"
        };
        for (String k : keys)
            add(makeButton(k));
    }

    private JButton makeButton(String k) {
        JButton b = new JButton(k);
        b.setFocusable(false);
        b.addActionListener(e -> {
            String placeholderNow = LanguageSetup.get(langKey, "input.code");
            String text = target.getText();

            if (text.equals(placeholderNow)
                    && !"CLR".equals(k)
                    && !"OK".equals(k)
                    && !"←".equals(k)) {
                text = "";
                target.setText("");
            }

            switch (k) {
                case "OK" -> {
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w instanceof JDialog) ((JDialog) w).dispose();
                }
                case "CLR" -> target.setText("");
                case "←"   -> {
                    if (!text.isEmpty())
                        target.setText(text.substring(0, text.length() - 1));
                }
                case "SP"  -> target.setText(target.getText() + " ");
                default    -> target.setText(target.getText() + k);
            }

            target.requestFocusInWindow();
        });
        return b;
    }
}
