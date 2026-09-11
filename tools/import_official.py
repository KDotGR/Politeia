"""Build offline, dated ballot catalogs and regression fixtures from Ministry archives.
Run from the working directory containing work/*-catalog-*.json (see provenance.json).
The app never executes remote JavaScript and does not require network access.
"""
import json,pathlib,urllib.request,concurrent.futures,unicodedata
W=pathlib.Path('work'); OUT=pathlib.Path('outputs/Politeia/app/src/main/assets'); OUT.mkdir(parents=True,exist_ok=True)
def read(p):return json.loads(p.read_text())
def latin(s):
 table={'Α':'A','Β':'V','Γ':'G','Δ':'D','Ε':'E','Ζ':'Z','Η':'I','Θ':'Th','Ι':'I','Κ':'K','Λ':'L','Μ':'M','Ν':'N','Ξ':'X','Ο':'O','Π':'P','Ρ':'R','Σ':'S','Τ':'T','Υ':'Y','Φ':'F','Χ':'Ch','Ψ':'Ps','Ω':'O','ς':'s'}
 table.update({k.lower():v.lower() for k,v in list(table.items())})
 return ''.join(table.get(c,c) for c in unicodedata.normalize('NFD',s) if not unicodedata.combining(c))
sets=[]; sources=[]
for key,rule,en,el,host,prefix,kind in [('june','PARLIAMENT','Parliament · June 2023','Βουλή · Ιούνιος 2023','https://ekloges-prev.singularlogic.eu','2023/june','v'),('may','SIMPLE','Parliament · May 2023','Βουλή · Μάιος 2023','https://ekloges-prev.singularlogic.eu','2023/may','v'),('eu','EU','European Parliament · 2024','Ευρωκοινοβούλιο · 2024','https://ekloges.ypes.gr','current','e')]:
 r=read(W/(key+'-result.json')); catalogs={l:read(W/(key+'-catalog-'+l+'.json'))['parties'] for l in ['en','el']}
 parties=[]
 for p in r['party']:
  id=str(p['PARTY_ID']);a=catalogs['en'].get(id,{});b=catalogs['el'].get(id,{})
  parties.append(dict(id=id,en=a.get('shortName',a.get('name',id)),el=b.get('shortName',b.get('name',id)),fullEn=a.get('name',id),fullEl=b.get('name',id),color='#'+a.get('color','64748B'),votes=p['VOTES'],seats=p.get('Edres',0)+p.get('EdresEpik',0),members=1))
 url=f'{host}/{prefix}/dyn1/{kind}/epik_1.js'
 sets.append(dict(id=key,rule=rule,en=en,el=el,seats=21 if key=='eu' else 300,valid=r['Egkyra'],parties=parties,source=url,updated=r['Updated']))
 sources.append(url)
base='https://ekloges-prev.singularlogic.eu/2023/october/'
localcatalog={l:read(W/('local-catalog-'+l+'.json')) for l in ['en','el']}
jobs=[]
for typ,dct in [('municipality',localcatalog['en']['municipalities']),('region',localcatalog['en']['regions'])]:
 for id in dct:
  code=('dhm_d_' if typ=='municipality' else 'snom_n_')+id
  for mode in ('dyn','stat'):jobs.append((typ,id,code,mode))
def fetch(job):
 typ,id,code,mode=job;p=W/'local-data'/f'{mode}-{code}.json'
 if not p.exists():
  for attempt in range(3):
   try:p.write_bytes(urllib.request.urlopen(base+mode+'/dn/'+code+'.js',timeout=25).read());break
   except Exception:
    if attempt==2:raise
 return p
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as ex:list(ex.map(fetch,jobs))
for typ,dct in [('municipality',localcatalog['en']['municipalities']),('region',localcatalog['en']['regions'])]:
 for id,c in dct.items():
  code=('dhm_d_' if typ=='municipality' else 'snom_n_')+id;r=read(W/'local-data'/f'dyn-{code}.json');m=read(W/'local-data'/f'stat-{code}.json')
  ps=[]
  for p in r['cds']:
   pid=str(p['CAND_ID']);n=m['cds'][pid];name=n.get('Synd') or n['Descr']
   ps.append(dict(id=pid,en=latin(name),el=name,fullEn=latin(n['Descr']),fullEl=n['Descr'],color=['#2979D0','#E56B6F','#08A88A','#D5A631','#9163C7','#C25C9A','#547786','#E18A45'][len(ps)%8],votes=p['AVOTES'],seats=p['BEdres'] if r['Status']==2 else p['AEdres'],members=1))
  ps.sort(key=lambda p:-p['votes'])
  winner=max(r['cds'],key=lambda p:p['BVOTES'] if r['Status']==2 else p['AVOTES'])
  areaEn=m.get('ElectionAreaEn',c['name']);areaEl=m.get('ElectionAreaGr',m['Descr']);url=base+'dyn/dn/'+code+'.js'
  sets.append(dict(id=typ+'-'+id,rule='LOCAL',kind=typ,en=areaEn+' · 2023',el=areaEl+' · 2023',seats=m['Edres'],valid=r['AEgkyra'],parties=ps,winner=str(winner['CAND_ID']),population=m.get('Population',0),units=m.get('CountDen',1),regionId=int(id) if typ=='region' else c['regionId'],source=url,updated=r['AUpdated']))
  sources.append(url)
(OUT/'elections.json').write_text(json.dumps(sets,ensure_ascii=False,separators=(',',':')))
(OUT/'provenance.json').write_text(json.dumps(dict(retrieved='2026-09-10',publisher='Hellenic Ministry of Interior / SingularLogic',catalogMeaning='Complete published ballot lists for the dated election; not a live registry or a declaration of eligibility for a future election.',localEnglish='Transliteration of official Greek ballot names; place names from official English catalog.',sources=sources),ensure_ascii=False,indent=2))
fixtures=pathlib.Path('outputs/Politeia/core/src/test/resources');fixtures.mkdir(parents=True,exist_ok=True)
with (fixtures/'historical.tsv').open('w') as f:
 for e in sets:
  f.write('\t'.join([e['id'],e['rule'],str(e['seats']),str(e['valid']),str(next((i for i,p in enumerate(e['parties']) if p['id']==e.get('winner')), '')),','.join(str(p['votes']) for p in e['parties']),','.join(str(p['seats']) for p in e['parties'])])+'\n')
print('Datasets',len(sets),'parties',sum(len(e['parties']) for e in sets),'bytes',(OUT/'elections.json').stat().st_size)
for e in sets[:3]:print(e['id'],e['valid'],sum(p['votes'] for p in e['parties']),[(p['en'],p['votes'],p['seats']) for p in e['parties'][:8]])
