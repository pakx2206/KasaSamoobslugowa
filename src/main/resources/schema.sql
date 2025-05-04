CREATE TABLE IF NOT EXISTS produkt (
    kod_kreskowy TEXT PRIMARY KEY,
    nazwa        TEXT NOT NULL,
    cena         REAL NOT NULL,
    nfc_tag      TEXT
);

CREATE TABLE IF NOT EXISTS transakcja (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    data         TEXT    NOT NULL,
    suma         REAL    NOT NULL
);

CREATE TABLE IF NOT EXISTS transakcja_produkt (
    transakcja_id    INTEGER NOT NULL,
    kod_kreskowy     TEXT    NOT NULL,
    ilosc            INTEGER NOT NULL,
    FOREIGN KEY(transakcja_id) REFERENCES transakcja(id),
    FOREIGN KEY(kod_kreskowy)  REFERENCES produkt(kod_kreskowy)
);
CREATE TABLE IF NOT EXISTS koszyk (
    kod_kreskowy TEXT PRIMARY KEY,
    ilosc        INTEGER NOT NULL
);