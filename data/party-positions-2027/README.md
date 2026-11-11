# Greek parliamentary election: party positions

Use [positions.json](positions.json) as the public database. It is a versioned JSON snapshot intended for a future Politeia reader; this change does not add a screen to the app. The roster is provisional: it includes the parliamentary groups and the named parties in the 10 November 2026 Interview/Political poll. The primary sources for that selection are in `roster_sources`. Coverage does not indicate candidacy or an official ballot. As new parties emerge and the ballot is confirmed, update the roster and register their official sources in [catalog.json](catalog.json).

Each reviewed position has a stable party and position ID, topic, Greek and English summaries, primary source URL, source type, review date and publication date when known. `standing_programme`, `statement` and `founding_declaration` are distinguished from `election_manifesto`; use the latter only when a party explicitly releases its programme for this election. A missing topic means no position was recorded for that topic.

The daily refresh checks registered sources and adds same-site policy links found on party programme and announcement pages. These links are listed under `new_documents` as `unreviewed`; the discovery date is not a publication date. Keyword discovery may miss documents or pick up related announcements. The job does not turn a new headline into a party position.

For reviewed positions, the refresh compares normalised HTML text and PDF links, or PDF bytes, with the baseline recorded on the first successful fetch. `unchanged` means the monitored content matched that baseline. `review_needed` flags a possible change for editorial review, including incidental page changes. `source_unavailable` preserves the last successful check and previous fingerprints. It does not imply the position was withdrawn. The refresh never overwrites a reviewed summary.

To update a summary, verify it against the linked original and edit the bilingual record in `catalog.json`. Set `reviewed_on` to the review date and, when using a revised document at the same URL, copy its verified `observed_digest` into `reviewed_digest`. To add a newly discovered position, give it a stable ID and cite the original document. The next refresh picks up these edits and removes discovered links already used as a reviewed source.

Run locally from the repo root:

```sh
python3 -m unittest discover -s scripts -p test_party_positions.py
python3 scripts/refresh_party_positions.py --validate-only
python3 scripts/refresh_party_positions.py
```

GitHub Actions runs the refresh daily at 04:23 UTC (06:23 or 07:23 in Athens), and can also be run manually. It commits only the generated snapshot. The schedule starts once the workflow is merged into the repository's default branch. GitHub Actions must be enabled and its token must be allowed to push data commits to that branch.
