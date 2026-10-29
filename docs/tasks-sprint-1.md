# Sprint 1 — taskuri gata de implementare

Specificația este fixată în [contractul v1](contracts-v1.md). Nu mai există un task de alegere a API-urilor. #2 este livrat prin PR-ul Spring; celelalte sunt complet specificate, dar se preiau după integrarea dependențelor lor. Estimările sunt orientative, nu obligații individuale. Membrii și absențele se stabilesc ulterior.

## [S1] 01 — Contract v1 decis și implementat în bootstrap-ul Spring

**Pistă:** Livrat prin bootstrap. **Estimare:** 0 h rămase după merge. **Dependențe:** integrarea PR-ului Spring.

### Pe scurt
Contractele nu mai trebuie alese de echipă. Sunt fixate în docs/contracts-v1.md, OpenAPI, records Java și exemplele JSON. Acest issue este livrat de noul PR Spring și se închide la integrarea lui.

### Exemplu
Comandă fixă: `{"type":"set-target-temperature","deviceId":"living-thermostat","temperature":22}`. Intervalul este 5–35°C, pas 0.1; lista unui scenariu are 1–50 comenzi și nu modifică nimic dacă este invalidă.

### Pași concreți
1. Citește docs/contracts-v1.md și docs/tasks-sprint-1.md: acestea sunt specificația, nu propuneri.
2. La review verifică numai că fișierele și exemplele sunt coerente; nu mai este necesară o ședință de alegere a câmpurilor/API-urilor.
3. După integrarea bootstrap-ului începe implementarea #3 sau verificarea independentă #10.

### Criterii de acceptanță
- [x] Endpoint-uri, comenzi, răspunsuri și erori v1 fixate și versionate.
- [x] Records Java pentru comenzi, starea dispozitivelor și scenarii; constrângeri de ID/temperatură/listă implementate.
- [x] Inventar-țintă și API Vendor A exacte; fixtures și exemple HTTP incluse.
- [x] Stadiul real (bec funcțional; termostat/scenarii încă 501) este explicit, fără a pretinde features gata.

### Fișiere și livrare
docs/contracts-v1.md; server/src/main/resources/static/openapi.yaml; shared/contracts; acceptance-tests/src/test/resources/fixtures; docs/http/smart-home.http.

### Nu intră în task
Nu implementa termostatul/scenariile aici și nu reproiecta contractul. PR-ul livrează decizia și infrastructura, nu toate stories.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 02 — Implementarea simulatorului VendorAThermostat după contract

**Pistă:** A — dispozitive. **Estimare:** 2–3. **Dependențe:** #2.

### Pe scurt
Adaugă termostatul fictiv al producătorului, în Java simplu. API-ul este deja decis; ambele engines vor folosi exact acest simulator.

### Exemplu
22.1°C în aplicație înseamnă 221 deci-grade în simulator. Două instanțe nu își schimbă reciproc temperatura.

### Pași concreți
1. În shared/simulators adaugă VendorAThermostat(String id), cu id() și temperatură țintă inițială 200 deci-grade.
2. Implementează setTargetDeciCelsius(int) și readTargetDeciCelsius(). Acceptă 50–350 inclusiv; în afara intervalului aruncă IllegalArgumentException fără schimbarea valorii.
3. Păstrează VendorALight și API-ul său existent: setPower(Power), readPower(), id(); implicit DISABLED.
4. Adaugă JUnit doar ca dependență de test a modulului, fără versiune proprie (este gestionată de BOM).
5. Testează inițializarea, limitele 50/350, setarea 221, erorile 49/351 și independența instanțelor.

### Criterii de acceptanță
- [ ] VendorAThermostat are exact metodele și unitățile de mai sus.
- [ ] Inputul invalid nu schimbă valoarea anterioară.
- [ ] Teste unitare pe interval și izolare; Maven verify trece.
- [ ] Nicio dependență Spring/Jackson și nicio orchestrare în simulator.

### Fișiere și livrare
shared/simulators/src/main/java/ro/upb/sasps/smarthome/simulators/VendorAThermostat.java; testul corespondent din src/test/java; shared/simulators/pom.xml.

### Nu intră în task
Fără senzori, alți producători, întârzieri sau defecte simulate. Nu modifica formatul HTTP.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 03 — Thermostat și VendorAThermostatAdapter în patterns

**Pistă:** A — integrări. **Estimare:** 2–3. **Dependențe:** #3.

### Pe scurt
Adaugă adaptorul termostatului. El traduce Celsius din aplicație în deci-gradele Vendor A. Numele interfeței și metodelor sunt deja stabilite.

### Exemplu
adapter.setTargetTemperature(22.1) setează 221 în vendor; adapter.targetTemperature() citește 221 și întoarce 22.1.

### Pași concreți
1. În package-ul patterns adaugă interfața Thermostat: String id(), void setTargetTemperature(double), double targetTemperature().
2. Adaugă VendorAThermostatAdapter(VendorAThermostat vendor), primit prin constructor.
3. La setare, folosește ContractChecks.requireTemperature(value), apoi convertește valoarea validă în deci-grade; la citire împarte la 10.0. Nu rotunji un input invalid pentru a-l accepta.
4. Testează conversia 22.1↔221, limitele, ID-ul și respingerea 22.25/NaN/Infinity fără efecte.
5. Păstrează adaptorul de bec și descrie pe scurt în docs/architecture.md ce API ascunde fiecare.

### Criterii de acceptanță
- [ ] Interfața și adaptorul au semnăturile fixate.
- [ ] Conversia păstrează exact pasul contractual de 0.1°C.
- [ ] Valorile invalide sunt respinse, nu corectate implicit.
- [ ] Teste unitare și Maven verify trec; fără import din baseline.

### Fișiere și livrare
implementations/patterns/src/main/java/ro/upb/sasps/smarthome/patterns/Thermostat.java și VendorAThermostatAdapter.java; teste în același modul.

### Nu intră în task
Fără Abstract Factory/Bridge, Spring sau al doilea producător. Baseline folosește simulatorul direct, nu acest adaptor.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 04 — Control manual complet pentru bec și termostat în ambele engines

**Pistă:** B — comenzi. **Estimare:** 3–4. **Dependențe:** #3, #4.

### Pe scurt
Completează engines, nu controllerele Spring: ambele trebuie să controleze termostatul și să întoarcă aceleași rezultate conform contractului deja fixat.

### Exemplu
POST /api/v1/commands cu temperatura 22 trebuie să treacă de la 501 la 200. Starea-țintă inițială conține living-light oprit și living-thermostat la 20.0.

### Pași concreți
1. În BaselineHome și PatternsHome adaugă exact living-thermostat (Vendor A, inițial 200 deci-grade). Păstrează living-light oprit.
2. Baseline tratează SetTargetTemperature prin control direct. Patterns adaugă SetTargetTemperatureCommand(Thermostat,double), cu execute(), și folosește adaptorul din #4.
3. Verifică inventarul înainte de execuție: ID inexistent → CommandResult.unknownDevice(id); ID existent cu tip greșit → CommandResult.incompatibleDevice(id,type).
4. getSnapshot() întoarce LightSnapshot/ThermostatSnapshot în ordine crescătoare după ID, fără referințe mutabile.
5. Completează aceleași teste core pentru ambele engines: setări, 5/35°C, repetarea valorii, ID necunoscut, incompatibilitate și lipsa efectelor.
6. În testele HTTP elimină doar așteptarea temporară 501 pentru temperatură, înlocuind-o cu 200/404/422 conform contractului. Scenariile rămân 501 până la #6.

### Criterii de acceptanță
- [ ] Inventarul și snapshot-ul corespund fixtures/responses/s1-initial-home.json.
- [ ] Temperatura funcționează identic în ambele engines și persistă între requests.
- [ ] UNKNOWN_DEVICE și INCOMPATIBLE_DEVICE sunt corecte și fără efecte.
- [ ] Comenzile și testele actualizate trec prin Maven verify.

### Fișiere și livrare
BaselineHome, PatternsHome și noul SetTargetTemperatureCommand; acceptance-tests/HomeAcceptanceTest; server/HttpContractTest.

### Nu intră în task
Nu modifica DTO-uri/endpoints, nu adăuga registru dinamic, undo sau senzori. Constraint-urile primitive sunt deja implementate în contracte.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 05 — Scenarii plate cu validare completă înainte de execuție

**Pistă:** C — scenarii. **Estimare:** 3–4. **Dependențe:** #5.

### Pe scurt
Implementează executeScene(SceneRequest) în ambele engines. Nu alegi semantica: lista are 1–50 comenzi, se prevalidează integral și se execută în ordine.

### Exemplu
Scenariul setează becul la true, apoi folosește un device inexistent. Răspunsul trebuie să fie 404 UNKNOWN_DEVICE cu commandIndex=1, iar becul să rămână ca înainte.

### Pași concreți
1. Override executeScene(SceneRequest) în BaselineHome și PatternsHome; înlocuiește rezultatul temporar NOT_IMPLEMENTED.
2. Verifică existența și compatibilitatea TUTUROR comenzilor în ordinea lor, fără să execuți nimic. Returnează prima eroare prin result.atCommand(index), index zero-based.
3. În baseline folosește control centralizat: validare, apoi execuție. În patterns construiește/validează comenzile înainte de execuție și adaugă SceneCommand(List<Command>) implements Command pentru delegarea execuției.
4. La succes întoarce CommandResult.success(); HomeSession construiește deja răspunsul HTTP cu executedCommands și snapshot sub lock.
5. Testează ordinea prin două setări ale aceluiași dispozitiv; ultima rămâne. Testează o eroare la mijloc și compară snapshot-ul înainte/după.
6. În testele HTTP înlocuiește 501 de la scene cu testele finale 200/404/422. Actualizează stadiul din README/contract/OpenAPI după ce feature-ul este gata.

### Criterii de acceptanță
- [ ] Scenariile valide au același efect în ambele engines.
- [ ] Inputul invalid, ID necunoscut și tipul incompatibil nu lasă modificări parțiale.
- [ ] commandIndex indică exact comanda invalidă din listă.
- [ ] Ordinea și repetarea aceluiași dispozitiv sunt respectate; verify trece.

### Fișiere și livrare
BaselineHome, PatternsHome, noul patterns/SceneCommand.java; teste core și HTTP; fixtures/scenes.

### Nu intră în task
Fără nesting, stocarea scenariilor, cicluri, Saga sau rollback hardware. Scenariile sunt trimise în request; numele este doar etichetă.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 06 — Exemple REST și teste HTTP finale pentru contractul v1

**Pistă:** C — integrare web. **Estimare:** 2–3. **Dependențe:** #5, #6.

### Pe scurt
Serverul și endpoint-urile există deja. Completează verificările și exemplele pentru funcționalitățile finale S1; vechiul task CLI este înlocuit de această integrare REST.

### Exemplu
Aceleași fișiere JSON sunt trimise către patterns:8080 și baseline:8081. La 200 comparăm starea; la 400/404/422 verificăm codul, indexul și lipsa efectelor.

### Pași concreți
1. Folosește exclusiv endpoint-urile fixe: GET /api/v1/health, GET /api/v1/home, POST /api/v1/commands, POST /api/v1/scenes/execute.
2. În server/pom.xml adaugă ca test resources și ../acceptance-tests/src/test/resources, păstrând server/src/test/resources, pentru reutilizarea fixtures din classpath.
3. Completează aceeași suită HTTP pentru ambele proprietăți smart-home.variant, folosind fișierele existente și rezultatele din README-ul fixtures.
4. Verifică succesul temperaturii/scenariilor, erorile de shape, ID/tip, interval și commandIndex; nu mai trebuie să existe așteptări 501 pentru features obligatorii S1.
5. Actualizează docs/http/smart-home.http, README și descrierea OpenAPI astfel încât să reflecte funcționalitățile chiar implementate.
6. Păstrează formatul comun al răspunsurilor și startup-ul pe porturi diferite; nu adăuga un endpoint de schimbare a variantei.

### Criterii de acceptanță
- [ ] Ambele servere respectă v1 pentru toate features S1.
- [ ] Fixtures se reutilizează, fără două seturi de inputuri divergente.
- [ ] Exemplele copy-paste și testele HTTP sunt actualizate.
- [ ] Maven verify și smoke test-ul JAR-ului sunt verzi.

### Fișiere și livrare
server/src/test/java/.../HttpContractTest.java; server/pom.xml; docs/http/smart-home.http; README.md; OpenAPI.

### Nu intră în task
Nu crea CLI nou, React, Swagger UI, autentificare sau DB. Controllerul nu primește logica engines.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 07 — Revizuirea suitei comune și protejarea separării engines

**Pistă:** D — calitate. **Estimare:** 2–3. **Dependențe:** #5, #6.

### Pe scurt
Completează cazurile lipsă și verifică structura, fără să dublezi testele scrise de autorii features. Scopul este aceeași corectitudine în ambele engines.

### Exemplu
Un singur caz core verifică device necunoscut, rulat cu BaselineHome::new și PatternsHome::new. Un guard verifică faptul că baseline nu importă patterns.

### Pași concreți
1. Revizuiește ContractV1Test, HomeAcceptanceTest și suita HTTP existentă; nu copia aceleași teste în clase noi.
2. Acoperă matricea fixă: control light; temperaturi 5/35; interval/precizie/NaN/Infinity; unknown ID; incompatibilitate; scene valide/ordine/eroare la mijloc; stări independente.
3. Verifică imutabilitatea snapshots/listelor și independența caselor; pentru input Java invalid constructorii resping prin IllegalArgumentException.
4. Adaugă un guard JUnit pentru dependențe/importuri directe: baseline↔patterns interzis; Spring/Jackson interzis în production code din shared/engines.
5. Folosește citire de fișiere Java/POM pentru guard, fără bibliotecă nouă. Documentează că verifică dependențe directe, nu orice posibilă încălcare arhitecturală.
6. Rulează verify și documentează matricea acoperită în docs/testing.md.

### Criterii de acceptanță
- [ ] Aceeași suită core și HTTP verifică ambele variante.
- [ ] Cazurile-limită și lipsa efectelor sunt acoperite.
- [ ] Un import/dependency direct interzis este detectat automat.
- [ ] Fără slăbirea testelor; toate verificările sunt în CI.

### Fișiere și livrare
acceptance-tests/src/test/java; server/src/test/java; docs/testing.md; POM-uri doar dacă sunt necesare pentru verificare.

### Nu intră în task
Fără refactorizarea engines sau analizor AST nou. Nu declara every switch un anti-pattern.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 08 — Runner Java pentru LOC și benchmark direct pe engines

**Pistă:** D — evaluare. **Estimare:** 2–3. **Dependențe:** #5.

### Pe scurt
Implementează măsurarea după o metodă deja fixată. Comparația principală ocolește Spring/HTTP, ca să nu măsurăm mai ales infrastructura comună.

### Exemplu
Fiecare engine execută 10.000 comenzi de încălzire JIT, apoi 5 rulări măsurate cu 50.000 setări alternative on/off. Validăm rezultatul final și păstrăm toți timpii.

### Pași concreți
1. Creează modulul Maven benchmarks, adaugă-l în reactor și depinde de contracts/baseline/patterns, fără server/Spring. Main class: ro.upb.sasps.smarthome.benchmarks.MetricsMain.
2. Împachetează JAR-ul benchmarks/target/smart-home-benchmarks.jar. Moduri fixe: loc și runtime. Pentru JSON de output poate folosi jackson-databind, versiune gestionată de BOM, fără dependență Spring.
3. loc numără linii fizice NEGOALE din fișiere .java, inclusiv comentarii; raportează separat fiecare engine/src/main/java, codul comun, server/app și testele. Exclude target, output și workspaces. Nu numi această valoare LOC logic.
4. runtime face 10.000 setări on/off pe o instanță de warm-up, apoi 5 runde × 50.000 pe instanțe noi. Timer: System.nanoTime; fără console output în secțiunea măsurată. Verifică în afara timerului că rezultatul este valid și becul final este oprit.
5. Alternează ordinea engines între runde și salvează ordinea, timpii brut, mediana, min/max, JDK, OS/arch, CPU count, parametrii și commit SHA. Fără măsurare de memorie în S1.
6. Output JSON în benchmarks/output (ignorat), plus un exemplu mic real și explicații în benchmarks/README.md. Nu fabrica rezultate și nu folosi timpii Spring ca timpi ai engine-ului.

### Criterii de acceptanță
- [ ] Comenzile loc și runtime sunt documentate și executabile.
- [ ] Ambele engines primesc același workload și pornesc din starea fixă.
- [ ] Rezultatele brute și proveniența sunt păstrate; mediana este calculată corect.
- [ ] Verificări meaningful pentru contor și calcule; verify și o rulare reală trec.

### Fișiere și livrare
benchmarks/pom.xml; benchmarks/src/main/java/.../MetricsMain.java; teste; pom.xml din rădăcină; benchmarks/README.md.

### Nu intră în task
Fără JMH, AI runner, dashboard sau benchmark cloud în S1. Rezultatul este descriptiv; nu dovedește superioritatea generală a patterns.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 09 — Onboarding independent pentru serverul Spring

**Pistă:** Comun — review rotativ. **Estimare:** 1–2. **Dependențe:** #2.

### Pe scurt
Un coleg diferit de autor verifică dacă poate porni serverul și lucra folosind doar repo-ul. Pașii sunt deja stabiliți.

### Exemplu
Dintr-o clonă nouă: build, pornire patterns:8080, pornire baseline:8081, health/status și o comandă de bec pe fiecare.

### Pași concreți
1. După merge-ul PR-ului Spring, clonează main într-un director nou și folosește JDK 21.
2. Urmează README: wrapper clean verify, apoi java -jar server/target/smart-home-server.jar.
3. Pornește al doilea proces cu --smart-home.variant=baseline --server.port=8081; rulează exemplele HTTP pentru bec și erori.
4. Verifică documentele contract/taskuri/instrucțiuni AI și faptul că stadiul implementării este descris corect.
5. Notează OS, JDK, comenzile și rezultatele în issue. Repară doar eventualele lipsuri din instrucțiuni prin PR.

### Criterii de acceptanță
- [ ] Clonarea, build-ul și serverele funcționează fără contextul chatului.
- [ ] Health, status și light command funcționează pe ambele variante.
- [ ] Instrucțiunile sunt suficiente și reflectă stadiul real.
- [ ] Dovezi independente atașate; nu este necesar un PR artificial dacă nu există modificări.

### Fișiere și livrare
README/CONTRIBUTING/docs doar dacă se descoperă o lipsă; dovezi în issue.

### Nu intră în task
Nu reinstala infrastructură globală, nu configura cloud/DB și nu extinde aplicația.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

---

## [S1] 10 — Demo REST și nota de progres pentru 16 noiembrie

**Pistă:** Comun — toți patru. **Estimare:** 2–3. **Dependențe:** #7, #8, #9, #10.

### Pe scurt
Pregătește prezentarea intermediară cu pași exacți și rezultate reale. Scope-ul demonstrației este deja ales.

### Exemplu
Pe ambele servere arătăm inventarul, controlul becului/termostatului, leave-home și un scenariu invalid fără efecte; apoi sumarul metricilor.

### Pași concreți
1. Pleacă dintr-un commit stabil cu #3–#10 integrate și notează SHA-ul.
2. În docs/demo-sprint-1.md scrie comenzile și rezultatele: startup ambele variante, inventar, light-on, temperature-22, leave-home, unknown-device și invalid-middle.
3. Pregătește docs/progress-sprint-1.md, aproximativ două pagini: realizat, diagramă, Command/Adapter/Composite justificate, probleme observate și rezultate măsurate.
4. Include proveniența metricilor #9 și limitele metodei; fără promisiuni că patterns sunt mai rapide.
5. Repetă demo-ul și păstrează output capturat real ca fallback; actualizați trackerul #1 și planul S2.

### Criterii de acceptanță
- [ ] Demo-ul poate fi repetat de alt coleg din instrucțiuni.
- [ ] Ambele variante și erorile fără efecte sunt demonstrate.
- [ ] Nota include date reale și separă constatări de ipoteze.
- [ ] Toți patru își pot explica contribuția; trackerul este actualizat.

### Fișiere și livrare
docs/demo-sprint-1.md; docs/progress-sprint-1.md; rezultate mici de referință; tracker #1.

### Nu intră în task
Nu construi un deck mare, UI sau raportul final în acest task.

Verificare comună: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`). Sunt necesare minimum două aprobări de la colegi diferiți de autor și CI verde. Termenii și formele JSON sunt în contractul v1; nu le schimba pentru a simplifica implementarea.

