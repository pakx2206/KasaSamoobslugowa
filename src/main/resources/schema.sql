ALTER TABLE produkt ADD COLUMN ilosc INTEGER NOT NULL DEFAULT 0;
CREATE TABLE produkt (
    kod_kreskowy TEXT PRIMARY KEY,
    nazwa TEXT NOT NULL,
    cena REAL NOT NULL,
    nfc_tag TEXT
);

CREATE TABLE transakcja (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    data TEXT NOT NULL,
    suma REAL NOT NULL
);

CREATE TABLE transakcja_produkt (
    transakcja_id INTEGER,
    kod_kreskowy TEXT,
    ilosc INTEGER,
    cena_jednostkowa REAL,
    FOREIGN KEY (transakcja_id) REFERENCES transakcja(id),
    FOREIGN KEY (kod_kreskowy) REFERENCES produkt(kod_kreskowy)
);

CREATE TABLE koszyk (
    kod_kreskowy TEXT PRIMARY KEY,
    ilosc INTEGER NOT NULL
);
CREATE TABLE produkt (
    kod_kreskowy TEXT PRIMARY KEY,
    nazwa TEXT NOT NULL,
    cena REAL NOT NULL,
    nfc_tag TEXT,
    ilosc INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE transakcja_produkt (
    transakcja_id INTEGER,
    kod_kreskowy TEXT,
    ilosc INTEGER,
    cena_jednostkowa REAL,
    FOREIGN KEY (transakcja_id) REFERENCES transakcja(id),
    FOREIGN KEY (kod_kreskowy) REFERENCES produkt(kod_kreskowy)
);

