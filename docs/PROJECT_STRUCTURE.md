# Project layout

Open `/Volumes/files/Politeia` in Android Studio and GitHub Desktop. This directory is the Gradle and Git repository root.

| Directory | Purpose |
| --- | --- |
| `app/` | Android application, resources, bundled election data and device tests |
| `core/` | Plain Java allocation engine and unit tests |
| `gradle/` | Versioned Gradle wrapper |
| `scripts/` | Build artifact verification |
| `tools/` | Development utilities, including the official-data importer |
| `docs/` | Architecture, layout and validation notes |
| `play-store/` | Store descriptions, privacy materials and promotional assets |
| `research/raw/` | Local source documents and downloaded election datasets |
| `research/legacy-tools/` | Archived one-off research scripts; not the supported build workflow |
| `artifacts/releases/` | Preserved signed release bundles, APKs and their matching R8 maps |
| `artifacts/legacy/` | Earlier source ZIPs and APKs |
| `artifacts/logs/`, `artifacts/screenshots/` | Historical development evidence |
| `.local/signing/` | Private upload keystore and password files; never commit |

`research` inputs, generated artifacts and private signing files are ignored by Git. The application builds from the versioned files in `app` and `core`; research downloads are optional. Existing source data is retained for reproducibility.

Standard build outputs remain in `app/build/outputs/`. Gradle recreates `.gradle/` and module `build/` directories. Do not move these generated folders into source directories or commit them.

## Signing

The ignored root `keystore.properties` points to `../.local/signing/politeia-upload.jks` because Gradle resolves the signing path relative to `app/`. Passwords remain in the private properties file and `.local/signing/`. The Android SDK stays installed outside the repository and is referenced by ignored `local.properties`.

```sh
./gradlew :app:bundleRelease
python3 scripts/verify_release_bundle.py
```

Keep each published AAB together with its matching `mapping.txt` under `artifacts/releases/<version>-code<code>/`. Do not overwrite a previously published release with a different build using the same version code.
