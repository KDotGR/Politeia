# Changelog

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
