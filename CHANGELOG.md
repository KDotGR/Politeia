# Changelog

## 0.4 — in development

- Remove the redundant historical-results action from More.
- Show color-coded seat changes against the selected election, including parties losing all seats and poll-to-official party matching.

- Remove the numbered step header; retain page titles and Previous/Next navigation.
- Display national historical percentages with at most two decimals while retaining precise values for editing and seat calculations.
- Keep R8 optimization, obfuscation and resource shrinking enabled; versionCode 6.

## Unreleased — public documentation and repository checks

- Replace maintainer-oriented README content with English and Greek user instructions and a contributor quick start.
- Add tracked-file credential checks, CI coverage and broader ignores for private credential exports.

## Unreleased — project organization

- Consolidate the local project at the project root with separate research, release artifacts and private signing folders.
- Make the official-data importer resolve paths relative to the repository root.
- Document the folder layout and exclude private files and generated archives from Git.

## 0.3 — in development

- Enable R8 code optimization and obfuscation plus resource shrinking for release builds.
- Bundle the generated deobfuscation mapping for Google Play crash reports.
- Android versionCode 5; development stays on `codex/version-0.3`.

## Unreleased — percentage-only national elections

- Parliament and European elections now use percentages exclusively, without redundant input-format buttons.
- Convert historical results and saved vote-count scenarios to percentages automatically.
- Keep both percentages and vote counts for local and custom elections.

## Unreleased — election result sources

- Share the existing poll catalog and refresh settings with European-election scenarios.
- Require an explicit previous-election or poll choice, followed by a selection, before advancing from step 2.
- Load poll percentages immediately without a confirmation popup; load historical vote counts automatically for editing in step 4.
- Use one name in both languages when adding a new party. Preserve existing translated party names.

## Unreleased — custom-election refinements

- Add a Yes/No bonus switch; bonus settings appear only when enabled.
- Use one custom party name in both languages and require its candidate count.
- Cap custom party seats by available candidates; redistribute surplus proportionally among eligible parties with capacity, leaving any unfillable seats vacant.
- Explain candidate limits, transfers and vacancies in Greek and English.

## 0.2 — 13 September 2026 (review candidate)

- Split setup into election type, party list, electoral law, votes and seats pages; custom elections skip party-list selection.
- Choose electoral laws independently of historical party lists. Poll selection loads editable percentages.
- Open coalition simulation, poll assumptions and calculation explanations on dedicated pages, returning to seats.
- Space settings controls and preserve scenarios and navigation across recreation.
- Prevent closing selection dialogs from blocking quick navigation taps.
- Android versionCode 4; development remains on `codex/version-0.2` pending approval to merge.

## Unreleased — 1.2.0 preparation

### UI refinement — 11 September 2026

- Show only the active step with muted progress labels; Previous stays at the upper left and the primary action ends the step.
- Group scenario actions under More and language, sound, sources and poll refresh under Settings.
- Add short step transitions, touch haptics, navigation/success/error sounds and animated coalition progress. Sound respects the app toggle and silent mode; motion respects Android animation settings.
- Preserve party-entry scroll position and handle Android 16 Back navigation without losing the scenario.
- Fix stray editor text that prevented compilation.
- Set the Android/Google Play application ID to `com.KDapps.politeia`.
- Verified 13 UI tests at standard and narrower emulator dimensions; 396 core tests and debug lint pass.

### Initial baseline

Initial version-control baseline captured on 11 September 2026. Earlier development was not recorded in Git; this snapshot does not reconstruct that history.

- Greek and English Android election calculator with parliamentary, European, local and custom scenarios.
- Historical fixtures, calculation explanations, percentage remainders and parliamentary coalition simulation.
- Poll-source refresh, party colours, saved scenarios, optional sounds and constrained step navigation.
- Android API 36 and versionCode 3 configured; conditional upload-key signing and Play Store draft materials added.

Release status: API 36 is installed and the updated debug build is verified. Release build/signing, final listing assets and publisher/privacy details remain outstanding.
