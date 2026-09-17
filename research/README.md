# Research material

`raw/` contains original downloaded election data and reference documents. `legacy-tools/` retains one-off research scripts for historical reference. Both directories are local, ignored archives.

The supported importer is `tools/import_official.py`. It resolves all paths relative to the repository root and reads `raw/`; it may download missing local-election files. It is not required for normal builds or tests.
