package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import java.awt.*;

public class VirtualKeyboardPanel extends JPanel {
    private final JTextField target;
    private final String langKey;

    public VirtualKeyboardPanel(JTextField target, boolean fullLayout, String langKey) {
        this.target = target;
        this.langKey = langKey;
        setBackground(new Color(0x004A99));
        setLayout(new GridBagLayout());
        if (fullLayout) initFull();
        else initNumeric();
    }

    private void initFull() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,3,3,3);
        c.weighty = 1.0;

        String[] row1 = {"1","2","3","4","5","6","7","8","9","0","←","DELETE"};
        for(int i=0; i<row1.length; i++){
            c.gridx = i; c.gridy = 0; c.weightx = 1.0;
            if("DELETE".equals(row1[i])) {
                c.gridwidth = 2;
                add(makeButton("DELETE"), c);
                i++;
                c.gridwidth = 1;
            } else {
                add(makeButton(row1[i]), c);
            }
        }

        String[] row2 = {"Q","W","E","R","T","Y","U","I","O","P"};
        for(int i=0; i<row2.length; i++){
            c.gridx = i; c.gridy = 1; c.weightx = 1.0;
            add(makeButton(row2[i]), c);
        }

        String[] row3 = {"A","S","D","F","G","H","J","K","L"};
        for(int i=0; i<row3.length; i++){
            c.gridx = i+1; c.gridy = 2; c.weightx = 1.0;
            add(makeButton(row3[i]), c);
        }

        String[] row4 = {"Z","X","C","V","B","N","M"};
        for(int i=0; i<row4.length; i++){
            c.gridx = i+2; c.gridy = 3; c.weightx = 1.0;
            add(makeButton(row4[i]), c);
        }

        c.gridx = 2; c.gridy = 4;
        c.gridwidth = 6;
        add(makeButton("SPACE"), c);
        c.gridwidth = 1;

        c.gridx = 8; c.gridy = 4; c.weightx = 1.0;
        add(makeButton("OK"), c);
    }

    private void initNumeric() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(3,3,3,3);
        c.weighty = 1.0;

        String[] keys = {"1","2","3","4","5","6","7","8","9","0","←","DELETE"};
        for(int i=0; i<keys.length; i++){
            c.gridx = i; c.gridy = 0; c.weightx = 1.0;
            add(makeButton(keys[i]), c);
        }
    }

    private JButton makeButton(String key) {
        String label = switch (key) {
            case "SPACE"->"SPACE";
            case "DELETE"->"DELETE";
            default -> key;
        };

        JButton b = new JButton(label);
        b.setFocusable(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 20f));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x005BBB));
        b.setOpaque(true);

        b.addActionListener(e -> {
            String txt = target.getText();
            String placeholderNow = LanguageSetup.get(langKey, "input.code");

            if (txt.equals(placeholderNow)
                    && !"DELETE".equals(label)
                    && !"OK".equals(label)
                    && !"←".equals(label)
                    && !"SPACE".equals(label)) {
                txt = "";
                target.setText("");
            }

            switch (label) {
                case "OK" -> {
                    Window w = SwingUtilities.getWindowAncestor(this);
                    if (w instanceof JDialog) ((JDialog) w).dispose();
                }
                case "DELETE" -> target.setText("");
                case "←" -> {
                    if (!txt.isEmpty())
                        target.setText(txt.substring(0, txt.length() - 1));
                }
                case "SPACE" -> target.setText(target.getText() + " ");
                default -> target.setText(target.getText() + label);
            }

            target.requestFocusInWindow();
        });

        b.getModel().addChangeListener(ev -> {
            ButtonModel m = b.getModel();
            if (m.isPressed()) b.setBackground(new Color(0x004A99));
            else if (m.isRollover()) b.setBackground(new Color(0x006ECC));
            else b.setBackground(new Color(0x005BBB));
        });

        return b;
    }
}
