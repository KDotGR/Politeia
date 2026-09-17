#!/usr/bin/env python3
"""Check tracked files for common credentials without printing secret values."""
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
SECRET_NAME = re.compile(r'(^|/)(?:\.local|\.ssh)(?:/|$)|(?:^|/)(?:keystore|local)\.properties$|(?:^|/)\.env(?:\.|$)|(?:password|credentials|service[-_]account).*\.(?:txt|json|properties)$|\.(?:jks|keystore|p12|pfx|pem|key)$', re.I)
SECRET_CONTENT = [
    re.compile(rb'-----BEGIN (?:RSA |EC |OPENSSH |DSA |ENCRYPTED )?PRIVATE KEY-----'),
    re.compile(rb'(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{50,})'),
    re.compile(rb'AKIA[0-9A-Z]{16}'),
]


def main():
    names = subprocess.check_output(['git', 'ls-files', '-z'], cwd=ROOT).decode().split('\0')
    failures = []
    for name in filter(None, names):
        if SECRET_NAME.search(name) and not name.endswith('.example'):
            failures.append(name)
            continue
        path = ROOT / name
        if path.is_file():
            data = path.read_bytes()
            assignments = re.findall(rb'(?m)^[ \t]*(?:storePassword|keyPassword)[ \t]*=[ \t]*(.*)$', data)
            private_password = any(value.strip() and not (name.endswith('.example') and re.fullmatch(rb'REPLACE_WITH_[A-Z_]+', value.strip())) for value in assignments)
            if private_password or any(pattern.search(data) for pattern in SECRET_CONTENT):
                failures.append(name)
    if failures:
        print('Potential private files detected; review before publishing:', file=sys.stderr)
        for name in failures:
            print(' - ' + name, file=sys.stderr)
        return 1
    print('Tracked-file credential check passed. This check does not detect every secret format.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
