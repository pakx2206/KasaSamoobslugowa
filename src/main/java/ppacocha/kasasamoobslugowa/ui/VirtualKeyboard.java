package ppacocha.kasasamoobslugowa.ui;

import javax.swing.*;
import java.awt.*;

public class VirtualKeyboard extends JDialog {
    private final JTextField target;
    private final boolean full;

    public VirtualKeyboard(Frame owner, JTextField target) {
        this(owner, target, false);
    }

    public VirtualKeyboard(Frame owner, JTextField target, boolean fullLayout) {
        super(owner, true);
        this.target = target;
        this.full   = fullLayout;
        initKeyboard();
    }

    private void initKeyboard() {
        getContentPane().removeAll();
        if (full) {
            setLayout(new GridLayout(4,10,5,5));
            String[] keys = {
                    "1","2","3","4","5","6","7","8","9","0",
                    "Q","W","E","R","T","Y","U","I","O","P",
                    "A","S","D","F","G","H","J","K","L","←",
                    "Z","X","C","V","B","N","M","SP","CLR","OK"
            };
            for (String k: keys) addKeyButton(k);
        } else {
            setLayout(new GridLayout(4,3,5,5));
            String[] keys = {"1","2","3","4","5","6","7","8","9","0","←","OK"};
            for (String k: keys) addKeyButton(k);
        }
        pack();
        Point p = target.getLocationOnScreen();
        setLocation(p.x, p.y + target.getHeight() + 2);
    }

    private void addKeyButton(String k) {
        JButton b = new JButton(k);
        b.setFocusable(false);
        b.addActionListener(e -> {
            if ("OK".equals(k)) {
                dispose();
            } else if ("CLR".equals(k)) {
                target.setText("");
            } else if ("←".equals(k)) {
                String t = target.getText();
                if (!t.isEmpty()) target.setText(t.substring(0, t.length()-1));
            } else if ("SP".equals(k)) {
                target.setText(target.getText() + " ");
            } else {
                target.setText(target.getText() + k);
            }
            target.requestFocusInWindow();
        });
        add(b);
    }
}
