package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.service.KasaService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class ScanPanel extends JPanel {
    private final KasaService kasaService;
    private final MainFrame mainFrame;
    private final JTextField codeField;
    private final JLabel messageLabel;

    public ScanPanel(KasaService service, MainFrame frame) {
        this.kasaService = service;
        this.mainFrame = frame;
        setLayout(new BorderLayout(10,10));

        JLabel label = new JLabel("Wprowadź kod kreskowy lub tag NFC:");
        codeField = new JTextField(20);
        JButton addButton = new JButton("Dodaj");
        JButton searchButton = new JButton("Wyszukaj");

        messageLabel = new JLabel(" ");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

        JPanel inputPanel = new JPanel();
        inputPanel.add(label);
        inputPanel.add(codeField);
        inputPanel.add(addButton);
        inputPanel.add(searchButton);

        searchButton.addActionListener(e -> searchProducts());

        add(inputPanel, BorderLayout.NORTH);
        add(messageLabel, BorderLayout.CENTER);

        addButton.addActionListener(e -> addProduct());
        codeField.addActionListener(e -> addProduct());
    }

    private void addProduct() {
        String code = codeField.getText().trim();
        try {
            kasaService.dodajPoKodzieLubTagu(code);
            mainFrame.getCartPanel().refreshList();
            messageLabel.setText("Dodano produkt: " + code);
            codeField.setText("");
        } catch (IllegalArgumentException ex) {
            messageLabel.setText("Błąd: " + ex.getMessage());
        }
    }

    private void searchProducts() {
        String partial = codeField.getText().trim();
        if (partial.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Wpisz fragment kodu kreskowego.");
            return;
        }

        List<Produkt> wyniki = kasaService.szukajPoFragmencieKodu(partial);
        if (wyniki.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Brak wyników.");
            return;
        }

        Produkt selected = (Produkt) JOptionPane.showInputDialog(
            this,
            "Wybierz produkt:",
            "Wyniki wyszukiwania",
            JOptionPane.PLAIN_MESSAGE,
            null,
            wyniki.toArray(),
            wyniki.get(0)
        );

        if (selected != null) {
            kasaService.dodajPoKodzieLubTagu(selected.getKodKreskowy());
            mainFrame.getCartPanel().refreshList();
            messageLabel.setText("Dodano produkt: " + selected.getNazwa());
            codeField.setText("");
        }
    }
}