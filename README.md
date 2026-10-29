# Smart Home — SASPS

Simulator de casă inteligentă în **Java 17+ și Spring Boot**, cu două engines echivalente: **baseline** (control direct) și **patterns** (Command, Adapter și extensiile planificate). Spring este o interfață comună peste engines, nu parte din logica de design comparată.

**Contractele sunt deja decise.** Începe cu [contractul v1](docs/contracts-v1.md) și [taskurile explicate pas cu pas](docs/tasks-sprint-1.md). Primul task nu mai cere alegerea interfețelor.

## Pornire

Instalează JDK 17 sau mai nou. Maven Wrapper descarcă Maven 3.9.11 și dependențele la prima rulare; Maven nu trebuie instalat separat. Importă pom.xml din rădăcină în IDE.

macOS/Linux:

```sh
git clone https://github.com/bsefcic/sasps-smart-home.git
cd sasps-smart-home
./mvnw clean verify
java -jar server/target/smart-home-server.jar
```

Windows PowerShell:

```powershell
git clone https://github.com/bsefcic/sasps-smart-home.git
cd sasps-smart-home
.\mvnw.cmd clean verify
java -jar server/target/smart-home-server.jar
```

Înainte de integrarea PR-ului Spring în main, pornește din branch-ul său `feat/spring-web-contract-v1`. Configurația implicită folosește patterns pe http://127.0.0.1:8080. Verifică în browser `/api/v1/health`, `/api/v1/home` sau `/openapi.yaml`.

Pentru baseline, într-un al doilea terminal:

```sh
java -jar server/target/smart-home-server.jar --smart-home.variant=baseline --server.port=8081
```

Fiecare proces păstrează o casă în memorie între requests; restartul resetează starea. Variantele folosesc aceleași endpoint-uri și formate. Configurația cu o variantă necunoscută este respinsă la pornire.

## Exemplu HTTP

```sh
curl -X POST http://127.0.0.1:8080/api/v1/commands -H "Content-Type: application/json" -d '{"type":"set-light-power","deviceId":"living-light","power":true}'
```

Pentru PowerShell poți folosi:

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/api/v1/commands -ContentType application/json -Body '{"type":"set-light-power","deviceId":"living-light","power":true}'
```

Mai multe exemple: [docs/http/smart-home.http](docs/http/smart-home.http), utilizabil în IntelliJ HTTP Client sau VS Code REST Client.

## Ce funcționează acum

- Server Spring Boot 3.5.16, validare strictă a JSON-ului, erori uniforme, selectarea engine-ului și CORS configurat pentru un React local viitor.
- Health, citirea stării și control on/off pentru becul living-light în ambele engines.
- Command și Adapter în patterns; API direct în baseline.
- Contracte Java și exemple complete pentru termostat și scenarii, cu teste de validare.
- Teste HTTP reale comune pentru ambele engines, inclusiv păstrarea stării și concurență; CI pe JDK 17 și 21.

**Funcționalități încă de implementat:** simulatorul/adaptorul de termostat, controlul lui în engines și scenariile plate sunt stories #3–#6. Cererile valide de temperatură și scenarii returnează momentan **501 NOT_IMPLEMENTED**, fără efecte. Nu sunt tratate ca implementate doar pentru că există schema sau endpoint-ul.

React nu este implementat și nu este necesar pentru S1. Îl putem conecta ulterior la API; origin-urile implicite permise sunt http://localhost:5173 și http://localhost:3000. Nu sunt necesare hardware, baze de date, Docker sau chei API.

## Module

| Modul | Rol |
| --- | --- |
| shared/contracts | Requests, results și snapshots imuabile, fără Spring/Jackson |
| shared/simulators | API-uri fictive de producători |
| implementations/baseline | Engine cu control direct |
| implementations/patterns | Engine cu patterns justificate |
| server | Spring Boot, HTTP, DI și sincronizarea sesiunii |
| app | Demo CLI opțional din bootstrap-ul Java |
| acceptance-tests | Suită de acceptanță comună și fixtures JSON |
| benchmarks / experiments | Metodologie; scripturile vin în stories dedicate |

Structură Java standard: src/main/java, src/test/java și src/test/resources. Nu partajăm logica de execuție/scenarii între engines.

## Verificări

- `./mvnw clean verify`: compilare strictă, teste, packaging și formatare Java.
- `./mvnw spotless:apply`: formatare automată Java.
- `./mvnw test`: compilare și teste, fără toate verificările verify.

Pe Windows înlocuiește ./mvnw cu `.\mvnw.cmd`. CI verifică și pornirea JAR-ului Spring. Demo-ul vechi rămâne disponibil după build prin `java -jar app/target/smart-home.jar baseline` sau `patterns`; nu este interfața web principală.

## Cum preiei un task

1. Citește [ghidul complet S1](docs/tasks-sprint-1.md) și issue-ul, inclusiv dependențele.
2. Urmează [CONTRIBUTING.md](CONTRIBUTING.md) și instrucțiunile [AGENTS.md](AGENTS.md), inclusiv cele locale.
3. Implementează specificația fixată; nu mai există taskuri care cer să decideți contractele.
4. Adaugă teste, rulează verificările și deschide un PR pentru review de la alt coleg.

## Documentație

- [Contract v1: API, erori, inventar, scenarii, Vendor A](docs/contracts-v1.md)
- [Taskuri S1 cu pași și rezultate așteptate](docs/tasks-sprint-1.md)
- [Scope](docs/scope.md) și [plan/calendar](docs/plan.md)
- [Arhitectură](docs/architecture.md), [lucru cu AI](docs/ai-workflow.md), [evaluare](docs/evaluation.md)

Comparăm metrici structurale, schimbări, corectitudine și execuție. Măsurăm engines direct; latența HTTP/Spring se raportează separat. Experimentul AI este planificat pentru S2–S3, cu workspace-uri izolate. Nu presupunem că patterns câștigă la toate metricile.
