package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.service.KasaService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

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

        messageLabel = new JLabel(" ");
        messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));

        JPanel inputPanel = new JPanel();
        inputPanel.add(label);
        inputPanel.add(codeField);
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);
        add(messageLabel, BorderLayout.CENTER);

        addButton.addActionListener(e -> addProduct());
        codeField.addActionListener(e -> addProduct());
    }

    private void addProduct() {
        String code = codeField.getText().trim();
        try {
            kasaService.dodajPoKodzieLubTagu(code);
            messageLabel.setText("Dodano produkt: " + code);
            codeField.setText("");
        } catch (IllegalArgumentException ex) {
            messageLabel.setText("Błąd: " + ex.getMessage());
        }
    }
}
