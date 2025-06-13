package ppacocha.kasasamoobslugowa.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JList;
import javax.swing.JTextField;
import java.awt.Frame;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class NumericInputDialogTest {
    private NumericInputDialog dlg;

    @BeforeEach
    void setUp() throws Exception {
        Frame owner = new Frame();
        Constructor<NumericInputDialog> ctor =
                NumericInputDialog.class.getDeclaredConstructor(Frame.class, String.class, String.class, boolean.class);
        ctor.setAccessible(true);
        dlg = ctor.newInstance(owner, "pl", "nip", false);
    }

    @Test
    void onKey_digitsBackspaceAndClear() throws Exception {
        Method onKey = NumericInputDialog.class.getDeclaredMethod("onKey", String.class);
        onKey.setAccessible(true);
        JTextField tf = getField(dlg, "tf", JTextField.class);
        onKey.invoke(dlg, "1");
        onKey.invoke(dlg, "2");
        onKey.invoke(dlg, "3");
        assertEquals("123", tf.getText());
        onKey.invoke(dlg, "\u2190");
        assertEquals("12", tf.getText());
        onKey.invoke(dlg, "CLR");
        assertEquals("", tf.getText());
    }

    @Test
    void loyaltyMode_initialPrefixIsPoland() throws Exception {
        Frame owner = new Frame();
        Constructor<NumericInputDialog> ctor = NumericInputDialog.class.getDeclaredConstructor(Frame.class, String.class, String.class, boolean.class);
        ctor.setAccessible(true);
        NumericInputDialog loy = ctor.newInstance(owner, "pl", "loyalty", true);
        @SuppressWarnings("unchecked")
        JList<Object> list = getField(loy, "prefixList", JList.class);
        Object country = list.getSelectedValue();
        Field code = country.getClass().getDeclaredField("code");
        code.setAccessible(true);
        assertEquals("+48", code.get(country));
    }

    private <T> T getField(Object o, String name, Class<T> type) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return type.cast(f.get(o));
    }
}
