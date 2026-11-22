# Dokumentacja projektu

## Nazwa kursu
Testowanie i Jakość Oprogramowania II

## Autor
Krystian Chaim

## Temat projektu
Maszyna wirtualna CHIP-8

## Opis projektu
Aplikacja desktopowa implementująca emulator 8-bitowej wirtualnej maszyny **CHIP-8** wraz z graficznym narzędziem diagnostycznym (debuggerem).
Rdzeń symulacji odwzorowuje architekturę sprzętową oryginalnego systemu: 4096 bajtów pamięci operacyjnej RAM (z wbudowanym zestawem czcionek szesnastkowych `0–F`),
16 rejestrów ogólnego przeznaczenia (`V0–VF`), 16-bitowy rejestr indeksowy (`I`), licznik rozkazów (`PC`), 16-poziomowy stos wywołań,
dwa timery (`DT`, `ST`), 16-klawiszową klawiaturę szesnastkową oraz monochromatyczny bufor ekranu o rozdzielczości **64×32 piksele** z detekcją kolizji XOR.


## Technologie użyte w projekcie
* **Język programowania:** Java 17
* **Interfejs użytkownika i renderowanie:** Java Swing oraz `java.awt.Canvas`
* **Środowisko programistyczne:** IntelliJ IDEA
* **Framework testowy:** JUnit 5 (Jupiter 5.10.2)
* **Kontrola wersji:** Git / GitHub

## Uruchomienie projektu

1. Sklonuj repozytorium:
   ```bash
   git clone https://github.com/KrystianChaim/chip-8
   
2. Odszukaj plik src/com/chip8/Main.java z główną funkcją oraz uruchom go