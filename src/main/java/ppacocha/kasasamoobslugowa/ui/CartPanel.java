package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;

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
        JButton btnCheckout = new JButton("Finalizuj transakcję");
        JButton btnBack     = new JButton("Powrót");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnRemove);
        btnPanel.add(btnChange);
        btnPanel.add(btnCheckout);
        btnPanel.add(btnBack);

        totalLabel = new JLabel("Suma: 0");
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD));

        add(scrollPane, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
        add(totalLabel, BorderLayout.NORTH);

        btnRemove.addActionListener(e -> removeProduct());
        btnChange.addActionListener(e -> changeQuantity());
        btnCheckout.addActionListener(e -> checkout());
        btnBack.addActionListener(e -> mainFrame.getCardLayout().show(mainFrame.getCards(), "START"));

        refreshList();
    }

    private void refreshList() {
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
        Transakcja t = kasaService.finalizujTransakcje();
        JOptionPane.showMessageDialog(
            this,
            "Transakcja zakończona.\nSuma: " + t.getSuma(),
            "Potwierdzenie",
            JOptionPane.INFORMATION_MESSAGE
        );
        refreshList();
    }
}
