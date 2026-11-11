#!/usr/bin/env python3
"""Build the public 2027 party data from reviewed entries and official-source observations."""

import argparse
from concurrent.futures import ThreadPoolExecutor
from datetime import date, datetime
import hashlib
from html.parser import HTMLParser
import json
from pathlib import Path
import re
import tempfile
import unicodedata
from urllib.parse import quote, urljoin, urlsplit, urlunsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener
from zoneinfo import ZoneInfo

DATA = Path(__file__).resolve().parents[1] / 'data/party-positions-2027'
MAX_BYTES = 32 * 1024 * 1024
KINDS = {'standing_programme', 'statement', 'founding_declaration', 'election_manifesto'}
POLICY_WORDS = re.compile(r'προγραμματικ|κυβερνητικ.{0,4} προγραμμα|εκλογικ.{0,4} προγραμμα|'
                          r'θεσεισ (για|μασ)|προτασ(η|εισ) (για|μασ)|ιδρυτικ.{0,4} διακηρυξ|'
                          r'^προγραμμα$|προταση νομου|manifesto|policy proposal|policy platform')


def secure_url(url):
    p = urlsplit(url)
    if p.scheme != 'https' or not p.hostname or p.username or p.password or p.port not in (None, 443):
        raise ValueError(f'Expected public HTTPS source: {url}')
    return p


def site(url):
    return secure_url(url).hostname.removeprefix('www.')


def validate(catalog):
    if catalog['schema_version'] != 1 or catalog['election'] != 'gr-parliament-2027' or not catalog['parties']:
        raise ValueError('Unexpected catalog schema or election')
    parties = set()
    for party in catalog['parties']:
        identifier = party['id']
        if identifier in parties or not re.fullmatch(r'[a-z0-9-]+', identifier):
            raise ValueError(f'Invalid or duplicate party ID: {identifier}')
        parties.add(identifier)
        if not all(party['name'][lang].strip() for lang in ('el', 'en')):
            raise ValueError(f'Missing party name: {identifier}')
        if not party['discovery_urls']:
            raise ValueError(f'Missing discovery source: {identifier}')
        for url in party['discovery_urls']:
            secure_url(url)
        ids = set()
        for position in party['positions']:
            if (position['id'] in ids or not position['id'] or not position['topic']
                    or position['kind'] not in KINDS):
                raise ValueError(f'Invalid position: {identifier}/{position["id"]}')
            ids.add(position['id'])
            secure_url(position['source_url'])
            if not all(position['summary'][lang].strip() for lang in ('el', 'en')):
                raise ValueError(f'Missing bilingual position: {identifier}/{position["id"]}')
            reviewed = date.fromisoformat(position['reviewed_on'])
            if position['published_on'] and date.fromisoformat(position['published_on']) > reviewed:
                raise ValueError(f'Publication date is after review: {identifier}/{position["id"]}')
            if 'reviewed_digest' in position and not re.fullmatch(r'[a-f0-9]{64}', position['reviewed_digest']):
                raise ValueError(f'Invalid reviewed digest: {identifier}/{position["id"]}')


class SameSiteRedirect(HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        if site(newurl) != site(req.full_url):
            raise ValueError('Source redirected to a different site')
        return super().redirect_request(req, fp, code, msg, headers, newurl)


def fetch(url):
    secure_url(url)
    url = quote(url, safe=":/?&=%#[]@!$'()*+,;")
    request = Request(url, headers={'User-Agent': 'Politeia/1.0 (public party policy source monitor)'})
    with build_opener(SameSiteRedirect()).open(request, timeout=15) as response:
        body = response.read(MAX_BYTES + 1)
        if len(body) > MAX_BYTES:
            raise ValueError('Source exceeds download limit')
        mime = response.headers.get_content_type()
        if mime == 'text/html':
            body = body.decode(response.headers.get_content_charset() or 'utf-8', errors='replace').encode()
        return body, mime


class Page(HTMLParser):
    ignored = {'script', 'style', 'noscript', 'nav', 'footer', 'header', 'aside', 'svg', 'template'}

    def __init__(self, body):
        super().__init__(convert_charrefs=True)
        self.skip = []
        self.main = 0
        self.all_text, self.main_text, self.links = [], [], []
        self.anchor = None
        self.feed(body.decode('utf-8', errors='replace'))
        self.close()

    def handle_starttag(self, tag, attrs):
        if tag in self.ignored:
            self.skip.append(tag)
        if self.skip:
            return
        if tag == 'main':
            self.main += 1
        if tag == 'a':
            self.anchor = (dict(attrs).get('href') or '', [])

    def handle_endtag(self, tag):
        if self.skip:
            if tag == self.skip[-1]:
                self.skip.pop()
            return
        if tag == 'main':
            self.main = max(0, self.main - 1)
        if tag == 'a' and self.anchor is not None:
            href, parts = self.anchor
            self.links.append((href, clean(' '.join(parts))))
            self.anchor = None

    def handle_data(self, text):
        if not self.skip:
            self.all_text.append(text)
            if self.main:
                self.main_text.append(text)
            if self.anchor is not None:
                self.anchor[1].append(text)


def clean(text):
    return ' '.join(unicodedata.normalize('NFC', text).split())


def normalized_words(text):
    return ''.join(c for c in unicodedata.normalize('NFD', text.casefold()) if not unicodedata.combining(c))


def observation(url, fetcher):
    try:
        body, mime = fetcher(url)
        if body.startswith(b'%PDF-'):
            return hashlib.sha256(body).hexdigest(), None
        if mime != 'text/html':
            raise ValueError('Unsupported document type')
        page = Page(body)
        words = clean(' '.join(page.main_text or page.all_text))
        # Include PDF destinations so replacement attachments trigger review too.
        attachments = sorted({href for href, _ in page.links if urlsplit(href).path.lower().endswith('.pdf')})
        if not words:
            raise ValueError('No readable text')
        return hashlib.sha256(json.dumps([words, attachments], ensure_ascii=False).encode()).hexdigest(), page
    except (OSError, ValueError, LookupError) as error:
        print(f'Source unavailable: {url} ({type(error).__name__})')
        return None, None


def candidates(index, page):
    for href, title in page.links:
        if not href or not POLICY_WORDS.search(normalized_words(title)):
            continue
        try:
            link = secure_url(urljoin(index, href))
            if site(link.geturl()) != site(index) or link.path in ('', '/', urlsplit(index).path):
                continue
        except ValueError:
            continue
        yield urlunsplit((link.scheme, link.netloc, link.path, '', '')), title[:200]


def refresh(catalog, previous, today, fetcher=fetch):
    validate(catalog)
    day = today.isoformat()
    urls = sorted({url for party in catalog['parties'] for url in party['discovery_urls']}
                  | {p['source_url'] for party in catalog['parties'] for p in party['positions']})
    with ThreadPoolExecutor(max_workers=6) as pool:
        observations = dict(zip(urls, pool.map(lambda url: observation(url, fetcher), urls)))
    old_parties = {p['id']: p for p in previous.get('parties', [])}
    parties = []
    for party in catalog['parties']:
        old = old_parties.get(party['id'], {})
        old_positions = {p['id']: p for p in old.get('positions', [])}
        old_checks = {p['url']: p for p in old.get('discovery_checks', [])}
        documents = {d['url']: d for d in old.get('new_documents', [])}
        source_urls = {p['source_url'] for p in party['positions']}
        checks = []
        for url in party['discovery_urls']:
            _, page = observations[url]
            checks.append({'url': url, 'status': 'checked' if page else 'source_unavailable',
                           'last_success': day if page else old_checks.get(url, {}).get('last_success')})
            if page:
                for link, title in candidates(url, page):
                    if link not in source_urls:
                        first_seen = documents.get(link, {}).get('first_seen', day)
                        documents[link] = {'url': link, 'title': title, 'first_seen': first_seen,
                                           'discovered_from': url, 'status': 'unreviewed'}
        positions = []
        for position in party['positions']:
            url = position['source_url']
            last = old_positions.get(position['id'], {})
            if last.get('source_url') != url:
                last = {}
            digest, _ = observations[url]
            baseline = position.get('reviewed_digest') or last.get('baseline_digest') or digest
            status = 'source_unavailable' if digest is None else 'unchanged' if digest == baseline else 'review_needed'
            positions.append({**position, 'source_status': status, 'baseline_digest': baseline,
                              'observed_digest': digest or last.get('observed_digest'),
                              'last_source_success': day if digest else last.get('last_source_success')})
        documents = sorted((d for d in documents.values() if d['url'] not in source_urls
                            and d['discovered_from'] in party['discovery_urls']),
                           key=lambda d: (d['first_seen'], d['url']), reverse=True)[:40]
        parties.append({**party, 'positions': positions, 'discovery_checks': checks, 'new_documents': documents})
    return {**catalog, 'checked_on': day, 'parties': parties}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--validate-only', action='store_true', help='Validate the reviewed catalog without fetching')
    args = parser.parse_args()
    catalog = json.loads((DATA / 'catalog.json').read_text(encoding='utf-8'))
    if args.validate_only:
        validate(catalog)
        return
    path = DATA / 'positions.json'
    previous = json.loads(path.read_text(encoding='utf-8')) if path.exists() else {}
    snapshot = refresh(catalog, previous, datetime.now(ZoneInfo('Europe/Athens')).date())
    with tempfile.NamedTemporaryFile(mode='w', encoding='utf-8', dir=DATA, delete=False) as output:
        output.write(json.dumps(snapshot, ensure_ascii=False, indent=2) + '\n')
    Path(output.name).replace(path)
    count = sum(len(p['positions']) for p in snapshot['parties'])
    unavailable = sum(p['source_status'] == 'source_unavailable' for party in snapshot['parties'] for p in party['positions'])
    unavailable += sum(c['status'] == 'source_unavailable' for party in snapshot['parties'] for c in party['discovery_checks'])
    print(f'{len(snapshot["parties"])} parties, {count} reviewed positions; {unavailable} unavailable checks')


if __name__ == '__main__':
    main()
