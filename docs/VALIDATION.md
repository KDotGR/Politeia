# Validation

## Election result sources — 13 September 2026

- Wrote focused UI regressions first; the explicit-source test failed against the old default-enabled Next button.
- Eight relevant UI cases passed on the API 36 emulator, in a six-case run followed by two final cases: explicit source selection and recreation, direct Parliament historical import (158 ND seats), European poll scenario (21 seats), one-language new-party naming, poll detail recreation, independent historical law preservation, blank edited vote restoration, and direct European historical import (7 ND seats).
- European scenarios reuse the same national poll catalog, cache and refresh schedule. The UI identifies their national-poll origin.
- Review caught and fixed eager legacy-source inference that could discard a draft containing an unfinished vote input.
- Debug APK rebuilt successfully; diff whitespace check passed. No unrelated engine tests or full UI suite were run because the allocation engine and poll parser are unchanged.
- Changes remain uncommitted on `codex/version-0.2` for the user to commit and push. No main-branch push or Play upload was performed.

## Custom-election refinement — 13 September 2026

- Tests written first: bonus-switch UI test failed against the prior app; candidate tests initially failed to compile without the new capacity interface.
- Ten candidate-limit engine tests pass: bonus caps, repeated redistribution, exhausted capacity, threshold/zero-vote exclusion, zero candidates, explicit ties and vote/percentage consistency.
- Redistribution uses proportional largest remainders among eligible parties with spare candidates; unfillable seats remain vacant.
- Five custom UI cases pass: bonus on/off visibility and recreation, single bilingual name and required candidates, bonus calculation, seat redistribution, and saved/recreated vacancies. The bonus-toggle test passed on focused rerun after explicitly focusing fields before editing to avoid a keyboard-dismiss timeout.
- Debug APK build passed. Only custom-related tests were run for this change; the full election regression suite was deliberately not rerun.
- Changes are left uncommitted at the user’s request.

## Version 0.2 — 13 September 2026

Navigation tests were committed first (`1e8024a`), and the isolated-step test was observed failing against the old UI. Additional regressions cover legacy saved drafts and retaining a chosen law when loading a poll. The test fixture also now uses the actual activity preference filename after the application-ID change.

- Core: 396 tests, zero failures/errors; calculation engine unchanged.
- UI: all 20 cases passed at 320dp width (840 × 1920, density 420) with 130% text, including a focused rerun of the landscape case after making its assertion scroll-aware.
- Fixed native selection-dialog dim layers swallowing quick Next/Previous taps; all four affected navigation regressions passed at standard size.
- Final standard-size run: all 20 UI tests passed at 960 × 1920, density 420, 100% text. Android lint and debug APK build passed. Existing AGP/API compatibility and deprecation warnings remain.
- Independent test, implementation and review agents contributed. No physical-device installation or Play upload is part of this validation.

## Previous baseline — 11 September 2026

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

The version 0.2 review candidate targets Android 16/API 36 with `versionCode 4` and `versionName 0.2`, following the publisher’s requested version numbering. The debug APK is validated; this change has not been merged to main, tagged as a final release, or uploaded to Google Play. A new signed release AAB and Play Console review remain separate release steps.
