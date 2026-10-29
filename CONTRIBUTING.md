# Contribuții

## Onboarding

Java 17+, Maven Wrapper 3.9.11. Din rădăcină: `./mvnw clean verify` sau Windows `.\mvnw.cmd clean verify`. Formatare: `./mvnw spotless:apply`. Importă pom.xml în IDE. Nu instala Maven separat.

Rulează `java -jar server/target/smart-home-server.jar`. Pentru baseline folosește `--smart-home.variant=baseline --server.port=8081`. Verifică GET /api/v1/health și exemplele HTTP din docs/http/smart-home.http. Nu sunt necesare React, hardware, DB sau API keys.

## Înainte de cod

Citește docs/contracts-v1.md și docs/tasks-sprint-1.md: forma JSON, erorile, inventarul, semantica scenariilor și API-ul Vendor A sunt decise. Nu inventa variații și nu aștepta un task separat de proiectare. Contractele și starterul Spring sunt livrate prin PR-ul curent; taskurile de implementare pornesc după integrarea lui.

## Workflow

- Preia un issue cu dependențele integrate; anunță că lucrezi la el. Membrii se atribuie ulterior.
- Branch separat din main actualizat: feat/<issue>-short-name, fix/... sau docs/.... Până la integrarea bootstrap-ului coordonează explicit pornirea din branch-ul PR-ului.
- Un task public acoperă ambele engines, unde este precizat. Nu adăuga logica comparată în server/shared.
- PR-ul explică schimbarea, issue-ul, verificările și limitele. Un coleg diferit de autor face review.
- Fără modificări directe în main, refactorizări fără legătură sau introducerea features rezervate evaluării AI.

## Definition of Ready

Issue-ul are specificație fixă, exemple, criterii și dependențe. «Complet specificat» nu înseamnă că se poate începe înaintea dependențelor de implementare. Nu există decizii de interfață lăsate autorului unui story.

## Definition of Done

- Comportamentul și erorile contractuale au teste relevante; aceleași cazuri trec pe ambele variante.
- ./mvnw clean verify și CI sunt verzi. Testele HTTP verifică starea și codul de răspuns, nu doar că serverul pornește.
- Documentația descrie exact ce este implementat și ce încă returnează 501. La implementarea unui feature, testul temporar 501 este înlocuit explicit cu succes/erori conform contractului.
- Review rezolvat și PR integrat. Abia atunci se închide taskul; trackerul sprintului rămâne deschis până la final.
- Rezultatele au commit, comandă și condiții reale, fără cifre inventate.

Aceste fișiere nu activează branch protection. Codul și package-urile sunt în engleză; documentația în română. Spring rămâne în server, DTO-urile nu depind de Jackson, iar engines nu se importă reciproc. Vezi instrucțiunile locale și docs/ai-workflow.md.
