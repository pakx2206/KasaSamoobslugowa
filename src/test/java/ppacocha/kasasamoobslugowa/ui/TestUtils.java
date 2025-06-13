package ppacocha.kasasamoobslugowa.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class TestUtils {
    @SuppressWarnings("unchecked")
    static <T extends Component> T findComponent(Container root, Class<T> cls) {
        for (Component c : root.getComponents()) {
            if (cls.isInstance(c)) return (T)c;
            if (c instanceof Container) {
                T child = findComponent((Container)c, cls);
                if (child != null) return child;
            }
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    static <T extends Component> T[] findAllComponents(Container root, Class<T> cls) {
        List<T> list = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (cls.isInstance(c)) list.add((T)c);
            if (c instanceof Container) {
                T[] inner = findAllComponents((Container)c, cls);
                for (T t : inner) list.add(t);
            }
        }
        return list.toArray((T[])java.lang.reflect.Array.newInstance(cls, 0));
    }
}
