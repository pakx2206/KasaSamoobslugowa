package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class VirtualKeyboardPanelTest {

    @Test
    void constructKeyboard_hasButtons() {
        JTextField tf = new JTextField();
        VirtualKeyboardPanel k = new VirtualKeyboardPanel(tf,true,"pl");
        assertNotNull(k);
        JButton[] buttons = TestUtils.findAllComponents(k, JButton.class);
        assertTrue(buttons.length > 0);
    }
    @Test
    void fullLayout_containsDigitKeys() {
        JTextField tf = new JTextField();
        var vk = new VirtualKeyboardPanel(tf,true,"pl");
        boolean found5 = false;
        for(var comp: vk.getComponents()) {
            if(comp instanceof JButton && "5".equals(((JButton)comp).getText()))
                found5 = true;
        }
        assertTrue(found5);
    }

    @Test
    void pressingKey_appendsToTargetField() {
        JTextField tf = new JTextField();
        var vk = new VirtualKeyboardPanel(tf,true,"pl");
        for(var comp: vk.getComponents()) {
            if(comp instanceof JButton && "7".equals(((JButton)comp).getText())) {
                ((JButton)comp).doClick();
                break;
            }
        }
        assertEquals("7", tf.getText());
    }
    @Test
    void containsSomeButtons() {
        JTextField tf = new JTextField();
        VirtualKeyboardPanel vk = new VirtualKeyboardPanel(tf,true,"en");
        Component[] comps = vk.getComponents();
        boolean hasButton = false;
        for (Component c : comps) {
            if (c instanceof JButton) {
                hasButton = true;
                break;
            }
        }
        assertTrue(hasButton,"Keyboard panel powinien zawierać przynajmniej jeden JButton");
    }
}
