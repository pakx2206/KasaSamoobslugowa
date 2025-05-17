package ppacocha.kasasamoobslugowa;

import javax.swing.SwingUtilities;
import ppacocha.kasasamoobslugowa.ui.AppFrame;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}
