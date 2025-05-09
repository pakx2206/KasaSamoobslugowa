PRAGMA foreign_keys = ON;

CREATE TABLE produkt (
  kod_kreskowy    TEXT PRIMARY KEY,
  nazwa           TEXT NOT NULL,
  cena            REAL NOT NULL,
  nfc_tag         TEXT,
  ilosc           INTEGER NOT NULL DEFAULT 0,
  vat_rate        REAL    NOT NULL DEFAULT 0.23
);

CREATE TABLE IF NOT EXISTS koszyk (
  kod_kreskowy    TEXT    PRIMARY KEY,
  ilosc           INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS transakcja (
  id              INTEGER PRIMARY KEY AUTOINCREMENT,
  data            TEXT    NOT NULL,
  suma            REAL    NOT NULL,
  typ_platnosci   TEXT    NOT NULL
);

CREATE TABLE IF NOT EXISTS transakcja_produkt (
  transakcja_id      INTEGER NOT NULL,
  kod_kreskowy       TEXT    NOT NULL,
  ilosc              INTEGER NOT NULL,
  cena_jednostkowa   REAL    NOT NULL,
  FOREIGN KEY(transakcja_id)    REFERENCES transakcja(id) ON DELETE CASCADE,
  FOREIGN KEY(kod_kreskowy)     REFERENCES produkt(kod_kreskowy)
);
