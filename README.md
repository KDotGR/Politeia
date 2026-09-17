# Politeia / Πολιτεία

**Independent application — not a government service.** Politeia is not affiliated with, endorsed by, or representative of the Greek government, the Ministry of Interior, or any government entity. Its calculations are unofficial estimates and scenario simulations.

Politeia is an Android app for exploring how votes translate into seats in Greek elections. Use historical election data, published polls or your own scenarios, then follow the explanation of the allocation. The interface supports **English and Greek** and requires **Android 8.0 or later**.

## Using the app

1. **Choose an election:** Parliament, European Parliament, local (municipal or regional), or custom.
2. **Choose a source:** select a previous election or an available poll, then choose its dataset. Historical results and poll percentages load automatically and remain editable. Custom elections skip this step. Polls are available for Parliament and European Parliament scenarios; the European option applies national polling to a 21-seat scenario, not polling specifically conducted for European elections.
3. **Choose the law:** use the default current profile or select one of the supported historical profiles. Choosing a historical party list does not automatically select its historical law. For custom elections, define the total seats, entry threshold and optional bonus-seat rules.
4. **Enter votes:** Parliament and European Parliament elections use percentages. Local and custom elections also support vote counts. Add or edit party entries as needed. For custom elections, enter each party's name and number of candidates.
5. **Calculate seats:** view the seat chart and party totals. Dedicated buttons open the allocation explanation and, when applicable, poll sources and assumptions. Parliament results also offer **Simulate Government Coalition**: select one or more parties to test whether they reach the 151-seat majority target. Each detail page has a back button to return to the results.

Use the previous/next buttons to move between steps. Only the current step is shown. The **More** menu in the Votes step lets you save, open or clear scenarios. Open **Settings** using the gear icon to change language, toggle sounds, view sources and privacy information, or manage optional poll updates.

### Percentages, remaining votes and candidate limits

For elections with a 3% threshold, you can enter only the parties you want to include. Any unentered share up to 100% is treated as a group of other parties, each assumed to be below 3%, and receives no seats. For example, entering 30% and 25% leaves 45% as other parties. The total cannot exceed 100%.

Custom elections limit each party's seats to its number of candidates. Excess seats are redistributed among eligible parties with remaining candidates; seats remain vacant if there are too few eligible candidates overall. The explanation records these adjustments. If an allocation needs a lottery to resolve a tie, the app asks you to specify the outcome.

## Data and scope

The bundled historical catalog covers the May and June 2023 parliamentary elections, the 2024 European elections, and the 2023 elections for 332 municipalities and 13 regions. These are dated reference datasets, not a current registry of parties eligible to contest a future election. Local list names in English are transliterations.

Historical vote counts are converted to percentages for Parliament and European Parliament scenarios. Percentage rounding and the selected law can affect the result. Parliamentary calculations cover national party seat entitlements; they do not allocate candidates or seats to individual constituencies. Exceptional constituency allocations require district-level data beyond this calculator.

Polls come from third-party sources and are estimates, not official election results or predictions. Optional refresh checks for published polls; an update does not replace your active scenario. Bundled data and calculations work offline. Refreshing polls and opening source links require an internet connection.

Consult the original official sources for election results and legislation:

- [Ministry of Interior — official election results](https://ekloges.ypes.gr/)
- [Ministry of Interior — electoral legislation](https://www.ypes.gr/eklogiki-nomothesia/)
- [PolitPro — third-party Greek polling source](https://politpro.eu/en/greece)
- [Historical dataset provenance](app/src/main/assets/provenance.json)

No account is required. Scenarios and preferences are stored on your device. Read the [privacy policy](PRIVACY_POLICY.md) for details.

## Γρήγορη εκκίνηση στα ελληνικά

**Η Πολιτεία είναι ανεξάρτητη εφαρμογή και δεν εκπροσωπεί ούτε συνδέεται με κρατικό φορέα. Οι υπολογισμοί της είναι ανεπίσημες εκτιμήσεις και προσομοιώσεις.**

1. Επιλέξτε βουλευτικές, ευρωπαϊκές, αυτοδιοικητικές ή προσαρμοσμένες εκλογές.
2. Επιλέξτε προηγούμενες εκλογές ή διαθέσιμη δημοσκόπηση και φορτώστε τα αποτελέσματα. Οι προσαρμοσμένες εκλογές παραλείπουν αυτό το βήμα.
3. Επιλέξτε εκλογικό νόμο ή ορίστε τους δικούς σας κανόνες. Η επιλογή παλαιότερων εκλογικών δεδομένων δεν αλλάζει αυτόματα τον νόμο.
4. Επεξεργαστείτε τα ποσοστά. Στις αυτοδιοικητικές και προσαρμοσμένες εκλογές μπορείτε επίσης να εισαγάγετε αριθμό ψήφων. Στις προσαρμοσμένες εκλογές ορίστε και τον αριθμό υποψηφίων κάθε κόμματος.
5. Υπολογίστε τις έδρες και ανοίξτε, αν θέλετε, την εξήγηση κατανομής, τις πηγές δημοσκοπήσεων ή την προσομοίωση κυβερνητικής συνεργασίας. Στις βουλευτικές εκλογές ο στόχος πλειοψηφίας είναι 151 έδρες.

Για εκλογές με όριο 3%, το ποσοστό που απομένει έως το 100% θεωρείται ότι ανήκει σε λοιπά κόμματα, καθένα κάτω από το όριο. Από το γρανάζι αλλάζετε γλώσσα, ήχους και ενημερώσεις δημοσκοπήσεων. Από το μενού **Άλλα** στο βήμα Ψήφοι αποθηκεύετε και ανοίγετε σενάρια.

## Build from source

Clone this repository and open its root folder in Android Studio:

```sh
git clone https://github.com/KDotGR/Politeia.git
cd Politeia
```

Install **Android SDK 36** and use **JDK 17 or a compatible newer JDK** for Gradle. Let Android Studio sync the project, select the **app** configuration and a connected device or emulator, then press **Run**. Android Studio can create your local SDK configuration during setup. No release signing key is needed for a debug build.

You can also build and check it from a terminal:

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

The second command requires a connected device or running emulator. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk` and uses a development signing certificate.

The `core` module contains the Java calculation engine; `app` contains the Android interface, local storage and data access. The checked-in JSON and test fixtures are sufficient to build and test. The optional import helper uses research inputs under `research/raw` and is not needed to run the app.

See [validation results](docs/VALIDATION.md), [project structure](docs/PROJECT_STRUCTURE.md), the [changelog](CHANGELOG.md), and [release preparation](play-store/release-checklist.md) for development details. Report a reproducible problem or suggest an improvement through [GitHub Issues](https://github.com/KDotGR/Politeia/issues), including the app version, election type and steps needed to reproduce it.

Before contributing, run `python3 scripts/check_public_files.py` to check tracked files for common secret patterns. Keep signing keys, passwords and local signing configuration out of commits.
