# Politeia Google Play release checklist

This checklist is the handoff for the Google Play Console owner. It assumes the release bundle is built from the `release` variant after the Android 16/API 36 SDK is installed.

## Build and signing

1. Install Android SDK Platform 36 and build-tools 36.x in Android Studio.
2. Run the core tests, lint, and Android instrumentation tests on an API 36 emulator.
3. Create a private upload key in Android Studio or with `keytool`. Keep the keystore and passwords in a password manager. Never commit them.
4. Copy `keystore.properties.example` to `keystore.properties`, fill it with the upload-key values, and generate a **signed Android App Bundle** (`app-release.aab`) with the release variant. Enrol in Google Play App Signing when creating the app; Google retains the app-signing key and the developer retains the upload key.
5. Increase `versionCode` for every subsequent upload. The current release is `3` / `1.2.0`.

## Play Console declarations

- App name: **Politeia / Πολιτεία**
- Suggested category: Education or Tools
- Target audience: general audience; this app is not directed to children
- App access: no account or login is required
- Ads: none
- Privacy policy: host `privacy-policy.html` at a public HTTPS URL and enter that URL in Play Console and in the app listing
- Data safety: complete the form according to the current app build. The app has no account, analytics, advertising SDK, or intentional collection/sharing of personal data. It stores scenarios and settings locally. Recheck this declaration if a future release adds analytics, crash reporting, accounts, or remote storage.
- Content rating: complete the questionnaire; the app contains election information and simulations, with no user-generated public content
- Government-app declaration: answer according to the publisher's identity and authorization
- Financial-features declaration: no financial features
- News and magazine declaration: choose the category that matches the publisher's intended listing

## Listing assets and review notes

- Provide a 512 x 512 PNG store icon, phone screenshots, and a feature graphic if requested by Play Console.
- Use the English and Greek copy in `listing-en.md` and `listing-el.md`.
- Mention that the calculator explains each allocation step and that historical datasets are dated reference scenarios.
- The optional poll refresh opens links to published sources; it does not turn the app into an official government service.
- If the developer account is a new personal account created after 13 November 2023, complete Google's required closed-testing process before production access.

## Release evidence

Keep the signed bundle, mapping file if shrinking is enabled, test report, version number, source commit/archive, and the final privacy-policy URL together for the release record.
