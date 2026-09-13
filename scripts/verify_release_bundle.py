#!/usr/bin/env python3
"""Verify that the release bundle carries its matching R8 deobfuscation map."""

from pathlib import Path
import re
import sys
import zipfile


PROJECT_ROOT = Path(__file__).resolve().parents[1]
BUNDLE = PROJECT_ROOT / "app/build/outputs/bundle/release/app-release.aab"
MAPPING = PROJECT_ROOT / "app/build/outputs/mapping/release/mapping.txt"
EMBEDDED_MAPPING = "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map"
ENGINE_CLASS = "gr.politeia.core.ElectionEngine"


def verify_release_bundle() -> None:
    """Fail unless the generated and embedded maps match and rename the engine."""
    if not BUNDLE.is_file():
        raise ValueError("Release AAB is missing; build bundleRelease first.")
    if not MAPPING.is_file():
        raise ValueError("R8 mapping.txt is missing; release obfuscation is required.")

    generated = MAPPING.read_bytes()
    if not generated.strip():
        raise ValueError("Generated R8 mapping.txt is empty.")

    with zipfile.ZipFile(BUNDLE) as bundle:
        if bundle.namelist().count(EMBEDDED_MAPPING) != 1:
            raise ValueError("Release AAB must contain exactly one embedded R8 map.")
        embedded = bundle.read(EMBEDDED_MAPPING)

    if not embedded.strip():
        raise ValueError("Embedded R8 map is empty.")
    if embedded != generated:
        raise ValueError("Embedded R8 map does not match release mapping.txt.")

    for component in ("gr.politeia.app.MainActivity", "gr.politeia.app.PollUpdateService"):
        if f"{component} -> {component}:" not in generated.decode("utf-8").splitlines():
            raise ValueError("Manifest component names must remain stable, including Activity preferences.")

    class_mapping = re.search(
        rf"^{re.escape(ENGINE_CLASS)} -> ([^:\r\n]+):\r?$",
        generated.decode("utf-8"),
        flags=re.MULTILINE,
    )
    if class_mapping is None:
        raise ValueError("R8 map is missing the ElectionEngine class mapping.")
    if class_mapping.group(1).strip() == ENGINE_CLASS:
        raise ValueError("ElectionEngine was not renamed; obfuscation is not active.")


def main() -> int:
    try:
        verify_release_bundle()
    except (ValueError, OSError, UnicodeError, zipfile.BadZipFile, RuntimeError) as error:
        print(f"Release bundle verification failed: {error}", file=sys.stderr)
        return 1
    print("Release bundle verified: matching embedded R8 map and renamed ElectionEngine.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
