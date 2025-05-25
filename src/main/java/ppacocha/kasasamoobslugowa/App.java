package ppacocha.kasasamoobslugowa;

import javax.swing.SwingUtilities;
import ppacocha.kasasamoobslugowa.ui.AppFrame;

public class App {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.class.path"));

        SwingUtilities.invokeLater(() -> new AppFrame().setVisible(true));
    }
}
