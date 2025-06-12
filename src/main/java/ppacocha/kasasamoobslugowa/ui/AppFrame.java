package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.dao.ProduktDAO;
import ppacocha.kasasamoobslugowa.dao.impl.MongoProduktDAO;
import ppacocha.kasasamoobslugowa.nfc.CardReaderNdef;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.service.RaportService;
import ppacocha.kasasamoobslugowa.util.PDFReportGenerator;
import ppacocha.kasasamoobslugowa.util.ReceiptGenerator;
import ppacocha.kasasamoobslugowa.util.LanguageSetup;

import javax.swing.border.Border;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;

public class AppFrame extends JFrame {
    private javax.swing.Timer inactivityTimer;
    private final KasaService kasaService;
    private final RaportService raportService;
    private Thread nfcThread;
    private CardReaderNdef reader;
    private String PickedLanguage = "pl";
    private CardLayout layout;
    private boolean waitingForAgeVerification = false;
    private boolean staffClosingAllowed = false;
    private Produkt productAwaitingVerification = null;
    private static final String STAFF_NFC_TAG = "NFC01";
    private boolean awaitingModifyAuth = false;
    private JPanel tablePanel;
    private JPanel sumPanel;
    private JDialog modifyAuthDialog;
    private List<String> basketOrderCodes = new ArrayList<>();
    private final ProduktDAO produktDao;
    private JDialog ageDialog;
    private final JButton printReportButton = new JButton("Drukuj raport");
    private JList<Produkt> productList;
    private DefaultListModel<Produkt> productListModel;
    private VirtualKeyboardPanel kb;
    private boolean loyaltyApplied = false;
    private JButton modifyCartButton;
    private boolean threadAlive;
    public AppFrame() {

        kasaService = new KasaService();
        raportService = new RaportService();
        this.produktDao = new MongoProduktDAO();
        productListModel = new DefaultListModel<>();
        productList = new JList<>(productListModel);
        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Produkt sel = productList.getSelectedValue();
                    if (sel != null) {
                        try {
                            handleScan(sel.getBarCode());
                            refreshBasketTable();
                            productCodeTextField.setText("");
                            productListModel.clear();
                            layout.show(layoutPanel, "card3");
                        } catch (IllegalArgumentException ex) {
                            JOptionPane.showMessageDialog(
                                    AppFrame.this,
                                    ex.getMessage(),
                                    LanguageSetup.get(PickedLanguage, "error"),
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            }
        });

        layout = new CardLayout();
        AppTheme.setupDefaults();
        initComponents();
        getContentPane().setBackground(AppTheme.SECONDARY_BACKGROUND);
        setLocationRelativeTo(null);
        setSize(1920, 1080);
        reInitCardLayout();
        getContentPane().repaint();

        manualProductEntryPanel.removeAll();
        manualProductEntryPanel.setLayout(new BorderLayout(10,10));
        JPanel topBar = new JPanel(new BorderLayout(5,5));
        topBar.add(backToBasketFromManualEntryButton, BorderLayout.WEST);
        JPanel inputPanel = new JPanel(new BorderLayout(5,5));
        inputPanel.add(productCodeTextField, BorderLayout.CENTER);
        inputPanel.add(searchForProductButton,   BorderLayout.EAST);
        topBar.add(inputPanel, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(productList);
        kb = new VirtualKeyboardPanel(productCodeTextField, true, PickedLanguage);
        manualProductEntryPanel.add(kb, BorderLayout.SOUTH);
        manualProductEntryPanel.add(topBar, BorderLayout.NORTH);
        manualProductEntryPanel.add(scroll,  BorderLayout.CENTER);
        manualProductEntryPanel.revalidate();
        manualProductEntryPanel.repaint();
        layout.show(layoutPanel, "cardSearch");
        Dimension btnSize = new Dimension(500, 300);
        Font      btnFont = new Font("Segoe UI", Font.BOLD, 36);

        for (JButton b : new JButton[]{ payByCashButton, payByCardButton }) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setMinimumSize(btnSize);
            b.setFont(btnFont);
        }

        paymentPanel.removeAll();
        paymentPanel.setLayout(new BoxLayout(paymentPanel, BoxLayout.X_AXIS));
        paymentPanel.add(Box.createHorizontalGlue());
        paymentPanel.add(payByCashButton);
        paymentPanel.add(Box.createHorizontalStrut(50));
        paymentPanel.add(payByCardButton);
        paymentPanel.add(Box.createHorizontalGlue());
        paymentPanel.revalidate();
        paymentPanel.repaint();



        updateTexts();
        layout.show(layoutPanel, "card2");

        productCodeTextField.getDocument().addDocumentListener(new DocumentListener() {
            void filter() {
                String txt = productCodeTextField.getText().trim();
                productListModel.clear();
                if (!txt.isEmpty()) {
                    kasaService.searchByCodeOrName(txt)
                            .forEach(productListModel::addElement);
                }
            }
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e){ filter(); }
        });
        addProductManually.addActionListener(evt -> {
            String code;
            Produkt sel = productList.getSelectedValue();
            if (sel != null) {
                code = sel.getBarCode();
            } else {
                code = productCodeTextField.getText().trim();
            }
            try {
                handleScan(code);
                refreshBasketTable();
                productCodeTextField.setText("");
                productListModel.clear();
                layout.show(layoutPanel, "card3");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        AppFrame.this,
                        ex.getMessage(),
                        LanguageSetup.get(PickedLanguage, "error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
        loyaltyCardButton.addActionListener(evt -> {
            if (loyaltyApplied) return;

            String code = NumericInputDialog.showNumericDialog(
                    this,
                    PickedLanguage,
                    "loyalty.prompt",
                    true
            );

            if (code != null && !code.isBlank()) {
                try {
                    kasaService.applyLoyaltyCard(code);
                    loyaltyApplied = true;
                    loyaltyCardButton.setEnabled(false);
                    refreshBasketTable();
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            LanguageSetup.get(PickedLanguage, "loyalty.success"),
                            LanguageSetup.get(PickedLanguage, "success"),
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            LanguageSetup.get(PickedLanguage, "error") + " " + ex.getMessage(),
                            LanguageSetup.get(PickedLanguage, "error"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        try {
            reader = new CardReaderNdef();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    LanguageSetup.get(PickedLanguage, "NFCScanner.connection.error") + "\n" + e.getMessage(),
                    LanguageSetup.get(PickedLanguage, "NFCScanner.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            reader = null;
        }
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JPasswordField pf = new JPasswordField();
                int ok = JOptionPane.showConfirmDialog(
                        AppFrame.this,
                        pf,
                        "Podaj hasło personelu:",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE
                );
                if (ok != JOptionPane.OK_OPTION) return;
                String pass = new String(pf.getPassword());
                if (!"admin123".equals(pass)) {
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            "Nieprawidłowe hasło",
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                try {
                    List<Transakcja> todays = raportService.getTodaysTransactions();
                    PDFReportGenerator.generateDailyReportPDF(todays);
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            "Raport dzienny zapisany jako PDF w folderze 'reports'",
                            "Gotowe",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            "Błąd generowania raportu: " + ex.getMessage(),
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                dispose();
                System.exit(0);
            }
        });
    }

    private void reInitCardLayout() {
        layoutPanel.add(startingPanel, "card2");
        layoutPanel.add(basketPanel, "card3");
        ProductSearchPanel searchPanel = new ProductSearchPanel(
                PickedLanguage,
                kasaService,
                () -> layout.show(layoutPanel, "card3"),
                code -> {
                    handleScan(code);
                    refreshBasketTable();
                    layout.show(layoutPanel, "card3");
                }
        );
        layoutPanel.add(searchPanel, "cardSearch");

        layoutPanel.add(languageSelectionPanel, "card5");
        layoutPanel.add(paymentPanel, "card6");
    }
    private void handleScan(String code) {
        if (awaitingModifyAuth && STAFF_NFC_TAG.equals(code)) {
            awaitingModifyAuth = false;
            if (modifyAuthDialog != null && modifyAuthDialog.isShowing())
                modifyAuthDialog.dispose();
            showModifyCartDialog();
            return;
        }
        if (productAwaitingVerification != null && STAFF_NFC_TAG.equals(code)) {
            if (ageDialog != null && ageDialog.isShowing()) {
                ageDialog.dispose();
            }

            kasaService.verifyAge();
            kasaService.addByCodeOrTag(productAwaitingVerification.getBarCode());
            refreshBasketTable();
            productAwaitingVerification = null;
            layout.show(layoutPanel, "card3");
            layoutPanel.requestFocusInWindow();
            return;
        }
        Produkt p = produktDao.findById(code);
        if (p == null) p = produktDao.findByNfcTag(code);
        if (p == null) {
            JOptionPane.showMessageDialog(
                    this,
                    LanguageSetup.get(PickedLanguage, "error.notFound") + ": " + code,
                    LanguageSetup.get(PickedLanguage, "error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        if (p.isRequiresAgeVerification() && !kasaService.isAgeVerified()) {
            productAwaitingVerification = p;
            ageDialog = new JDialog(this, LanguageSetup.get(PickedLanguage, "age.verifyTitle"), Dialog.ModalityType.APPLICATION_MODAL);
            ageDialog.setUndecorated(true);
            ageDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            ageDialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
            JPanel content = new JPanel(new BorderLayout(20,20));
            content.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
            content.setBackground(Color.WHITE);
            JLabel title = new JLabel(LanguageSetup.get(PickedLanguage, "age.verifyTitle"), SwingConstants.CENTER);
            title.setFont(new Font("Segoe UI", Font.BOLD, 28));
            content.add(title, BorderLayout.NORTH);
            String msgTemplate = LanguageSetup.get(PickedLanguage, "age.verifyMessage");
            JLabel message = new JLabel(
                    String.format("<html><div style='text-align:center;font-size:20px;'>%s</div></html>",
                            String.format(msgTemplate, p.getName())
                    ),
                    SwingConstants.CENTER
            );
            content.add(message, BorderLayout.CENTER);
            JLabel prompt = new JLabel(LanguageSetup.get(PickedLanguage, "age.verifyPrompt"), SwingConstants.CENTER);
            prompt.setFont(new Font("Segoe UI", Font.PLAIN, 18));
            content.add(prompt, BorderLayout.SOUTH);
            ageDialog.setContentPane(content);
            ageDialog.setSize(600, 300);
            ageDialog.setResizable(false);
            ageDialog.setLocationRelativeTo(this);
            ageDialog.setVisible(true);
            return;
        }
        kasaService.addByCodeOrTag(code);
        refreshBasketTable();
        layout.show(layoutPanel, "card3");
        layoutPanel.requestFocusInWindow();
    }

    private void initInactivityTimer() {
        inactivityTimer = new javax.swing.Timer(60_000, e -> showInactivityWarning());
        inactivityTimer.setRepeats(false);
    }
    private void resetInactivityTimer() {
        if (inactivityTimer == null) initInactivityTimer();
        inactivityTimer.setDelay(60_000);
        inactivityTimer.restart();
    }
    private void showInactivityWarning() {
        final JDialog dlg = new JDialog(this, LanguageSetup.get(PickedLanguage, "timer.noActivity"), true);
        dlg.setLayout(new BorderLayout(10,10));
        dlg.add(new JLabel(LanguageSetup.get(PickedLanguage, "timer.ask"), SwingConstants.CENTER), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton yes = new JButton(LanguageSetup.get(PickedLanguage, "timer.yes"));
        JButton no = new JButton(LanguageSetup.get(PickedLanguage, "timer.no"));
        btns.add(yes);
        btns.add(no);
        dlg.add(btns, BorderLayout.SOUTH);

        javax.swing.Timer dialogTimer = new javax.swing.Timer(60_000, ev -> {
            dlg.dispose();
            returnToStart();
        });
        dialogTimer.setRepeats(false);

        yes.addActionListener(ev -> {
            dialogTimer.stop();
            dlg.dispose();
            resetInactivityTimer();
        });
        no.addActionListener(ev -> {
            dialogTimer.stop();
            dlg.dispose();
            returnToStart();
        });

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dialogTimer.start();
        dlg.setVisible(true);
    }
    private void returnToStart() {
        kasaService.resetSession();
        loyaltyApplied = false;
        threadAlive = false;
        loyaltyCardButton.setEnabled(true);
        refreshBasketTable();
        inactivityTimer.stop();
        layout.show(layoutPanel, "card2");
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        layoutPanel = new javax.swing.JPanel();
        startingPanel = new javax.swing.JPanel();
        callHelpButton = new javax.swing.JButton();
        selectLanguageButton = new javax.swing.JButton();
        startCheckout = new javax.swing.JButton();
        basketPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        gotoPaymentButton = new javax.swing.JButton();
        gotoManualEntryButton = new javax.swing.JButton();
        callHelpButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        loyaltyCardButton = new javax.swing.JButton();
        manualProductEntryPanel = new javax.swing.JPanel();
        productCodeTextField = new javax.swing.JTextField();
        searchForProductButton = new javax.swing.JButton();
        productNameLabel = new javax.swing.JLabel();
        addProductManually = new javax.swing.JButton();
        backToBasketFromManualEntryButton = new javax.swing.JButton();
        paymentPanel = new javax.swing.JPanel();
        payByCashButton = new javax.swing.JButton();
        payByCardButton = new javax.swing.JButton();
        languageSelectionPanel = new javax.swing.JPanel();
        polishLanguageButton = new javax.swing.JButton();
        englishLanguageButton = new javax.swing.JButton();
        Dimension big = new Dimension(400, 400);
        selectLanguageButton.setPreferredSize(big);
        selectLanguageButton.setMaximumSize(big);
        selectLanguageButton.setMinimumSize(big);
        callHelpButton.setPreferredSize(big);
        callHelpButton.setMinimumSize(big);
        callHelpButton.setMaximumSize(big);
        pack();
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        layoutPanel.setBackground(AppTheme.SECONDARY_BACKGROUND);
        layoutPanel.setLayout(new java.awt.CardLayout());
        layoutPanel.setLayout(this.layout);

        callHelpButton.setText("Pomoc");
        callHelpButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        callHelpButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        callHelpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callHelpButtonActionPerformed(evt);
            }
        });

        selectLanguageButton.setText("Język");
        callHelpButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        callHelpButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectLanguageButtonActionPerformed(evt);
            }
        });

        startCheckout.setText("Rozpocznij Kasowanie Produktów");
        startCheckout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startCheckoutActionPerformed(evt);
            }
        });
        JPanel topRightPanel = new JPanel();
        topRightPanel.setLayout(new BoxLayout(topRightPanel, BoxLayout.Y_AXIS));
        topRightPanel.setOpaque(false);

        selectLanguageButton.setPreferredSize(new Dimension(200,200));
        selectLanguageButton.setMaximumSize(selectLanguageButton.getPreferredSize());
        callHelpButton     .setPreferredSize(new Dimension(200,200));
        callHelpButton     .setMaximumSize(callHelpButton.getPreferredSize());

        topRightPanel.add(selectLanguageButton);
        topRightPanel.add(Box.createVerticalStrut(10));
        topRightPanel.add(callHelpButton);
        javax.swing.GroupLayout startingPanelLayout = new javax.swing.GroupLayout(startingPanel);
        startingPanel.setLayout(startingPanelLayout);
        startingPanelLayout.setHorizontalGroup(
                startingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(startingPanelLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(topRightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                        .addGroup(startingPanelLayout.createSequentialGroup()
                                .addContainerGap(302, Short.MAX_VALUE)
                                .addComponent(startCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, 956, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(324, Short.MAX_VALUE))
        );
        startingPanelLayout.setVerticalGroup(
                startingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(startingPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(topRightPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                                .addComponent(startCheckout, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(160, Short.MAX_VALUE))
        );

        layoutPanel.add(startingPanel, "card2");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                },
                new String [] {
                        "Nazwa Produktu", "Ilość", "Cena jedn.", "Cena"
                }
        ) {
            boolean[] canEdit = new boolean[] { false, false, false, false };
            public boolean isCellEditable(int r, int c) { return canEdit[c]; }
        });
        jTable1.setRowHeight(40);
        jScrollPane1.setViewportView(jTable1);

        gotoPaymentButton.setText("Zapłać");
        gotoPaymentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoPaymentButtonActionPerformed(evt);
            }
        });

        gotoManualEntryButton.setText("Wprowadź produkt ręcznie");
        gotoManualEntryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoManualEntryButtonActionPerformed(evt);
            }
        });

        callHelpButton2.setText("Pomoc");
        callHelpButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setPreferredSize(AppTheme.BUTTON_SIZE);
        callHelpButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        selectLanguageButton.setMaximumSize(AppTheme.BUTTON_SIZE);
        callHelpButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                callHelpButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(204, 204, 204));

        jLabel2.setBackground(new java.awt.Color(204, 204, 204));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 36));
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("505250.50 PLN");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
                                .addContainerGap())
        );

        loyaltyCardButton.setText("jButton1");

        javax.swing.GroupLayout basketPanelLayout = new javax.swing.GroupLayout(basketPanel);
        basketPanel.setLayout(basketPanelLayout);
        basketPanelLayout.setHorizontalGroup(
                basketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basketPanelLayout.createSequentialGroup()
                                .addContainerGap(56, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                                .addGroup(basketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(gotoPaymentButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(gotoManualEntryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(loyaltyCardButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(80, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basketPanelLayout.createSequentialGroup()
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(callHelpButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );
        basketPanelLayout.setVerticalGroup(
                basketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(basketPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(callHelpButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(22, 22, 22)
                                .addGroup(basketPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(basketPanelLayout.createSequentialGroup()
                                                .addComponent(gotoManualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(33, 33, 33)
                                                .addComponent(loyaltyCardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(75, 75, 75)
                                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(43, 43, 43)
                                                .addComponent(gotoPaymentButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(55, 55, 55))
        );

        layoutPanel.add(basketPanel, "card3");
        beautifyBasketPanel();
        productCodeTextField.setText("Wprowadź kod produktu");
        productCodeTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                productCodeTextFieldFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                productCodeTextFieldFocusLost(evt);
            }
        });

        productCodeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                productCodeTextFieldActionPerformed(evt);
            }
        });

        searchForProductButton.setText("Wyszukaj");
        searchForProductButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchForProductButtonActionPerformed(evt);
            }
        });

        productNameLabel.setFont(new java.awt.Font("Segoe UI", 0, 18));
        productNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        addProductManually.setText("Dodaj produkt");
        addProductManually.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProductManuallyActionPerformed(evt);
            }
        });

        backToBasketFromManualEntryButton.setText("Cofnij");
        backToBasketFromManualEntryButton.setMaximumSize(new java.awt.Dimension(80, 80));
        backToBasketFromManualEntryButton.setMinimumSize(new java.awt.Dimension(80, 80));
        backToBasketFromManualEntryButton.setPreferredSize(new java.awt.Dimension(80, 80));
        backToBasketFromManualEntryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backToBasketFromManualEntryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout manualProductEntryPanelLayout = new javax.swing.GroupLayout(manualProductEntryPanel);
        manualProductEntryPanel.setLayout(manualProductEntryPanelLayout);
        manualProductEntryPanelLayout.setHorizontalGroup(
                manualProductEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(manualProductEntryPanelLayout.createSequentialGroup()
                                .addGroup(manualProductEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualProductEntryPanelLayout.createSequentialGroup()
                                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(backToBasketFromManualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(manualProductEntryPanelLayout.createSequentialGroup()
                                                .addContainerGap(277, Short.MAX_VALUE)
                                                .addGroup(manualProductEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(productNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(productCodeTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE))
                                                .addGap(18, 18, 18)
                                                .addComponent(searchForProductButton)
                                                .addGap(0, 228, Short.MAX_VALUE)))
                                .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, manualProductEntryPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(addProductManually, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        manualProductEntryPanelLayout.setVerticalGroup(
                manualProductEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(manualProductEntryPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(backToBasketFromManualEntryButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40)
                                .addGroup(manualProductEntryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(searchForProductButton, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                                        .addComponent(productCodeTextField))
                                .addGap(40, 40, 40)
                                .addComponent(productNameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(124, 124, 124)
                                .addComponent(addProductManually, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(162, Short.MAX_VALUE))
        );

        layoutPanel.add(manualProductEntryPanel, "card4");

        payByCashButton.setText("Zapłać gotówką");
        payByCashButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payByCashButtonActionPerformed(evt);
            }
        });

        payByCardButton.setText("Zapłać kartą");
        payByCardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payByCardButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout paymentPanelLayout = new javax.swing.GroupLayout(paymentPanel);
        paymentPanel.setLayout(paymentPanelLayout);
        paymentPanelLayout.setHorizontalGroup(
                paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(paymentPanelLayout.createSequentialGroup()
                                .addContainerGap(301, Short.MAX_VALUE)
                                .addComponent(payByCashButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(155, 155, 155)
                                .addComponent(payByCardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(328, Short.MAX_VALUE))
        );
        paymentPanelLayout.setVerticalGroup(
                paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(paymentPanelLayout.createSequentialGroup()
                                .addContainerGap(237, Short.MAX_VALUE)
                                .addGroup(paymentPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(payByCashButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(payByCardButton, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(242, Short.MAX_VALUE))
        );

        layoutPanel.add(paymentPanel, "card4");

        polishLanguageButton.setText("Polski");
        polishLanguageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                polishLanguageButtonActionPerformed(evt);
            }
        });

        englishLanguageButton.setText("Angielski");
        englishLanguageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                englishLanguageButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout languageSelectionPanelLayout = new javax.swing.GroupLayout(languageSelectionPanel);
        languageSelectionPanel.setLayout(languageSelectionPanelLayout);
        languageSelectionPanelLayout.setHorizontalGroup(
                languageSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(languageSelectionPanelLayout.createSequentialGroup()
                                .addContainerGap(354, Short.MAX_VALUE)
                                .addComponent(polishLanguageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(128, 128, 128)
                                .addComponent(englishLanguageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(342, Short.MAX_VALUE))
        );
        languageSelectionPanelLayout.setVerticalGroup(
                languageSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(languageSelectionPanelLayout.createSequentialGroup()
                                .addContainerGap(259, Short.MAX_VALUE)
                                .addGroup(languageSelectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(polishLanguageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(englishLanguageButton, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(300, Short.MAX_VALUE))
        );

        layoutPanel.add(languageSelectionPanel, "card5");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(layoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(layoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    private void beautifyBasketPanel() {
        Color borderColor = gotoManualEntryButton.getBackground();

        jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setBorder(null);
        jScrollPane1.getViewport().setOpaque(false);

        jTable1.setBorder(null);
        jTable1.setShowGrid(false);
        jTable1.setIntercellSpacing(new Dimension(20, 0));
        jTable1.setRowHeight(80);
        jTable1.getTableHeader().setVisible(false);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.setEnabled(false);
        jTable1.setFocusable(false);

        Font bigFont = new Font("Segoe UI", Font.BOLD, 36);
        jTable1.setFont(bigFont);

        DefaultTableCellRenderer nameRenderer = new DefaultTableCellRenderer();
        nameRenderer.setFont(bigFont);
        nameRenderer.setOpaque(false);
        nameRenderer.setForeground(new Color(0x2B2B2B));
        nameRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        Font qtyFont = new Font("Segoe UI", Font.PLAIN, 24);
        DefaultTableCellRenderer qtyRenderer = new DefaultTableCellRenderer();
        qtyRenderer.setFont(qtyFont);
        qtyRenderer.setOpaque(false);
        qtyRenderer.setForeground(new Color(0x2B2B2B));
        qtyRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                super.setValue(value == null ? "" : value.toString());
            }
        };
        priceRenderer.setFont(bigFont);
        priceRenderer.setOpaque(false);
        priceRenderer.setForeground(new Color(0x2B2B2B));
        priceRenderer.setHorizontalAlignment(SwingConstants.RIGHT);

        TableColumnModel cm = jTable1.getColumnModel();
        cm.getColumn(0).setCellRenderer(nameRenderer);
        cm.getColumn(1).setCellRenderer(qtyRenderer);
        cm.getColumn(2).setCellRenderer(priceRenderer);
        cm.getColumn(3).setCellRenderer(priceRenderer);

        tablePanel = new JPanel(new BorderLayout());
        Border inset   = BorderFactory.createEmptyBorder(0, 20, 10, 20);
        Border outline = BorderFactory.createLineBorder(borderColor, 12, true);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(inset, outline));
        tablePanel.add(jScrollPane1, BorderLayout.CENTER);

        jLabel2.setFont(bigFont);
        sumPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sumPanel.setOpaque(false);
        sumPanel.setPreferredSize(new Dimension(200, jTable1.getRowHeight()));
        sumPanel.add(jLabel2);

        modifyCartButton = new JButton(LanguageSetup.get(PickedLanguage, "cart.modify"));
        modifyCartButton.setPreferredSize(new Dimension(300, 80));
        modifyCartButton.setFont(new Font("Segoe UI", Font.BOLD, 24));
        modifyCartButton.addActionListener(ev -> {
            awaitingModifyAuth = true;
            modifyAuthDialog = showStaffAuthDialog();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, borderColor));
        bottom.add(modifyCartButton, BorderLayout.WEST);
        bottom.add(sumPanel,         BorderLayout.EAST);
        tablePanel.add(bottom,       BorderLayout.SOUTH);

        tablePanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int w = tablePanel.getWidth();
                cm.getColumn(0).setPreferredWidth((int)(w * 0.55));
                cm.getColumn(1).setPreferredWidth((int)(w * 0.06));
                cm.getColumn(2).setPreferredWidth((int)(w * 0.185));
                cm.getColumn(3).setPreferredWidth((int)(w * 0.185));
            }
        });

        Dimension btnSize = new Dimension(300,200);
        for (JButton b : new JButton[]{gotoManualEntryButton, loyaltyCardButton, gotoPaymentButton, callHelpButton2}) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setMinimumSize(btnSize);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        JPanel controls = new JPanel();
        controls.setOpaque(false);
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(Box.createVerticalStrut(50));
        controls.add(gotoManualEntryButton);
        controls.add(Box.createVerticalStrut(40));
        controls.add(loyaltyCardButton);
        controls.add(Box.createVerticalStrut(40));
        controls.add(gotoPaymentButton);
        controls.add(Box.createVerticalGlue());
        controls.add(Box.createVerticalStrut(40));
        controls.add(callHelpButton2);
        controls.add(Box.createVerticalStrut(50));

        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, controls);
        sp.setResizeWeight(0.75);
        sp.setDividerLocation(0.75);
        sp.setDividerSize(0);
        sp.setEnabled(false);
        sp.setBorder(null);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));
        centerWrapper.add(sp, BorderLayout.CENTER);

        basketPanel.removeAll();
        basketPanel.setLayout(new BorderLayout());
        basketPanel.add(centerWrapper, BorderLayout.CENTER);
        basketPanel.revalidate();
        basketPanel.repaint();
    }

    //GEN-LAST:event_beautifyBasketPanel
    private void callHelpButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_callHelpButtonActionPerformed
        callHelpFunction();
    }//GEN-LAST:event_callHelpButtonActionPerformed
    private void callHelpButton2ActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_callHelpButton2ActionPerformed
        callHelpFunction();
    }//GEN-LAST:event_callHelpButton2ActionPerformed
    private void gotoManualEntryButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_gotoManualEntryButtonActionPerformed
        System.out.println(LanguageSetup.get(PickedLanguage, "goTo.manual"));
        refreshBasketTable();
        layout.show(layoutPanel, "cardSearch");

    }//GEN-LAST:event_gotoManualEntryButtonActionPerformed
    private void productCodeTextFieldFocusGained(FocusEvent evt) {
        //GEN-FIRST:event_productCodeTextFieldFocusGained
        if (productCodeTextField.getText().equals(LanguageSetup.get(PickedLanguage,"input.code")))
            productCodeTextField.setText("");
    }//GEN-LAST:event_productCodeTextFieldFocusGained
    private void productCodeTextFieldFocusLost(FocusEvent evt) {
        //GEN-FIRST:event_productCodeTextFieldFocusLost
        if (productCodeTextField.getText().isEmpty())
            productCodeTextField.setText(LanguageSetup.get(PickedLanguage,"input.code"));
    }//GEN-LAST:event_productCodeTextFieldFocusLost
    private void productCodeTextFieldActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_productCodeTextFieldActionPerformed
        addProductManuallyActionPerformed(evt);
    }//GEN-LAST:event_productCodeTextFieldActionPerformed
    private void startCheckoutActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_startCheckoutActionPerformed
        kasaService.resetSession();
        loyaltyApplied = false;
        loyaltyCardButton.setEnabled(true);
        refreshBasketTable();
        resetInactivityTimer();
        layout.show(layoutPanel, "card3");
        if (reader != null && (nfcThread == null || !nfcThread.isAlive())) {
            threadAlive = true;
            nfcThread = new Thread(() -> {
                while (threadAlive) {
                    try {
                        String raw = reader.readTextRecord().trim();
                        String digits = raw.replaceFirst("(?i)^n", "");
                        if (!digits.matches("\\d+")) continue;
                        String tag = "NFC" + digits;
                        SwingUtilities.invokeLater(() -> {
                            try {
                                handleScan(tag);
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(
                                        this,
                                        LanguageSetup.get(PickedLanguage, "NFCScanner.error") + ex.getMessage(),
                                        LanguageSetup.get(PickedLanguage, "error"),
                                        JOptionPane.ERROR_MESSAGE
                                );
                            }
                        });
                    } catch (Exception ignored) {}
                }
            }, "NFC-Scanner-Thread");
            nfcThread.setDaemon(true);
            nfcThread.start();
        }
    }//GEN-LAST:event_startCheckoutActionPerformed
    private void searchForProductButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_searchForProductButtonActionPerformed
        String partial = productCodeTextField.getText().trim();
        if (partial.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageSetup.get(PickedLanguage, "write.barCode"));
            return;
        }
        List<Produkt> wyniki = kasaService.findByPartialCode(partial);
        if (wyniki.isEmpty()) {
            JOptionPane.showMessageDialog(this, LanguageSetup.get(PickedLanguage, "no.results"));
            return;
        }
        Produkt selected = (Produkt) JOptionPane.showInputDialog(
                this,
                LanguageSetup.get(PickedLanguage, "search.choose"),
                LanguageSetup.get(PickedLanguage, "search.title"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                wyniki.toArray(),
                wyniki.get(0)
        );
        if (selected != null) {
            handleScan(selected.getBarCode());
            refreshBasketTable();
            JOptionPane.showMessageDialog(
                    this,
                    LanguageSetup.get(PickedLanguage, "added.product") + selected.getName(),
                    LanguageSetup.get(PickedLanguage, "info"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            layout.show(layoutPanel, "card3");
        }
    }//GEN-LAST:event_searchForProductButtonActionPerformed
    private void addProductManuallyActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_addProductManuallyActionPerformed
        String code = productCodeTextField.getText().trim();
        try {
            handleScan(code);
            JOptionPane.showMessageDialog(this, LanguageSetup.get(PickedLanguage, "added.product") + code);
            productCodeTextField.setText("");
            refreshBasketTable();
            layout.show(layoutPanel, "card3");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-FIRST:event_addProductManuallyActionPerformed
    private void gotoPaymentButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_gotoPaymentButtonActionPerformed
        layout.show(layoutPanel, "card6");
    }//GEN-LAST:event_gotoPaymentButtonActionPerformed
    private void payByCashButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_payByCashButtonActionPerformed
        try {
            Transakcja tx = kasaService.finalizeTransaction(
                    LanguageSetup.get(PickedLanguage, "cash")
            );
            String nip = NumericInputDialog.showNumericDialog(
                    this,
                    PickedLanguage,
                    "nip"
            );


            if (nip != null && !nip.isBlank()) {
                tx.setNip(nip);
            }
            ReceiptGenerator.generateAndSaveReceipt(
                    this, tx, PickedLanguage, kasaService
            );

            kasaService.resetSession();
            loyaltyApplied = false;
            loyaltyCardButton.setEnabled(true);
            refreshBasketTable();
            layout.show(layoutPanel, "card2");

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    LanguageSetup.get(PickedLanguage, "empty.cart"),
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_payByCashButtonActionPerformed
    private void payByCardButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_payByCardButtonActionPerformed
        try {
            Transakcja tx = kasaService.finalizeTransaction(
                    LanguageSetup.get(PickedLanguage, "card")
            );
            String nip = NumericInputDialog.showNumericDialog(
                    this,
                    PickedLanguage,
                    "nip"
            );


            if (nip != null && !nip.isBlank()) {
                tx.setNip(nip);
            }
            ReceiptGenerator.generateAndSaveReceipt(
                    this, tx, PickedLanguage, kasaService
            );

            kasaService.resetSession();
            loyaltyApplied = false;
            loyaltyCardButton.setEnabled(true);
            refreshBasketTable();

            layout.show(layoutPanel, "card2");
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    LanguageSetup.get(PickedLanguage, "empty.cart"),
                    "Błąd",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_payByCardButtonActionPerformed
    private void backToBasketFromManualEntryButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_backToBasketFromManualEntryButtonActionPerformed
        refreshBasketTable();
        layout.show(layoutPanel, "card3");
    }//GEN-LAST:event_backToBasketFromManualEntryButtonActionPerformed
    private void selectLanguageButtonActionPerformed(ActionEvent evt) {
        //GEN-FIRST:event_selectLanguageButtonActionPerformed
        layout.show(layoutPanel, "card5");
    }//GEN-LAST:event_selectLanguageButtonActionPerformed
    private void polishLanguageButtonActionPerformed(ActionEvent evt) {
        PickedLanguage = "pl";
        updateTexts();
        rebuildKeyboard();
        layout.show(layoutPanel, "card2");
    }
    private void englishLanguageButtonActionPerformed(ActionEvent evt) {
        PickedLanguage = "en";
        updateTexts();
        rebuildKeyboard();
        layout.show(layoutPanel, "card2");
    }
    private void rebuildKeyboard() {
        manualProductEntryPanel.remove(kb);
        kb = new VirtualKeyboardPanel(productCodeTextField, true, PickedLanguage);
        manualProductEntryPanel.add(kb, BorderLayout.SOUTH);
        manualProductEntryPanel.revalidate();
        manualProductEntryPanel.repaint();
    }
    private void callHelpFunction() {
        //GEN-FIRST:event_callHelpFunction
        JOptionPane.showMessageDialog(
                paymentPanel,
                LanguageSetup.get(PickedLanguage, "alarm.help.info"),
                LanguageSetup.get(PickedLanguage, "alarm.help"),
                JOptionPane.INFORMATION_MESSAGE
        );
    }//GEN-LAST:event_callHelpFunction
    private void refreshBasketTable() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        BigDecimal suma = BigDecimal.ZERO;
        basketOrderCodes.clear();

        Map<String,Integer> ilosci = new LinkedHashMap<>();
        Map<String,Produkt> produktyMap = new LinkedHashMap<>();
        for (Produkt p : kasaService.getKoszyk()) {
            ilosci.merge(p.getBarCode(), 1, Integer::sum);
            produktyMap.putIfAbsent(p.getBarCode(), p);
        }

        for (var e : ilosci.entrySet()) {
            Produkt p = produktyMap.get(e.getKey());
            int qty = e.getValue();
            BigDecimal unitPrice = kasaService.getPriceWithDiscount(p);
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(qty));
            suma = suma.add(totalPrice);

            String qtyStr = qty + "×";
            String unitStr = unitPrice.setScale(2, RoundingMode.HALF_UP).toPlainString() + " PLN";
            String totalStr = totalPrice.setScale(2, RoundingMode.HALF_UP).toPlainString() + " PLN";

            model.addRow(new Object[]{ p.getName(), qtyStr, unitStr, totalStr });
            basketOrderCodes.add(p.getBarCode());
        }

        jLabel2.setText(suma.setScale(2, RoundingMode.HALF_UP).toPlainString() + " PLN");
        tablePanel.revalidate();
        tablePanel.repaint();
        resetInactivityTimer();
        boolean hasItems = !basketOrderCodes.isEmpty();
        gotoPaymentButton.setEnabled(hasItems);
    }


    private void showModifyCartDialog() {
        JDialog dlg = new JDialog(this,
                LanguageSetup.get(PickedLanguage, "cart.modifyTitle"),
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        Map<String,Integer> localCounts = new LinkedHashMap<>();
        for (Produkt p : kasaService.getKoszyk()) {
            localCounts.merge(p.getBarCode(), 1, Integer::sum);
        }

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        Map<String, JLabel> labelMap = new HashMap<>();

        for (var entry : localCounts.entrySet()) {
            String code = entry.getKey();
            Produkt p = produktDao.findById(code);
            int qty = entry.getValue();

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            JLabel name = new JLabel(p.getName() + "  [");
            JLabel qtyLabel = new JLabel(String.valueOf(qty));
            JLabel closingBracket = new JLabel("]");
            name.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            qtyLabel .setFont(new Font("Segoe UI", Font.BOLD, 20));
            labelMap.put(code, qtyLabel);

            JButton minus = new JButton("–");
            JButton plus  = new JButton("+");
            Dimension bsz = new Dimension(70,70);
            minus.setPreferredSize(bsz);
            plus.setPreferredSize(bsz);

            minus.addActionListener(ev -> {
                int current = localCounts.get(code);
                if (current > 0) {
                    localCounts.put(code, current - 1);
                    qtyLabel.setText(String.valueOf(current - 1));
                }
            });
            plus.addActionListener(ev -> {
                int current = localCounts.getOrDefault(code, 0);
                int val = Math.min(current + 1, produktDao.findById(code).getQuantity());
                localCounts.put(code, val);
                qtyLabel.setText(String.valueOf(val));
            });

            row.add(name);
            row.add(qtyLabel);
            row.add(closingBracket);
            row.add(minus);
            row.add(plus);
            list.add(row);
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        dlg.getContentPane().add(sp, BorderLayout.CENTER);

        JButton close = new JButton("Zamknij");
        close.setPreferredSize(new Dimension(120, 40));
        close.addActionListener(e -> {

            for (var ent : localCounts.entrySet()) {
                kasaService.changeQuantityByCode(ent.getKey(), ent.getValue());
            }
            refreshBasketTable();
            dlg.dispose();
        });
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.add(close);
        dlg.getContentPane().add(footer, BorderLayout.SOUTH);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    private JDialog showStaffAuthDialog() {
        JDialog dlg = new JDialog(this,
                LanguageSetup.get(PickedLanguage, "cart.modifyTitle"),
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setUndecorated(true);
        dlg.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(20,20));
        content.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        content.setBackground(Color.WHITE);

        JLabel title = new JLabel(
                LanguageSetup.get(PickedLanguage, "cart.modifyTitle"),
                SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        content.add(title, BorderLayout.NORTH);

        JLabel message = new JLabel(
                String.format(
                        "<html><div style='text-align:center;font-size:20px;'>%s</div></html>",
                        LanguageSetup.get(PickedLanguage, "cart.modifyPrompt")
                ),
                SwingConstants.CENTER);
        content.add(message, BorderLayout.CENTER);

        dlg.setContentPane(content);
        dlg.setSize(600, 300);
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(this);
        modifyAuthDialog = dlg;
        dlg.setVisible(true);
        return dlg;

    }




    private void updateTexts() {
        //GEN-FIRST:event_updateTexts
        callHelpButton.setText(LanguageSetup.get(PickedLanguage, "menu.help"));
        callHelpButton2.setText(LanguageSetup.get(PickedLanguage, "menu.help"));
        selectLanguageButton.setText(LanguageSetup.get(PickedLanguage, "menu.language"));
        startCheckout.setText(LanguageSetup.get(PickedLanguage, "start.scan"));
        gotoManualEntryButton.setText(LanguageSetup.get(PickedLanguage, "cart.manual"));
        gotoPaymentButton.setText(LanguageSetup.get(PickedLanguage, "cart.checkout"));
        backToBasketFromManualEntryButton.setText(LanguageSetup.get(PickedLanguage, "cart.back"));
        productCodeTextField.setText(LanguageSetup.get(PickedLanguage, "input.code"));
        searchForProductButton.setText(LanguageSetup.get(PickedLanguage, "search.find"));
        addProductManually.setText(LanguageSetup.get(PickedLanguage, "cart.addManual"));
        payByCashButton.setText(LanguageSetup.get(PickedLanguage, "payment.cash"));
        payByCardButton.setText(LanguageSetup.get(PickedLanguage, "payment.card"));
        polishLanguageButton.setText(LanguageSetup.get(PickedLanguage, "language.pl"));
        englishLanguageButton.setText(LanguageSetup.get(PickedLanguage, "language.en"));
        loyaltyCardButton.setText(LanguageSetup.get(PickedLanguage, "loyalty.card"));
        printReportButton.setText(LanguageSetup.get(PickedLanguage, "report.print"));
        modifyCartButton.setText(LanguageSetup.get(PickedLanguage, "cart.modify"));
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setColumnIdentifiers(new String[]{
                LanguageSetup.get(PickedLanguage, "column.productName"),
                LanguageSetup.get(PickedLanguage, "column.quantity"),
                LanguageSetup.get(PickedLanguage, "column.unitPrice"),
                LanguageSetup.get(PickedLanguage, "column.price")
        });
    }//GEN-LAST:event_updateTexts

    // Variables declaration - do not modify
    private javax.swing.JButton addProductManually;
    private javax.swing.JButton backToBasketFromManualEntryButton;
    private javax.swing.JPanel basketPanel;
    private javax.swing.JButton callHelpButton;
    private javax.swing.JButton callHelpButton2;
    private javax.swing.JButton englishLanguageButton;
    private javax.swing.JButton gotoManualEntryButton;
    private javax.swing.JButton gotoPaymentButton;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel languageSelectionPanel;
    private javax.swing.JPanel layoutPanel;
    private javax.swing.JButton loyaltyCardButton;
    private javax.swing.JPanel manualProductEntryPanel;
    private javax.swing.JButton payByCardButton;
    private javax.swing.JButton payByCashButton;
    private javax.swing.JPanel paymentPanel;
    private javax.swing.JButton polishLanguageButton;
    private javax.swing.JTextField productCodeTextField;
    private javax.swing.JLabel productNameLabel;
    private javax.swing.JButton searchForProductButton;
    private javax.swing.JButton selectLanguageButton;
    private javax.swing.JButton startCheckout;
    private javax.swing.JPanel startingPanel;
    // End of variables declaration
}