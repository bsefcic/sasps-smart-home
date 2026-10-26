# Arhitectură și decizii inițiale

## Două implementări, aceeași graniță publică

`implementations/baseline` folosește control centralizat și apeluri directe. `implementations/patterns` încapsulează acțiunile și adaptează API-urile producătorilor. Starterul demonstrează Command și Adapter pe un bec, nu implementează întregul proiect.

`shared/contracts` conține exclusiv DTO-uri și contractul public. `shared/simulators` conține API-uri fictive de producători, fără automatizări sau orchestrare. Nu partajăm motorul de comenzi, scenarii sau politicile, fiindcă tocmai acestea sunt comparate. DTO-ul `HomeCommand` nu este în sine implementarea GoF Command; aceasta este obiectul executabil din varianta patterns.

`tests/acceptance` execută aceleași cazuri pe fabricile ambelor variante. CLI-ul și benchmark-urile selectează varianta la graniță; codul unei variante nu o importă pe cealaltă. Ordinea dispozitivelor și rezultatele publice se stabilesc contractual.

## Decizii

1. Un singur repository, pentru onboarding și contracte comune; evaluarea AI folosește workspace-uri separate.
2. Un singur proces local, fără infrastructură externă. Node.js 24 execută TypeScript cu tipuri șterse, iar `tsc --noEmit` verifică separat tipurile.
3. API public mic; în Sprint 1 nu adăugăm extensibilitate speculativă. Compoziția și validarea se dezvoltă în stories dedicate.
4. Dependențele de dezvoltare sunt fixate prin package-lock; nu avem dependențe runtime în bootstrap.
5. Nu tratăm orice `switch` ca anti-pattern. Identificăm concret responsabilități amestecate, cuplare și propagarea schimbărilor pe măsură ce apar.

Orice schimbare importantă adaugă aici: context, alegere, alternativă respinsă și consecințe. Nu presupunem că mai multe clase sau patterns înseamnă calitate superioară.
