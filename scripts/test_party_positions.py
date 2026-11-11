import unittest
from datetime import date
from urllib.error import URLError

from refresh_party_positions import refresh, validate


SOURCE = 'https://party.example/policy/'
INDEX = 'https://party.example/news/'
TODAY = date(2026, 11, 11)
CATALOG = {'schema_version': 1, 'election': 'gr-parliament-2027', 'parties': [{
    'id': 'party', 'name': {'el': 'Κόμμα', 'en': 'Party'},
    'discovery_urls': [INDEX], 'positions': [{
        'id': 'housing', 'topic': 'housing', 'kind': 'statement',
        'summary': {'el': 'Στέγη', 'en': 'Housing'}, 'source_url': SOURCE,
        'reviewed_on': '2026-11-11', 'published_on': None,
    }],
}]}


def page(content):
    return f'<html><body><main>{content}</main></body></html>'.encode(), 'text/html'


class RefreshTest(unittest.TestCase):
    def test_refresh_discovers_official_documents_and_flags_revised_evidence(self):
        docs = {SOURCE: page('<p>Proposes 100 public homes.</p>'), INDEX: page('')}
        initial = refresh(CATALOG, {}, TODAY, docs.__getitem__)
        self.assertEqual(initial['parties'][0]['positions'][0]['source_status'], 'unchanged')
        docs[SOURCE] = page('<p>Proposes 200 public homes.</p>')
        docs[INDEX] = page('''<a href="/new-plan/?utm_source=x">Προγραμματικές θέσεις για τη στέγη</a>
            <a href="https://other.example/plan">Νέο πρόγραμμα</a>
            <a href="/sports/">Αθλητικά αποτελέσματα</a>''')
        updated = refresh(CATALOG, initial, TODAY, docs.__getitem__)
        party = updated['parties'][0]
        self.assertEqual([d['url'] for d in party['new_documents']], ['https://party.example/new-plan/'])
        position = party['positions'][0]
        self.assertEqual(position['source_status'], 'review_needed')
        self.assertEqual(position['summary']['en'], 'Housing')
        self.assertEqual(refresh(CATALOG, updated, TODAY, docs.__getitem__), updated)

    def test_network_failure_preserves_last_success_and_candidates(self):
        docs = {SOURCE: page('<p>Housing proposal.</p>'), INDEX: page('<a href="/plan.pdf">Πρόγραμμα</a>')}
        initial = refresh(CATALOG, {}, TODAY, docs.__getitem__)

        self.assertEqual(len(initial['parties'][0]['new_documents']), 1)

        def unavailable(url):
            raise URLError('unavailable')

        party = refresh(CATALOG, initial, date(2026, 11, 12), unavailable)['parties'][0]
        self.assertEqual(party['new_documents'], initial['parties'][0]['new_documents'])
        self.assertEqual(party['positions'][0]['source_status'], 'source_unavailable')
        self.assertEqual(party['positions'][0]['last_source_success'], '2026-11-11')

    def test_rejects_non_https_source(self):
        with self.assertRaises(ValueError):
            validate({**CATALOG, 'parties': [{**CATALOG['parties'][0], 'discovery_urls': ['http://party.example/']}]})


if __name__ == '__main__':
    unittest.main()
