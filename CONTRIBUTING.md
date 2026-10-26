# Contribuții

## Pornire

Instalează Node.js 24 și rulează `npm ci`, apoi `npm run check`. Pe macOS/Linux poți folosi `nvm install` și `nvm use`; pe Windows instalează aceeași versiune majoră prin managerul preferat. Comenzile npm sunt identice pe toate platformele. Nu sunt necesare Docker, baze de date, API keys sau hardware.

## Un issue → un branch → un PR

- Preia un issue Ready și anunță în el că lucrezi la el; atribuirea nominală se face după adăugarea membrilor.
- Pleacă din `main` actualizat, după integrarea bootstrap-ului. Branch: `feat/<issue-number>-short-name`, `fix/...` sau `docs/...`.
- Dacă PR-ul bootstrap nu este încă integrat, așteaptă integrarea sa sau coordonează explicit o ramificare din acel branch. Nu recrea independent structura.
- Un task care schimbă comportamentul public acoperă ambele variante, cu excepția cazului în care issue-ul spune altceva.
- Descrie PR-ul prin comportamentul obținut, issue-ul (`Closes #...`) și verificări. Un coleg diferit de autor face review.
- Păstrează commit-urile mici și nu include refactorizări fără legătură. Nu lucra direct pe `main`.

## Definition of Ready

Issue-ul are scop, criterii de acceptanță, dependențe și variante afectate. Contractele necesare sunt stabilite, iar dependențele sunt integrate. Orele sunt estimări orientative, nu promisiuni.

## Definition of Done

- Criteriile de acceptanță sunt demonstrate; comportamentul comun are teste.
- `npm run check` trece și CI este verde. În PR se menționează eventualele verificări care nu au putut fi rulate.
- Documentația/comenzile afectate sunt actualizate.
- Un coleg a făcut review; observațiile importante sunt rezolvate și PR-ul este integrat.
- Rezultatele măsurate au comandă, commit și condiții de rulare; nu inventăm cifre.

Aceasta este convenția echipei. Protecțiile de branch și aprobările obligatorii nu sunt configurate automat de aceste fișiere.

## Cod și teste

Codul, numele și mesajele de commit sunt în engleză. Documentația de curs este în română. Folosește tipuri stricte, fără `any`, valori globale mutabile sau dependențe ascunse. Verifică erorile și păstrează izolarea instanțelor. Nu adăuga infrastructură distribuită pentru simulator.

Unit tests pot fi specifice variantei. Testele comune verifică API-ul public, nu numele claselor. O schimbare legitimă de contract se discută și se aplică în ambele variante; în experimentele controlate agentul nu poate modifica evaluatorul.

Vezi `docs/ai-workflow.md` pentru folosirea AI.
