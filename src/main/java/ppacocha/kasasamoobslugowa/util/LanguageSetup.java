
package ppacocha.kasasamoobslugowa.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LanguageSetup {
    private static final Map<String, String> pl = new HashMap<>();
    private static final Map<String, String> en = new HashMap<>();
    private static String currentLanguage = "pl";
    
    static{
    
    // Język Polski
        pl.put("app.title",                  "Kasa Samoobsługowa");
        pl.put("start.scan",                 "Rozpocznij Kasowanie Produktów");
        pl.put("start.scan.console",         "Zaczynam kasowanie");
        pl.put("menu.help",                  "Pomoc");
        pl.put("menu.language",              "Język");
        pl.put("cart.manual",                "Wpisz kod produktu");
        pl.put("cart.checkout",              "Zapłać");
        pl.put("cart.back",                  "Powrót");
        pl.put("input.code",                 "Wprowadź kod produktu");
        pl.put("search.find",                "Wyszukaj");
        pl.put("cart.addManual",             "Dodaj produkt");
        pl.put("payment.cash",               "Zaplac gotowka");
        pl.put("payment.card",               "Zaplac karta");
        pl.put("language.pl",                "Polski");
        pl.put("language.en",                "Angielski");
        pl.put("column.productName",         "Nazwa Produktu");
        pl.put("column.quantity",            "Ilosc");
        pl.put("column.price",               "Cena");
        pl.put("added.product",              "Dodano produkt: ");
        pl.put("write.barCode",              "Wpisz fragment kodu kreskowego.");
        pl.put("back.cart",                   "Cofam do panelu koszyka");
        pl.put("goTo.manual",                 "Przechodze do panelu manualnego dodawania produktu");
        pl.put("no.results",                   "Brak wyników");
        pl.put("search.choose",                "Wybierz produkt:");
        pl.put("search.title",                "Wyniki wyszukiwania");
        pl.put("info",                        "Informacja");
        pl.put("goTo.payment",                "Przechodzę do płatności");
        pl.put("paid.cash",                    "Płacę gotówką");
        pl.put("cash",                         "Gotówka");
        pl.put("empty.cart",                   "Koszyk jest pusty!");
        pl.put("paid.card",                    "Płace kartą");
        pl.put("card",                         "Karta");
        pl.put("alarm.help",                   "Wezwano pomoc");
        pl.put("alarm.help.info",              "Wezwano pomoc - proszę czekać na obsługę");
        pl.put("goTo.languagePanel",           "Przechodzę do wyboru języka");
        pl.put("nip",                          "Wprowadź NIP nabywcy (opcjonalnie): ");
    // Komunikaty NFC i błędów
        pl.put("NFCScanner.connection.error", "Nie można uruchomić czytnika NFC");
        pl.put("NFCScanner.error",            "Błąd NFC: ");
        pl.put("NFCScan.confirmation",        "Zeskanowano NFC: ");
        pl.put("error",                       "Błąd");
        
    }
    //Język Angielski
    static{
    
    // English
        en.put("app.title",                  "Self-Checkout");
        en.put("start.scan",                 "Start Checkout");
        en.put("start.scan.console",         "Starting checkout");
        en.put("menu.help",                  "Help");
        en.put("menu.language",              "Language");
        en.put("cart.manual",                "Enter product code");
        en.put("cart.checkout",              "Pay");
        en.put("cart.back",                  "Back");
        en.put("input.code",                 "Enter product code");
        en.put("search.find",                "Search");
        en.put("cart.addManual",             "Add product");
        en.put("payment.cash",               "Pay by Cash");
        en.put("payment.card",               "Pay by Card");
        en.put("language.pl",                "Polish");
        en.put("language.en",                "English");
        en.put("column.productName",         "Product Name");
        en.put("column.quantity",            "Quantity");
        en.put("column.price",               "Price");
        en.put("added.product",              "Added product: ");
        en.put("back.cart",                  "Back to cart panel");
        en.put("goTo.manual",                "Going back to manual product adding panel");
        en.put("write.barCode",              "Write bar code.");
        en.put("no.results",                  "No results");
        en.put("search.choose",                "Choose product:");
        en.put("search.title",                "Search results");
        en.put("info",                        "Information");
        en.put("goTo.payment",                "Going to payment options");
        en.put("paid.cash",                    "I'm paying cash");
        en.put("cash",                         "Cash");
        en.put("empty.cart",                   "Cart is empty!");
        en.put("paid.card",                    "I'm paying card");
        en.put("card",                         "Card");
        en.put("alarm.help",                   "Help was called");
        en.put("alarm.help.info",              "Help has been called - please wait for service");
        en.put("goTo.languagePanel",           "Going to choose a language panel");
        en.put("nip",                          "Enter the buyer's NIP (optional): ");
    // NFC & error messages
        en.put("NFCScanner.connection.error", "Cannot initialize NFC reader");
        en.put("NFCScanner.error",            "NFC Error");
        en.put("NFCScan.confirmation",        "NFC scanned: ");
        en.put("error",                       "Error");

    
            
    }
    
    public static String get(String PickedLanguage, String key){
        if(PickedLanguage.equals("pl")){
            return pl.get(key);
        }else{
            return en.get(key);
        }
    }
    
}
