package ppacocha.kasasamoobslugowa.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageSetupTest {

    @Test
    void polish_startScan() {
        String v = LanguageSetup.get("pl","start.scan");
        assertEquals("Rozpocznij Kasowanie Produktów", v);
    }

    @Test
    void english_startScan() {
        String v = LanguageSetup.get("en","start.scan");
        assertEquals("Start Checkout", v);
    }

    @Test
    void polish_menuHelp() {
        assertEquals("Pomoc", LanguageSetup.get("pl","menu.help"));
    }

    @Test
    void english_menuHelp() {
        assertEquals("Help", LanguageSetup.get("en","menu.help"));
    }

    @Test
    void format_ageVerifyMessage_polish() {
        String template = LanguageSetup.get("pl","age.verifyMessage");
        String msg = String.format(template, "Piwo");
        assertTrue(msg.contains("Piwo") && msg.endsWith("(+18)"));
    }

    @Test
    void format_ageVerifyMessage_english() {
        String template = LanguageSetup.get("en","age.verifyMessage");
        String msg = String.format(template, "Beer");
        assertTrue(msg.contains("Beer") && msg.endsWith("(+18)"));
    }

    @Test
    void missingKeyReturnsNull() {
        assertNull(LanguageSetup.get("pl","__BRAK_TAKIEGO_KLUCZA__"));
        assertNull(LanguageSetup.get("en","__BRAK_TAKIEGO_KLUCZA__"));
    }

    @Test
    void unsupportedLanguageDefaultsToEnglish() {
        assertEquals(LanguageSetup.get("en","menu.help"),
                LanguageSetup.get("de","menu.help"));
    }
}
