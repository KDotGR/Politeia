import contextlib
import io
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import check_public_files as checker


class PublicFilesTest(unittest.TestCase):
    def check(self, name, content):
        with tempfile.TemporaryDirectory() as folder:
            root = Path(folder)
            target = root / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(content)
            with patch.object(checker, 'ROOT', root), patch.object(checker.subprocess, 'check_output', return_value=(name+'\0').encode()), contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
                return checker.main()

    def test_public_documentation(self):
        self.assertEqual(self.check('README.md', 'Build using your own signing key.'), 0)

    def test_private_directory(self):
        self.assertEqual(self.check('.local/signing/password.txt', 'dummy'), 1)

    def test_token_in_ordinary_file(self):
        self.assertEqual(self.check('notes.md', 'gh'+'p_'+'x'*36), 1)

    def test_signing_password_in_ordinary_file(self):
        self.assertEqual(self.check('notes.txt', 'store'+'Password=dummy'), 1)

    def test_token_in_template(self):
        self.assertEqual(self.check('.env.example', 'gh'+'p_'+'x'*36), 1)

    def test_placeholder_template(self):
        self.assertEqual(self.check('keystore.properties.example', 'store'+'Password=REPLACE_WITH_STORE_PASSWORD'), 0)


if __name__ == '__main__':
    unittest.main()
