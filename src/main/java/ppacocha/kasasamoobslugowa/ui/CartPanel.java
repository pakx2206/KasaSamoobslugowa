package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.nfc.CardReaderNdef;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class CartPanel extends JPanel {
    private final KasaService kasaService;
    private final MainFrame mainFrame;
    private final DefaultListModel<Produkt> listModel;
    private final JList<Produkt> productList;
    private final JLabel totalLabel;

    public CartPanel(KasaService service, MainFrame frame) {
        this.kasaService = service;
        this.mainFrame = frame;
        setLayout(new BorderLayout(10,10));

        listModel = new DefaultListModel<>();
        productList = new JList<>(listModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(productList);

        JButton btnRemove   = new JButton("Usuń");
        JButton btnChange   = new JButton("Zmień ilość");
        JButton btnManual   = new JButton("Dodaj kod ręcznie");
        JButton btnCheckout = new JButton("Finalizuj transakcję");
        JButton btnBack     = new JButton("Powrót");

        btnRemove.addActionListener(e -> removeProduct());
        btnChange.addActionListener(e -> changeQuantity());
        btnManual.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(this, "Podaj kod produktu:");
            if (code != null && !code.trim().isEmpty()) {
                try {
                    kasaService.dodajPoKodzieLubTagu(code.trim());
                    refreshList();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Błąd: " + ex.getMessage(),
                        "Błąd", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnCheckout.addActionListener(e -> checkout());
        btnBack.addActionListener(e -> mainFrame.getCardLayout().show(mainFrame.getCards(), "START"));

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnRemove);
        btnPanel.add(btnChange);
        btnPanel.add(btnManual);
        btnPanel.add(btnCheckout);
        btnPanel.add(btnBack);

        totalLabel = new JLabel("Suma: 0");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));

        add(totalLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        refreshList();

        try {
            CardReaderNdef reader = new CardReaderNdef();
            Thread nfcThread = new Thread(() -> {
                while (true) {
                    try {
                        String raw = reader.readTextRecord().trim().toLowerCase();
                        String code;
                        if (raw.matches("n\\d+")) {
                            code = "NFC" + String.format("%03d", Integer.parseInt(raw.substring(1)));
                        } else if (raw.matches("\\d+")) {
                            code = "NFC" + String.format("%03d", Integer.parseInt(raw));
                        } else {
                            code = raw.toUpperCase();
                        }
                        String finalCode = code;
                        SwingUtilities.invokeLater(() -> {
                            try {
                                kasaService.dodajPoKodzieLubTagu(finalCode);
                                refreshList();
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(this,
                                    "Błąd NFC: " + ex.getMessage(),
                                    "Błąd", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } catch (Exception ignored) {}
                }
            }, "NFC-Scanner-Thread");

            nfcThread.setDaemon(true);
            nfcThread.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Nie można uruchomić skanera NFC: " + e.getMessage(),
                "Błąd NFC", JOptionPane.ERROR_MESSAGE);
        }
    }

    void refreshList() {
        listModel.clear();
        for (Produkt p : kasaService.getKoszyk()) {
            listModel.addElement(p);
        }
        BigDecimal sum = kasaService.getKoszyk().stream()
            .map(Produkt::getCena)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        totalLabel.setText("Suma: " + sum);
    }

    private void removeProduct() {
        Produkt selected = productList.getSelectedValue();
        if (selected != null) {
            kasaService.usunPoKodzie(selected.getKodKreskowy());
            refreshList();
        }
    }

    private void changeQuantity() {
        Produkt selected = productList.getSelectedValue();
        if (selected != null) {
            String input = JOptionPane.showInputDialog(
                this,
                "Nowa ilość dla " + selected.getKodKreskowy() + ":"
            );
            try {
                int qty = Integer.parseInt(input);
                kasaService.zmienIloscPoKodzie(selected.getKodKreskowy(), qty);
                refreshList();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Niepoprawna ilość.");
            }
        }
    }

    private void checkout() {
        if (kasaService.getKoszyk().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Koszyk jest pusty — nie można finalizować transakcji",
                "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String[] options = {"Gotówka", "Karta"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Wybierz metodę płatności:",
            "Płatność", JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice < 0) return;
        String typPlatnosci = options[choice];
        Transakcja t;
        try {
            t = kasaService.finalizujTransakcje(typPlatnosci);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(
            this,
            "Transakcja zakończona.\nSuma: " + t.getSuma() + " PLN\nPłatność: " + t.getTypPlatnosci(),
            "Potwierdzenie", JOptionPane.INFORMATION_MESSAGE);
        refreshList();
        mainFrame.getCardLayout().show(mainFrame.getCards(), "START");
    }
}