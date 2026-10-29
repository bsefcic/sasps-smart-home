# Contract v1 — decis pentru Sprint 1

Acesta este contractul comun baseline/patterns. Deciziile de mai jos sunt stabilite; taskurile rămase le implementează. Nu schimbați numele câmpurilor sau comportamentul fără o modificare explicită a contractului.

## Cum pornim serverele

Java 17+, Maven Wrapper, Spring Boot 3.5.16. Build din rădăcină: `./mvnw clean verify` (Windows: `.\mvnw.cmd clean verify`).

```sh
java -jar server/target/smart-home-server.jar
java -jar server/target/smart-home-server.jar --smart-home.variant=baseline --server.port=8081
```

Primul server folosește patterns pe 127.0.0.1:8080; al doilea baseline pe 127.0.0.1:8081. Nu schimbăm varianta dintr-un request. Altă valoare de configurare decât baseline/patterns împiedică pornirea. Fiecare proces are o singură casă independentă, în memorie, păstrată între requests și resetată la restart.

## Endpoints și răspunsuri

| Metodă / cale | Input | Succes |
| --- | --- | --- |
| GET /api/v1/health | fără body | 200, `{"status":"UP"}` |
| GET /api/v1/home | fără body | 200, HomeSnapshot |
| POST /api/v1/commands | Command | 200, ExecutionResponse |
| POST /api/v1/scenes/execute | SceneRequest | 200, ExecutionResponse |

POST folosește Content-Type: application/json. OpenAPI este versionat în `server/src/main/resources/static/openapi.yaml` și servit la `/openapi.yaml`. Fișierul include și comportamentul-țintă al stories încă neimplementate.

### Comenzi — exact aceste câmpuri

```json
{"type":"set-light-power","deviceId":"living-light","power":true}
```

```json
{"type":"set-target-temperature","deviceId":"living-thermostat","temperature":22.1}
```

- type este unul dintre cele două string-uri de mai sus, case-sensitive.
- deviceId respectă `[a-z][a-z0-9-]{0,63}`. Nu normalizăm și nu facem trim implicit.
- power este JSON boolean, nu string/număr.
- temperature este JSON number finit, între **5.0 și 35.0 inclusiv**, cu pas de **0.1°C**. 22 și 22.0 sunt valide; 22.25 este invalid. Nu rotunjim inputul.
- Toate câmpurile sunt obligatorii și nenule; câmpurile suplimentare sunt respinse, inclusiv temperature pe o comandă de bec. Tipurile nu sunt convertite implicit din string.
- JSON invalid, proprietăți duplicate și tokeni suplimentari după obiect sunt respinși.
- Cererile Java sunt records SetLightPower și SetTargetTemperature; constructorii verifică ID/range/precizie și aruncă IllegalArgumentException pentru valori invalide. Inventarul se verifică în fiecare engine, nu în contracte.

### Scenariu plat

```json
{"name":"leave-home","commands":[{"type":"set-light-power","deviceId":"living-light","power":false},{"type":"set-target-temperature","deviceId":"living-thermostat","temperature":18}]}
```

name este string nenul, neblank, de maximum 64 de unități UTF-16 (String.length), păstrat exact. commands conține **1–50** de comenzi nenule, fără scenarii în interior. Nu există registru, creare sau salvare de scenarii în S1: trimitem lista și o executăm.

Ordinea este cea din listă; două setări ale aceluiași dispozitiv sunt permise, ultima rămâne în stare. Verificăm întâi forma/valorile tuturor comenzilor la granița HTTP, apoi existența și compatibilitatea dispozitivelor în engine pentru TOATĂ lista, înainte de execuție. Returnăm prima eroare din fiecare etapă, de la stânga la dreapta; o eroare de formă poate preceda o eroare de inventar dintr-o comandă anterioară.

O listă invalidă nu schimbă nimic. S1 nu simulează defecte ale hardware-ului: nu promitem rollback după un efect fizic sau compensări. SceneRequest copiază defensiv lista.

### ExecutionResponse

```json
{"ok":true,"executedCommands":1,"snapshot":{"devices":[{"id":"living-light","kind":"light","power":true}]}}
```

Aceasta este o comandă pe inventarul starterului. executedCommands este 1 la comandă sau lungimea listei la scenariu. snapshot este starea de după execuție, capturată în aceeași secțiune sincronizată. Câmpurile suplimentare de variantă/timestamp nu se adaugă: ambele servere oferă același format.

### Inventarul-țintă S1 și HomeSnapshot

După #3–#5, ambele engines pornesc cu exact două dispozitive, ordonate crescător după ID:

```json
{"devices":[{"id":"living-light","kind":"light","power":false},{"id":"living-thermostat","kind":"thermostat","targetTemperature":20.0}]}
```

Un bec nu are targetTemperature; termostatul nu are power. Snapshot-urile sunt records imuabile cu o listă copiată defensiv. Setările identice repetate sunt permise, fără efecte suplimentare. Nu avem persistență, înregistrare dinamică sau multi-user.

**Stadiul bootstrap-ului:** există doar living-light. Controlul valid al temperaturii și scenariile valide returnează 501 NOT_IMPLEMENTED, fără efecte. Tipurile, validarea și contractele există deja. #3–#6 completează implementarea și înlocuiesc explicit aceste răspunsuri temporare; nu considerăm funcționalitatea completă înaintea acelor stories.

## Erori

```json
{"ok":false,"error":{"code":"UNKNOWN_DEVICE","message":"Unknown device: missing"}}
```

La o comandă din scenariu adăugăm `commandIndex` zero-based în error (a doua comandă = 1). La o eroare a întregului body sau la o comandă individuală nu trimitem commandIndex. Erorile nu includ snapshot, executedCommands sau stack trace.

| HTTP | Code | Când |
| --- | --- | --- |
| 400 | INVALID_REQUEST | JSON/shape/tipuri/ID invalid, câmp lipsă/null/suplimentar, duplicate/trailing tokens |
| 400 | INVALID_COMMAND | type string necunoscut |
| 400 | INVALID_SCENE | nume blank/prea lung, commands nu este array sau are 0/>50 elemente |
| 404 | UNKNOWN_DEVICE | ID valid sintactic, inexistent în inventar |
| 404 | NOT_FOUND | endpoint inexistent |
| 422 | INVALID_TEMPERATURE | număr în afara intervalului, nefinit după conversie sau precizie peste o zecimală |
| 422 | INCOMPATIBLE_DEVICE | set-target-temperature către bec sau set-light-power către termostat |
| 405 | INVALID_REQUEST | metodă HTTP nepermisă |
| 415 | INVALID_REQUEST | Content-Type neacceptat |
| 501 | NOT_IMPLEMENTED | doar delimitarea temporară, documentată, a starterului |
| 500 | INTERNAL_ERROR | eroare neașteptată; mesaj public generic |

Ordinea la comandă: JSON → obiect/type → câmpuri obligatorii/permise → tipuri/ID → temperatură → inventar/compatibilitate → execuție. Codurile și statusurile sunt contractuale; mesajele de validare pot explica eroarea fără să fie parsate de client. Mesajele de inventar sunt generate prin fabricile din CommandResult.

## API Vendor A — decis, nu de proiectat în stories

- VendorALight există deja: constructor String id, Power.ENABLED/DISABLED, setPower(Power), readPower(), id(). Starea inițială DISABLED.
- VendorAThermostat(String id): target inițial **200 deci-grade Celsius**; metode `setTargetDeciCelsius(int)`, `readTargetDeciCelsius()` și `id()`. Acceptă 50–350 inclusiv; în afara intervalului aruncă IllegalArgumentException. Fiecare obiect are stare proprie; fără I/O sau întârzieri.
- În patterns, interfața Thermostat are `id()`, `setTargetTemperature(double)` și `targetTemperature()`. VendorAThermostatAdapter convertește Celsius ↔ deci-grade, fără rotunjirea inputului invalid. Comanda este SetTargetTemperatureCommand.
- În baseline apelăm direct API-ul Vendor A. Nu partajăm engine-ul sau prevalidarea inventarului între variante.

## Concurență și UI viitor

HomeSession serializează accesul la engine pentru comenzi, scenarii și citiri; snapshot-ul răspunsului se capturează sub același lock. Politica este identică pentru ambele variante. Ordinea cererilor concurente nu este garantată; nu există tranzacții între requests sau retry automat. Benchmark-ul principal compară engines direct, separat de HTTP/Spring.

CORS permite implicit origin-urile http://localhost:5173 și http://localhost:3000, metode GET/POST/OPTIONS, header Content-Type, fără credentials. Se configurează prin smart-home.cors.allowed-origins. React nu este implementat și nu este necesar pentru S1. Serverul ascultă implicit numai pe 127.0.0.1; nu include autentificare.
