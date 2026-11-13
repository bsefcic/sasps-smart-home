# Smart Home — SASPS

Simulator de casă inteligentă în **Java 21 și Spring Boot**, cu două engines echivalente: **baseline** (control direct) și **patterns** (Command, Adapter și extensiile planificate). Spring este o interfață comună peste engines, nu parte din logica de design comparată.

**Nou în echipă?** Începe cu [ghidul de arhitectură și diagramele](docs/architecture.md): explică scopul comparației, structura aplicației, fluxul unei comenzi și diferența dintre starter și ținta S1. Apoi citește [contractul v1](docs/contracts-v1.md) și [taskurile explicate pas cu pas](docs/tasks-sprint-1.md). Contractele sunt deja decise.

## Pornire

Instalează JDK 21 și configurează JAVA_HOME către el. Maven verifică folosirea JDK 21; compilarea țintește Java 21. Maven Wrapper descarcă Maven 3.9.11 și dependențele la prima rulare; Maven nu trebuie instalat separat. Importă pom.xml din rădăcină în IDE și setează Project SDK și language level la 21.

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

Configurația implicită folosește patterns pe http://127.0.0.1:8080. Verifică în browser `/api/v1/health`, `/api/v1/home` sau `/openapi.yaml`.

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
- Health, citirea stării, control on/off pentru living-light și temperatură pentru living-thermostat în ambele engines.
- Command și Adapter în patterns; API direct în baseline.
- Contracte Java și exemple complete pentru termostat și scenarii, cu teste de validare.
- Simulator VendorAThermostat în Java simplu și adaptorul patterns către interfața Thermostat în Celsius; conectate la engines prin control direct în baseline și Command/Adapter în patterns. Simulatorul pornește la 200 deci-grade Celsius, acceptă 50–350 și păstrează stare independentă per instanță.
- Teste HTTP reale comune pentru ambele engines, inclusiv păstrarea stării și concurență; CI pe JDK 21.

**S1-04 și S1-05 implementate:** control manual și scenarii plate de 1–50 comenzi în ambele engines. Scenariile se validează integral înainte de execuție; ID-urile necunoscute sau incompatibile nu lasă efecte parțiale, iar eroarea include commandIndex. Ordinea este respectată; ultima setare a unui dispozitiv rămâne în stare.

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

Matricea comună pentru ambele engines și limitele guard-ului arhitectural sunt descrise în [docs/testing.md](docs/testing.md).

- `./mvnw clean verify`: compilare strictă, teste, packaging și formatare Java.
- `./mvnw spotless:apply`: formatare automată Java.
- `./mvnw test`: compilare și teste, fără toate verificările verify.

Pe Windows înlocuiește ./mvnw cu `.\mvnw.cmd`. CI verifică și pornirea JAR-ului Spring. Demo-ul vechi rămâne disponibil după build prin `java -jar app/target/smart-home.jar baseline` sau `patterns`; nu este interfața web principală.

## Cum preiei un task

1. Citește [arhitectura](docs/architecture.md), apoi [ghidul complet S1](docs/tasks-sprint-1.md) și issue-ul, inclusiv dependențele.
2. Urmează [CONTRIBUTING.md](CONTRIBUTING.md) și instrucțiunile [AGENTS.md](AGENTS.md), inclusiv cele locale.
3. Implementează specificația fixată; nu mai există taskuri care cer să decideți contractele.
4. Adaugă teste, rulează verificările și deschide un PR pentru minimum două aprobări de la colegi diferiți de autor.

## Documentație

- [Contract v1: API, erori, inventar, scenarii, Vendor A](docs/contracts-v1.md)
- [Taskuri S1 cu pași și rezultate așteptate](docs/tasks-sprint-1.md)
- [Scope](docs/scope.md) și [plan/calendar](docs/plan.md)
- [Arhitectură](docs/architecture.md), [lucru cu AI](docs/ai-workflow.md), [evaluare](docs/evaluation.md)

Comparăm metrici structurale, schimbări, corectitudine și execuție. Măsurăm engines direct; latența HTTP/Spring se raportează separat. Experimentul AI este planificat pentru S2–S3, cu workspace-uri izolate. Nu presupunem că patterns câștigă la toate metricile.
