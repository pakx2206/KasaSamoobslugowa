package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NumericInputDialog extends JDialog {
    private final String language;
    private final String promptKey;
    private final boolean loyaltyMode;
    private JList<Country> prefixList;
    private final JTextField tf;
    private String value;

    private NumericInputDialog(Frame owner, String language, String promptKey, boolean loyaltyMode) {
        super(owner, true);
        this.language = language;
        this.promptKey = promptKey;
        this.loyaltyMode = loyaltyMode;
        this.tf = new JTextField();

        if (loyaltyMode) {
            List<Country> countries = new ArrayList<>();
            String[][] data = {
                    {"+48","/flags/pl.png"},
                    {"+49","/flags/de.png"},
                    {"+44","/flags/gb.png"},
                    {"+1","/flags/us.png"},
            };
            for (var row : data) {
                countries.add(new Country(row[0], row[1]));
            }
            prefixList = new JList<>(countries.toArray(new Country[0]));
            prefixList.setCellRenderer(new CountryCellRenderer());
            prefixList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            for (int i = 0; i < countries.size(); i++) {
                if (countries.get(i).code.equals("+48")) {
                    prefixList.setSelectedIndex(i);
                    break;
                }
            }
        }
        initUI(owner);
        pack();
        Dimension p = owner.getSize();
        setSize((int)(p.width * .45), (int)(p.height * .60));
        setLocationRelativeTo(owner);
    }

    private void initUI(Frame owner) {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(20,20,20,20));
        root.setBackground(Color.WHITE);

        JLabel header = new JLabel(
                LanguageSetup.get(language, promptKey),
                SwingConstants.CENTER
        );
        header.setFont(header.getFont().deriveFont(Font.BOLD, 28f));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10,10));
        center.setOpaque(false);

        if (loyaltyMode) {
            JScrollPane scroll = new JScrollPane(prefixList,
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setPreferredSize(new Dimension(180,300));
            center.add(scroll, BorderLayout.WEST);
        }

        JPanel mid = new JPanel(new BorderLayout(8,8));
        mid.setOpaque(false);

        tf.setFont(tf.getFont().deriveFont(Font.BOLD, 24f));
        tf.setPreferredSize(new Dimension(400,50));
        JPanel tfPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,5));
        tfPanel.setOpaque(false);
        tfPanel.add(tf);
        mid.add(tfPanel, BorderLayout.NORTH);

        String[] keys = {
                "1","2","3",
                "4","5","6",
                "7","8","9",
                "\u2190","0","CLR"
        };
        JPanel grid = new JPanel(new GridLayout(4,3,12,12));
        grid.setOpaque(false);
        for (String k:keys) {
            JButton b = new JButton(k);
            b.setFont(b.getFont().deriveFont(Font.BOLD,22f));
            b.setBackground(new Color(0x005A9C));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.addActionListener(e -> onKey(k));
            grid.add(b);
        }
        mid.add(grid, BorderLayout.CENTER);

        center.add(mid, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT,20,5));
        south.setOpaque(false);

        JButton cancel = new JButton(LanguageSetup.get(language,"button.cancel"));
        styleButton(cancel,160,50);
        cancel.addActionListener(e -> { value = null; dispose(); });
        south.add(cancel);

        JButton ok = new JButton(LanguageSetup.get(language,"button.ok"));
        styleButton(ok,160,50);
        ok.addActionListener(e -> {
            String num = tf.getText().trim();
            if (loyaltyMode && prefixList != null) {
                num = prefixList.getSelectedValue().code + num;
            }
            value = num;
            dispose();
        });
        south.add(ok);

        root.add(south, BorderLayout.SOUTH);

        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancel");
        root.getActionMap().put("cancel", new AbstractAction(){
            @Override public void actionPerformed(ActionEvent e){
                value = null; dispose();
            }
        });

        setContentPane(root);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void onKey(String k) {
        String t = tf.getText();
        switch(k) {
            case "\u2190" -> {
                if (!t.isEmpty()) tf.setText(t.substring(0,t.length()-1));
            }
            case "CLR" -> tf.setText("");
            default -> tf.setText(t + k);
        }
        tf.requestFocusInWindow();
    }

    private void styleButton(JButton b, int w, int h) {
        b.setFont(b.getFont().deriveFont(Font.BOLD,20f));
        b.setBackground(new Color(0x005A9C));
        b.setForeground(Color.WHITE);
        b.setPreferredSize(new Dimension(w,h));
        b.setFocusPainted(false);
    }

    public static String showNumericDialog(Frame owner, String language, String promptKey) {
        var dlg = new NumericInputDialog(owner, language, promptKey, false);
        dlg.setVisible(true);
        return dlg.value;
    }

    public static String showNumericDialog(Frame owner, String language, String promptKey, boolean loyalty) {
        var dlg = new NumericInputDialog(owner, language, promptKey, loyalty);
        dlg.setVisible(true);
        return dlg.value;
    }

    private static class Country {
        final String code;
        final ImageIcon flag;
        Country(String code, String path) {
            this.code = code;
            ImageIcon tmp;
            try {
                tmp = new ImageIcon(
                        ImageIO.read(getClass().getResourceAsStream(path))
                                .getScaledInstance(32,20,Image.SCALE_SMOOTH)
                );
            } catch (IOException|NullPointerException e) {
                tmp = new ImageIcon();
            }
            this.flag = tmp;
        }
        @Override public String toString() { return code; }
    }
    private static class CountryCellRenderer extends JLabel
            implements ListCellRenderer<Country> {
        CountryCellRenderer() {
            setOpaque(true);
            setFont(getFont().deriveFont(Font.PLAIN,22f));
            setBorder(new EmptyBorder(4,4,4,4));
        }
        @Override
        public Component getListCellRendererComponent(JList<? extends Country> l, Country v, int idx, boolean sel, boolean foc) {
            setText(v.code);
            setIcon(v.flag);
            setBackground(sel ? new Color(0xCCEEDD) : Color.WHITE);
            return this;
        }
    }
}
