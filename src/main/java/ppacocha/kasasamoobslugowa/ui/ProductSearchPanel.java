package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.dao.impl.MongoProduktDAO;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSearchPanel extends JPanel {
    private final String langKey;
    private final KasaService kasaService;
    private final Runnable onBack;
    private final java.util.function.Consumer<String> onProductSelected;

    private final JTextField searchField = new JTextField();
    private final JButton backButton   = new JButton();
    private final JPanel itemGrid      = new JPanel();

    public ProductSearchPanel(String langKey,
                              KasaService kasaService,
                              Runnable onBack,
                              java.util.function.Consumer<String> onProductSelected) {
        this.langKey            = langKey;
        this.kasaService        = kasaService;
        this.onBack             = onBack;
        this.onProductSelected  = onProductSelected;

        setLayout(new BorderLayout(10,10));
        setBackground(AppTheme.SECONDARY_BACKGROUND);

        itemGrid.setLayout(new GridLayout(0, 4, 10, 10));
        JScrollPane scroll = new JScrollPane(itemGrid,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getViewport().setBackground(AppTheme.BACKGROUND);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(5,5));
        bottom.setBackground(AppTheme.SECONDARY_BACKGROUND);
        searchField.setText(LanguageSetup.get(langKey, "search.find"));
        searchField.setFont(AppTheme.FONT_MEDIUM);
        searchField.setForeground(AppTheme.TEXT_COLOR);
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(LanguageSetup.get(langKey, "search.find")))
                    searchField.setText("");
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isBlank())
                    searchField.setText(LanguageSetup.get(langKey, "search.find"));
            }
        });
        bottom.add(searchField, BorderLayout.CENTER);

        backButton.setText(LanguageSetup.get(langKey, "cart.back"));
        backButton.setFont(AppTheme.FONT_MEDIUM_BOLD);
        backButton.setBackground(AppTheme.PRIMARY_BUTTON_BG);
        backButton.setForeground(AppTheme.PRIMARY_BUTTON_FG);
        backButton.setPreferredSize(new Dimension(180, 50));
        backButton.addActionListener(e -> onBack.run());
        bottom.add(backButton, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { reloadGrid(); }
            public void removeUpdate(DocumentEvent e) { reloadGrid(); }
            public void changedUpdate(DocumentEvent e){ reloadGrid(); }
        });

        reloadGrid();
    }

    private void reloadGrid() {
        String q = searchField.getText().trim().toLowerCase();
        ProduktDAO produktDao = new MongoProduktDAO();
        List<Produkt> all = produktDao.findAll();

        List<Produkt> filtered = all.stream()
                .filter(p -> q.isBlank() ||
                        p.getNazwa().toLowerCase().contains(q) ||
                        p.getKodKreskowy().contains(q))
                .collect(Collectors.toList());

        itemGrid.removeAll();
        for (Produkt p : filtered) {
            JButton btn = makeProductButton(p);
            itemGrid.add(btn);
        }
        itemGrid.revalidate();
        itemGrid.repaint();
    }

    private JButton makeProductButton(Produkt p) {
        ImageIcon icon;
        try {
            icon = new ImageIcon(getClass().getResource("/images/" + p.getKodKreskowy() + ".png"));
        } catch (Exception ex) {
            icon = new ImageIcon();
        }

        JButton b = new JButton();
        b.setIcon(icon);
        b.setText("<html><center><span style='font-size:12px;'>"
                + p.getNazwa()
                + "</span></center></html>");
        b.setVerticalTextPosition(SwingConstants.BOTTOM);
        b.setHorizontalTextPosition(SwingConstants.CENTER);

        Font base = b.getFont();
        b.setFont(base.deriveFont(Font.PLAIN, 12f));

        b.setBackground(AppTheme.BACKGROUND);
        b.setFocusPainted(false);
        b.addActionListener(e -> onProductSelected.accept(p.getKodKreskowy()));
        return b;
    }

}
