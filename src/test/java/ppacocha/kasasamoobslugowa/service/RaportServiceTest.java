package ppacocha.kasasamoobslugowa.service;

import org.junit.jupiter.api.Test;
import ppacocha.kasasamoobslugowa.model.Transakcja;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RaportServiceTest {
    @Test
    void getTodaysTransactions_notNull() {
        List<Transakcja> list = RaportService.getTodaysTransactions();
        assertNotNull(list);
    }

    @Test
    void getTodaysTransactions_allDatesAreToday() {
        List<Transakcja> list = RaportService.getTodaysTransactions();
        for (Transakcja t : list) {
            assertEquals(LocalDate.now(), t.getData().toLocalDate());
        }
    }
}
