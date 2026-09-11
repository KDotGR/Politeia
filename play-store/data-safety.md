# Data Safety declaration worksheet

Complete this worksheet against the exact release build in Play Console. It is a developer aid, not a substitute for the Play Console questionnaire.

## Current build inventory

- No account creation, login, contacts, payments, advertising SDK, analytics SDK, crash-reporting SDK, or user profile.
- Election inputs, selected parties, custom rules, language and sound preferences are stored locally on the device.
- The app has network access for published source links and optional poll refresh. It does not intentionally collect or share personal data.
- The app can schedule a background refresh service for poll data when the feature is enabled.
- Source websites may receive ordinary request metadata when a link is opened or refreshed. Those websites have their own privacy policies.

Review every answer after adding or upgrading any dependency. If analytics, crash reporting, accounts or remote storage are added, update the declaration and privacy policy before release.
