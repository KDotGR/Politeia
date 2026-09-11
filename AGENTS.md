# Project versioning

Keep this app in Git version control. For each completed, user-authorized change:

- Review the diff and run the checks appropriate to the change.
- Commit the relevant source, tests and documentation with a descriptive message.
- Push completed commits to the configured GitHub origin when access is available. Report any failed push.
- Do not commit signing keys, passwords, local SDK configuration, build outputs or IDE caches.
- Never rewrite shared history or force-push unless explicitly requested.
- Record user-facing changes in CHANGELOG.md. Increase Android versionCode for each new Play upload and versionName for the corresponding release.
- Use prerelease tags for unverified release candidates. Only tag a final release after its release build and required checks pass.

The initial repository snapshot is a release-preparation baseline, not a verified Google Play release. See docs/VALIDATION.md for the outstanding build and validation work.
