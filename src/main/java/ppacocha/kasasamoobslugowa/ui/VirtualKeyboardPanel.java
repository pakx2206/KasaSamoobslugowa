package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class VirtualKeyboardPanel extends JPanel {
    private final JTextField target;
    private final String placeholder;

    public VirtualKeyboardPanel(JTextField target, boolean fullLayout) {
        this.target = target;
        this.placeholder = target.getText();
        if (fullLayout) initFull();
        else           initNumeric();
    }

    private void initNumeric() {
        setLayout(new GridLayout(4, 3, 2, 2));
        String[] keys = {"1","2","3","4","5","6","7","8","9","0","←","OK"};
        for (String k : keys) add(makeButton(k));
    }

    private void initFull() {
        setLayout(new GridLayout(4, 10, 2, 2));
        String[] keys = {
                "1","2","3","4","5","6","7","8","9","0",
                "Q","W","E","R","T","Y","U","I","O","P",
                "A","S","D","F","G","H","J","K","L","←",
                "Z","X","C","V","B","N","M","SP","CLR","OK"
        };
        for (String k : keys) add(makeButton(k));
    }

    private JButton makeButton(String k) {
        JButton b = new JButton(k);
        b.setFocusable(false);
        b.addActionListener(e -> {
            String text = target.getText();

            if (text.equals(placeholder) &&
                    !"CLR".equals(k) &&
                    !"OK".equals(k) &&
                    !"←".equals(k)) {
                target.setText("");
                text = "";
            }

            if ("OK".equals(k)) {
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w != null) w.dispose();
                Window parent = SwingUtilities.getWindowAncestor(w);
                if (parent instanceof JDialog) ((JDialog)parent).dispose();
            } else if ("CLR".equals(k)) {
                target.setText("");
            } else if ("←".equals(k)) {
                if (!text.isEmpty()) {
                    target.setText(text.substring(0, text.length() - 1));
                }
            } else if ("SP".equals(k)) {
                target.setText(target.getText() + " ");
            } else {
                target.setText(target.getText() + k);
            }

            target.requestFocusInWindow();
        });
        return b;
    }
}
