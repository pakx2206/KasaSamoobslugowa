package ppacocha.kasasamoobslugowa.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NumericInputDialog extends JDialog {
    private String value = null;

    public NumericInputDialog(Frame owner, String title) {
        super(owner, title, true);
        initUI();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        var content = new JPanel(new BorderLayout(10,10));
        var tf = new JTextField(10);
        tf.setFont(tf.getFont().deriveFont(24f));
        content.add(tf, BorderLayout.NORTH);

        var keys = new String[][]{
                {"1","2","3"},
                {"4","5","6"},
                {"7","8","9"},
                {"←","0","CLR"}
        };
        var grid = new JPanel(new GridLayout(4,3,5,5));
        for (int row=0; row<4; row++) {
            for (int col=0; col<3; col++) {
                var key = keys[row][col];
                var b = new JButton(key);
                b.setFont(b.getFont().deriveFont(18f));
                b.addActionListener((ActionEvent e) -> {
                    switch (key) {
                        case "←" -> {
                            var t = tf.getText();
                            if (!t.isEmpty()) tf.setText(t.substring(0,t.length()-1));
                        }
                        case "CLR" -> tf.setText("");
                        default   -> tf.setText(tf.getText() + key);
                    }
                });
                grid.add(b);
            }
        }
        content.add(grid, BorderLayout.CENTER);

        var btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5,5));
        var ok = new JButton("OK");
        var cancel = new JButton("Anuluj");
        ok.addActionListener(e -> {
            value = tf.getText();
            dispose();
        });
        cancel.addActionListener(e -> {
            value = null;
            dispose();
        });
        btns.add(cancel);
        btns.add(ok);
        content.add(btns, BorderLayout.SOUTH);

        setContentPane(content);
    }

    public static String showNumericDialog(Frame owner, String title) {
        var dlg = new NumericInputDialog(owner, title);
        dlg.setVisible(true);
        return dlg.value;
    }
}
