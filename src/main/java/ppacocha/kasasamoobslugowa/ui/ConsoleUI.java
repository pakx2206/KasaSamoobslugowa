package ppacocha.kasasamoobslugowa.ui;

import ppacocha.kasasamoobslugowa.service.KasaService;
import ppacocha.kasasamoobslugowa.service.RaportService;
import ppacocha.kasasamoobslugowa.util.ParagonGenerator;
import ppacocha.kasasamoobslugowa.model.Produkt;
import ppacocha.kasasamoobslugowa.model.Transakcja;
import java.util.Scanner;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class ConsoleUI {
    private final KasaService kasaService = new KasaService();
    private final RaportService raportService = new RaportService();
    private final Scanner scanner = new Scanner(System.in);
    private final List<Transakcja> historiaTransakcji = new ArrayList<>();

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("=== KASA MENU ===");
            System.out.println("1. Dodaj produkt");
            System.out.println("2. Usuń produkt");
            System.out.println("3. Zmień ilość produktu");
            System.out.println("4. Wyświetl koszyk");
            System.out.println("5. Finalizuj transakcję");
            System.out.println("6. Generuj paragon");
            System.out.println("7. Generuj raport dzienny");
            System.out.println("0. Wyjście");
            System.out.print("Wybierz opcję: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Nieprawidłowy numer opcji.");
                continue;
            }

            switch (choice) {
                case 1 -> addProduct();
                case 2 -> removeProduct();
                case 3 -> changeQuantity();
                case 4 -> viewCart();
                case 5 -> checkout();
                case 6 -> printReceipt();
                case 7 -> printDailyReport();
                case 0 -> running = false;
                default -> System.out.println("Nieprawidłowy wybór, spróbuj ponownie.");
            }
        }
        System.out.println("Koniec programu.");
    }

    private void addProduct() {
        System.out.print("Nazwa: ");
        String nazwa = scanner.nextLine();
        System.out.print("Cena: ");
        BigDecimal cena = new BigDecimal(scanner.nextLine());
        System.out.print("Kod kreskowy: ");
        String kod = scanner.nextLine();
        System.out.print("Tag NFC: ");
        String nfc = scanner.nextLine();
        Produkt produkt = new Produkt(nazwa, cena, kod, nfc);
        kasaService.dodajProdukt(produkt);
        System.out.println("Dodano produkt: " + produkt);
        
    }

    private void removeProduct() {
        System.out.print("Kod kreskowy produktu do usunięcia: ");
        String kod = scanner.nextLine();
        Produkt dummy = new Produkt("", BigDecimal.ZERO, kod, "");
        kasaService.usunProdukt(dummy);
        System.out.println("Usunięto produkt o kodzie: " + kod);
        
    }

    private void changeQuantity() {
        System.out.print("Kod kreskowy: ");
        String kod = scanner.nextLine();
        System.out.print("Nowa ilość: ");
        int ilosc = Integer.parseInt(scanner.nextLine());
        Produkt dummy = new Produkt("", BigDecimal.ZERO, kod, "");
        kasaService.zmienIlosc(dummy, ilosc);
        System.out.println("Ustawiono ilość " + ilosc + " dla produktu o kodzie: " + kod);
        
    }

    private void viewCart() {
        List<Produkt> koszyk = kasaService.getKoszyk();
        if (koszyk.isEmpty()) {
            System.out.println("Koszyk jest pusty.");
        } else {
            System.out.println("Zawartość koszyka:");
            koszyk.forEach(p -> System.out.println(p));
        }
    }

    private void checkout() {
        Transakcja transakcja = kasaService.finalizujTransakcje();
        historiaTransakcji.add(transakcja);
        System.out.println("Transakcja zakończona. Suma: " + transakcja.getSuma());
       
    }

    private void printReceipt() {
        if (historiaTransakcji.isEmpty()) {
            System.out.println("Brak transakcji do wygenerowania paragonu.");
        } else {
            Transakcja ostatnia = historiaTransakcji.get(historiaTransakcji.size() - 1);
            String paragon = ParagonGenerator.generujParagon(ostatnia);
            System.out.println(paragon);
        }
    }

    private void printDailyReport() {
        if (historiaTransakcji.isEmpty()) {
        System.out.println("Brak transakcji do raportu dziennego.");
        return;
        }
        List<Transakcja> dzienne = historiaTransakcji;
        String raport = raportService.generujRaportDzienny(dzienne);
        System.out.println(raport);
    }
}