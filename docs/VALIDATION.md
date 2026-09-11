# Validation — 11 September 2026

- Core: 378 tests passed, zero failures or errors.
- Historical regression: 348 official election datasets, including both 2023 parliamentary elections, European elections 2024, 332 municipalities and 13 regions. Calculated party/list seats match the stored official results.
- Boundary and current-law tests: 30 tests covering thresholds, stepped bonus seats, coalition treatment, allocation ties and local rule changes.
- Android UI: 12 instrumented tests passed on the API 36 emulator, covering bilingual operation, historic calculations, custom entry, saved scenarios, sound controls, activity recreation, landscape layout, party-color picker behavior, remainder handling, coalition simulation and locked step navigation.
- Debug APK build and Android lint passed; APK signature verified.
- Installed through Android Studio over Wi-Fi on Samsung SM-S911B. The latest connected run installed the current APK and began the suite on the phone; Samsung's system ended the activity during a recreation test, so the deterministic pass is recorded from the API 36 emulator.

Core tests were written before the initial engine implementation and observed failing before implementation. Regression fixtures use Ministry of Interior/SingularLogic results, with provenance recorded alongside the catalog.

## Law sources

- Parliamentary Law 4654/2020: https://www.hellenicparliament.gr/UserFiles/bcc26661-143b-4f2d-8916-0e0e66ba4c50/eklog-voul-ap-all.pdf
- European election ministry circular: https://www.ypes.gr/wp-content/uploads/2024/05/eggr43270-egk30-20240524.pdf
- Municipal 2023 circular: https://www.ypes.gr/wp-content/uploads/2023/08/eggr65437-egk849-20230803.pdf
- Regional 2023 circular: https://www.ypes.gr/wp-content/uploads/2023/08/eggr65443-egk850-20230803.pdf
- Local code 5314/2026, voted text: https://www.hellenicparliament.gr/UserFiles/bcc26661-143b-4f2d-8916-0e0e66ba4c50/13344969.pdf
- Transition amendment, Law 5321/2026 Article 53: https://aade.gr/egkyklioi-kai-apofaseis/o-3033-ex-2026-30-07-2026

Historical agreement does not validate every possible future electoral scenario. See the README for constituency, percentage-estimation and dated-catalog limitations.

## Release status

The release configuration now targets Android 16/API 36 and version `1.2.0` (`versionCode 3`). A Google Play upload still requires the API 36 SDK platform, a publisher-owned upload key, a signed release AAB, a public HTTPS privacy-policy URL and completed Play Console declarations. The local SDK currently contains build-tools 36 but not the API 36 platform, so release-bundle generation is blocked until that SDK component is installed.
