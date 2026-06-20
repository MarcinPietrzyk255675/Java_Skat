# Java_Skat — sensowniejsza wersja

Ta wersja nie zmienia `Projekt_PIO`. Traktuje go jako bibliotekę `skat-core` i dokłada po stronie JavaFX:

- `game` — lokalny kontroler stanu gry,
- `ui` — widoki JavaFX,
- `network` — szkic komunikacji klient-serwer przez serializowalne DTO.

## Uruchomienie

Najpierw zainstaluj zamrożony Projekt_PIO lokalnie:

```bash
cd ~/IdeaProjects/Projekt_PIO
mvn clean install
```

Potem uruchom Java_Skat:

```bash
cd ~/IdeaProjects/Java_Skat
mvn clean javafx:run
```

## Ważne

Nie kopiuj klas `Karta`, `Figura`, `Kolor`, `Gracz`, `Rozdanie` z `Projekt_PIO` do Java_Skat. Java_Skat ma ich używać przez dependency `pl.skat:skat-core:1.0-SNAPSHOT`.

Klasy sieciowe nie wysyłają bezpośrednio `Karta`, tylko `CardDto`, bo `Projekt_PIO` nie może być już zmieniany.
