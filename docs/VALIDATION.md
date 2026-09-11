# Validation — 11 September 2026

- Android application ID: `com.KDapps.politeia`

- Core: 396 tests passed, zero failures or errors.
- Historical regression: 348 official election datasets, including both 2023 parliamentary elections, European elections 2024, 332 municipalities and 13 regions. Calculated party/list seats match the stored official results.
- Other core tests: 48 tests covering thresholds, stepped bonus seats, coalition treatment, allocation ties and local rule changes, percentage remainders and poll parsing.
- Android UI: 13 instrumented tests passed on the API 36 emulator at its standard dimensions and at 960 × 1920 pixels, covering bilingual operation, historic calculations, custom entry, saved scenarios, sound controls, activity recreation, landscape layout, party-color picker behavior, remainder handling, coalition simulation and locked step navigation.
- Debug APK build and Android lint passed (existing dependency/deprecation warnings remain).
- An earlier development build was installed on Samsung SM-S911B over Wi-Fi. This UI update was validated on the emulator; no physical phone was connected during this run.

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

The release configuration targets Android 16/API 36 and version `1.2.0` (`versionCode 3`). The API 36 platform is now installed and the updated debug app builds successfully. A Google Play upload still requires a verified signed release AAB, final listing assets, publisher details, a public HTTPS privacy-policy URL and completed Play Console declarations.
