# Politeia / Πολιτεία

Native Android election seat calculator in Greek and English. The Android/Google Play package name is `com.KDapps.politeia`. Open this folder in Android Studio, select the **app** configuration and your device, then Run. Requires Android 8.0 or later, Android SDK 36, and JDK 17 or newer.

## Build and test

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:bundleRelease
```

The second command requires a connected Android device or emulator. APK output: `app/build/outputs/apk/debug/app-debug.apk`. The supplied APK is a development build, signed with a debug certificate.

The Play Console upload artifact is the release Android App Bundle at `app/build/outputs/bundle/release/app-release.aab`. The bundle must be signed with your own upload key in Android Studio before upload. The app is configured for Android 16/API 36, which is required for new Google Play apps and updates from 31 August 2026.

## Features and scope

Parliament, European Parliament, municipal/regional and custom elections; percentage or vote-count entry; bilingual explanations; configurable custom bonus steps; editable parties and coalition membership; saved scenarios; responsive seat visualization; optional sounds.

The offline catalog contains complete published ballot lists for May/June 2023 parliamentary elections, European elections 2024, and 332 municipal plus 13 regional elections in 2023. These are dated scenarios, not a live registry of parties eligible in future elections. Local list English names are transliterations. Users can edit the lists.

Parliament calculations give national party entitlements. Exceptional constituency allocations under Articles 99/100 require constituency data and are outside this calculator. EU and local percentage inputs are estimates: use exact vote counts for integer-quota calculations. Exact allocation ties require an explicit lottery outcome. Current local rules and historical 2023 rules are distinct profiles. The app includes the 2026 local code; historical examples retain their original rules.

## Architecture

`core` is a platform-independent Java calculation engine with exact decimal/integer arithmetic. `app` contains the native Android UI, bilingual presentation, offline catalog and local persistence. Neither the app nor the engine requires network access. Source links open in the user's browser.

See `docs/VALIDATION.md` for test results and `app/src/main/assets/provenance.json` for all historical data URLs. The development import helper under `tools` requires the original research cache in `work`; it is not needed to build or run the app. The checked-in JSON and TSV contain the complete build and test inputs.

## Play Store release preparation

Use `play-store/release-checklist.md` for the listing, privacy-policy, Data safety, content-rating, signing and testing steps. Read the policy directly in [`PRIVACY_POLICY.md`](PRIVACY_POLICY.md), or use the GitHub Pages version at `https://kdotgr.github.io/Politeia/privacy-policy.html`. `play-store/listing-en.md` and `play-store/listing-el.md` contain the bilingual store copy.
