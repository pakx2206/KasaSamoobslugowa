package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.service.KasaService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);
    private final KasaService kasaService = new KasaService();

    public MainFrame() {
        super("Kasa Samoobsługowa");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        UIManager.put("Label.font", new Font("SansSerif", Font.PLAIN, 18));
        UIManager.put("Button.font", new Font("SansSerif", Font.PLAIN, 18));

        initMenuBar();
        initCards();
        setContentPane(cards);
    }

    private void initMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu mOpcje = new JMenu("Opcje");
        JMenuItem miHelp = new JMenuItem("Pomoc");
        miHelp.addActionListener(e -> JOptionPane.showMessageDialog(
            this,
            "Pomoc: wciśnij przyciski, żeby poruszać się po kasie.",
            "Pomoc",
            JOptionPane.INFORMATION_MESSAGE
        ));
        JMenuItem miExit = new JMenuItem("Wyjście");
        miExit.addActionListener(e -> dispose());
        mOpcje.add(miHelp);
        mOpcje.addSeparator();
        mOpcje.add(miExit);
        mb.add(mOpcje);
        setJMenuBar(mb);
    }

    private void initCards() {
        cards.add(buildStartPanel(), "START");
        cards.add(new ScanPanel(kasaService, this), "SCAN");
        cards.add(new CartPanel(kasaService, this), "CART");
        
        cardLayout.show(cards, "START");
    }

    private JPanel buildStartPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);

        JButton btnScan = new JButton("Rozpocznij skanowanie");
        btnScan.addActionListener(e -> cardLayout.show(cards, "SCAN"));

        JButton btnCart = new JButton("Pokaż koszyk");
        btnCart.addActionListener(e -> cardLayout.show(cards, "CART"));

        c.gridy = 0; p.add(btnScan, c);
        c.gridy = 1; p.add(btnCart, c);
        return p;
    }

    public CardLayout getCardLayout() { return cardLayout; }
    public JPanel getCards() { return cards; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
