# 📡 Uniwersalny Pilot IR — Android

Natywna aplikacja Android do sterowania urządzeniami przez podczerwień (IR), inspirowana projektem [Uniwersalny Pilot IR](https://embed.fbsbx.com/playables/view/2195950227896985/).

![Android](https://img.shields.io/badge/Android-API%2021%2B-green?logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.8-blue?logo=kotlin)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📱 Funkcje

- **Prawdziwe sterowanie IR** przez `ConsumerIrManager` API (Android 4.4+)
- Obsługa **protokołu NEC** i **formatu Pronto hex**
- Baza urządzeń: **Blaupunkt, Samsung, LG, Sony, Philips**
- Pełny pilot TV: Power, Source, VOL±, CH±, Mute, Menu, OK, strzałki, cyfry
- **Wskaźnik LED** (animacja przy wysyłaniu sygnału)
- **Tryb demonstracyjny** na telefonach bez nadajnika IR
- Wyświetlanie kodu NEC i Pronto po każdym naciśnięciu
- Wyszukiwarka urządzeń
- Ciemny motyw (Dark Mode)
- Wibracje haptyczne przy naciśnięciu przycisku

---

## 📋 Wymagania

| Wymaganie | Wartość |
|-----------|---------|
| Android SDK | minSdk 21 (Android 5.0 Lollipop) |
| Target SDK | 33 (Android 13) |
| Kotlin | 1.8.0 |
| Gradle | 8.0.2 |

### Telefony z nadajnikiem IR

Aplikacja wysyła prawdziwe sygnały IR na telefonach wyposażonych w nadajnik podczerwieni:

| Producent | Modele |
|-----------|--------|
| **Xiaomi** | Mi 3, Mi 4, Mi 5, Mi 6, Mi 9, Mi 10, Redmi Note (większość) |
| **Huawei** | Mate 9/10/20, P20/P30/P40 Pro |
| **Honor** | 8X, 9X, 20 Pro, Magic |
| **Samsung** | Galaxy S4, S5 (starsze modele) |
| **HTC** | One M7, M8, M9 |
| **LG** | G5 |

Na telefonach **bez** nadajnika IR aplikacja działa w **trybie demonstracyjnym** — wyświetla kody IR, ale ich nie wysyła.

---

## 🏗️ Architektura projektu

```
app/
└── src/main/
    ├── java/com/example/universalirremote/
    │   ├── MainActivity.kt              # Główna aktywność - pilot TV
    │   ├── DeviceSelectActivity.kt      # Wybór urządzenia z bazy
    │   ├── ir/
    │   │   └── IRManager.kt             # Obsługa ConsumerIrManager API
    │   ├── data/
    │   │   └── DeviceDatabase.kt        # Baza kodów IR (NEC + Pronto)
    │   └── model/
    │       └── IRDevice.kt              # Modele danych
    └── res/
        ├── layout/
        │   ├── activity_main.xml        # Layout pilota
        │   ├── activity_device_select.xml
        │   └── item_device.xml
        ├── values/
        │   ├── colors.xml
        │   ├── strings.xml
        │   └── themes.xml
        └── drawable/
            ├── led_active.xml
            ├── led_inactive.xml
            └── ...
```

---

## 🔧 Jak to działa — IR Manager

### Protokół NEC

Protokół NEC jest najczęściej stosowany w urządzeniach RTV. Każdy kod składa się z:
- **Impulsu startowego**: 9000 µs mark + 4500 µs space
- **32 bitów danych** (adres + komenda + ich negacje)
- **Bitu stopu**: 562 µs mark

Bit `0` = 562 µs mark + 562 µs space  
Bit `1` = 562 µs mark + 1687 µs space

### Format Pronto Hex

Format Pronto to bardziej ogólny zapis sygnałów IR:
```
0000 006D 0022 0002 [dane...]
^    ^    ^    ^
|    |    |    +-- liczba par w sekwencji 2
|    |    +------- liczba par w sekwencji 1
|    +------------ kod częstotliwości nośnej
+----------------- typ kodu (0000 = learned)
```

Każda para to: `[mark_units] [space_units]` gdzie jednostka = `1/(freq_code × 0.241246)` µs

### Użycie ConsumerIrManager

```kotlin
val irManager = context.getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager

// Sprawdzenie dostępności
if (irManager.hasIrEmitter()) {
    // Wysłanie sygnału
    irManager.transmit(
        frequency = 38000,   // Hz
        pattern = intArrayOf(9000, 4500, 562, 562, ...) // µs
    )
}
```

---

## 🚀 Uruchomienie

### Wymagania deweloperskie

- Android Studio Flamingo lub nowszy
- JDK 11+
- Android SDK z API 21+

### Kroki

```bash
# 1. Sklonuj repozytorium
git clone https://github.com/YOUR_USERNAME/UniversalIRRemote.git
cd UniversalIRRemote

# 2. Otwórz w Android Studio
# File → Open → wybierz katalog UniversalIRRemote

# 3. Zbuduj projekt
./gradlew assembleDebug

# 4. Zainstaluj na urządzeniu
./gradlew installDebug
```

### Instalacja APK

Plik APK po zbudowaniu znajdziesz w:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📦 Dodawanie własnych urządzeń

Dodaj nowe urządzenie do `DeviceDatabase.kt`:

```kotlin
IRDevice(
    id = "moje_urzadzenie",
    brand = "MójBrand",
    model = "Model XYZ",
    type = "TV",
    codes = mapOf(
        "POWER" to IRCode(
            nec = "0x1234ABCD",
            pronto = "0000 006D 0022 0002 ..."
        ),
        // ... więcej przycisków
    )
)
```

Kody IR możesz znaleźć w bazach danych takich jak:
- [IRDB](https://github.com/probonopd/irdb)
- [Pronto Database](http://www.remotecentral.com/cgi-bin/codes/)
- [IrScrutinizer](https://github.com/bengtmartensson/IrScrutinizer)

---

## 📄 Licencja

```
MIT License

Copyright (c) 2024 UniversalIRRemote Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🤝 Wkład w projekt

Pull requesty są mile widziane! Szczególnie potrzebne:
- Nowe bazy kodów IR dla popularnych urządzeń
- Obsługa klimatyzatorów (protokół HVAC)
- Obsługa projektorów
- Testy jednostkowe dla IRManager
