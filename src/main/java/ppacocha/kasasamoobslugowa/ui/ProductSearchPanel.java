package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.dao.impl.MongoProduktDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.Normalizer;
import java.util.List;

public class ProductSearchPanel extends JPanel {
    private final String langKey;
    private final KasaService kasaService;
    private final Runnable onBack;
    private final java.util.function.Consumer<String> onProductSelected;

    private final JTextField searchField = new JTextField();
    private final JButton backButton = new JButton();
    private final JPanel itemGrid = new JPanel();

    public ProductSearchPanel(String langKey, KasaService kasaService, Runnable onBack, java.util.function.Consumer<String> onProductSelected) {
        this.langKey = langKey;
        this.kasaService = kasaService;
        this.onBack = onBack;
        this.onProductSelected = onProductSelected;

        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.SECONDARY_BACKGROUND);

        itemGrid.setLayout(new GridLayout(0, 4, 10, 10));
        JScrollPane scroll = new JScrollPane(itemGrid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(AppTheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(50);
        add(scroll, BorderLayout.CENTER);

        JLabel searchLabel = new JLabel(LanguageSetup.get(langKey, "search.find"));
        searchLabel.setFont(AppTheme.FONT_MEDIUM_BOLD);
        searchLabel.setForeground(AppTheme.TEXT_COLOR);
        searchLabel.setLabelFor(searchField);

        searchField.setText("");
        searchField.setFont(AppTheme.FONT_MEDIUM);
        searchField.setForeground(AppTheme.TEXT_COLOR);
        searchField.addFocusListener(new FocusAdapter(){
            @Override public void focusGained(FocusEvent e) { /* l */ }
        });

        JPanel bottom = new JPanel(new BorderLayout(5,5));
        bottom.setBackground(AppTheme.SECONDARY_BACKGROUND);
        bottom.add(searchLabel, BorderLayout.WEST);
        bottom.add(searchField, BorderLayout.CENTER);

        backButton.setText(LanguageSetup.get(langKey, "cart.back"));
        backButton.setFont(AppTheme.FONT_MEDIUM_BOLD);
        backButton.setBackground(AppTheme.PRIMARY_BUTTON_BG);
        backButton.setForeground(AppTheme.PRIMARY_BUTTON_FG);
        backButton.setPreferredSize(new Dimension(180, 50));
        backButton.addActionListener(e -> onBack.run());
        bottom.add(backButton, BorderLayout.EAST);

        VirtualKeyboardPanel vk = new VirtualKeyboardPanel(searchField, true, langKey);
        vk.setPreferredSize(new Dimension(0, 200));

        JPanel south = new JPanel(new BorderLayout(5,5));
        south.setBackground(getBackground());
        south.add(bottom, BorderLayout.NORTH);
        south.add(vk, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> searchField.requestFocusInWindow());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                reloadGrid();
            }
            public void removeUpdate(DocumentEvent e) {
                reloadGrid();
            }
            public void changedUpdate(DocumentEvent e){}
        });
        reloadGrid();
    }

    private void reloadGrid() {
        String raw = searchField.getText().trim().toLowerCase();
        String q = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        List<Produkt> all = new MongoProduktDAO().findAll();

        List<Produkt> filtered = all.stream()
                .filter(p -> {
                    String name = p.getName().toLowerCase();
                    String nameNorm = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
                    return q.isEmpty() || nameNorm.contains(q) || p.getBarCode().contains(q);
                })
                .toList();

        itemGrid.removeAll();
        for (Produkt p : filtered) {
            itemGrid.add(makeProductButton(p));
        }
        itemGrid.revalidate();
        itemGrid.repaint();
    }

    private JButton makeProductButton(Produkt p) {
        ImageIcon raw = loadRawIcon(p.getBarCode());
        Image img = raw.getImage().getScaledInstance(120, 80, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(img);

        JButton b = new JButton("<html><center>" + p.getName() + "</center></html>", icon);
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);

        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCCCCCC)),
                BorderFactory.createEmptyBorder(12,12,12,12)
        ));
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        b.setForeground(new Color(0x2B2B2B));
        b.setFocusPainted(false);

        b.getModel().addChangeListener(e -> {
            ButtonModel m = b.getModel();
            if (m.isPressed())
                b.setBackground(new Color(0xF0F0F0));
            else
                b.setBackground(Color.WHITE);
        });
        b.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0x255EAB), 3),
                        BorderFactory.createEmptyBorder(10,10,10,10)
                ));
            }
            public void focusLost(FocusEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xCCCCCC)),
                        BorderFactory.createEmptyBorder(12,12,12,12)
                ));
            }
        });

        b.addActionListener(e -> onProductSelected.accept(p.getBarCode()));
        b.getAccessibleContext().setAccessibleName(p.getName());
        return b;
    }

    private ImageIcon loadRawIcon(String code) {
        try {
            return new ImageIcon(getClass().getResource("/images/" + code + ".png"));
        } catch (Exception ex) {
            return new ImageIcon();
        }
    }
}
