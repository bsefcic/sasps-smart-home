# Contract REST pentru Sprintul 2

Baseline și Patterns acceptă aceleași cereri și întorc aceleași răspunsuri. Păstrăm prefixul `/api/v1`, cererile din Sprintul 1 și semantica scenariilor plate din [PR #21](https://github.com/bsefcic/sasps-smart-home/pull/21).

| Funcție | Stadiu | Referință |
| --- | --- | --- |
| Termostat Vendor B | Implementat în PR #27 | [Contract v1](contracts-v1.md#inventarul-inițial-și-homesnapshot) |
| Citiri de mișcare | Implementat în PR #25 | [Contract v1](contracts-v1.md#citire-de-mișcare) |
| Scene imbricate | Contract pentru implementarea S2-03 | [Scene imbricate](#scene-imbricate) |
| Citiri de temperatură și moduri Confort/Eco | Contract pentru implementarea S2-05 | [Automatizarea temperaturii](#automatizarea-temperaturii) |

[`/openapi.yaml`](../server/src/main/resources/static/openapi.yaml) descrie API-ul disponibil. [OpenAPI pentru Sprintul 2](openapi-sprint-2.yaml) include și operațiile din S2-03 și S2-05. [Cererile pentru API-ul disponibil](http/smart-home.http) și [cererile pentru S2-03 și S2-05](http/smart-home-sprint-2.http) se folosesc cu aceleași date pentru ambele variante.

## Vendor B

Inventarul inițial include `bedroom-thermostat` la 20,0°C. [PR #27](https://github.com/bsefcic/sasps-smart-home/pull/27) folosește acest ID în câmpul existent `deviceId`. Comanda și starea publică rămân în Celsius.

```json
{"type":"set-target-temperature","deviceId":"bedroom-thermostat","temperature":22.1}
```

Trimisă la `POST /api/v1/commands`, comanda întoarce 200 cu `executedCommands: 1` și actualizează numai `targetTemperature` al termostatului ales. Aceeași comandă se poate folosi într-un scenariu. O valoare de 22,25°C întoarce 422 `INVALID_TEMPERATURE`, iar o comandă `set-light-power` adresată termostatului întoarce 422 `INCOMPATIBLE_DEVICE`.

## Scene imbricate

`POST /api/v1/scenes/execute` acceptă în continuare forma plată `{name, commands}`. Forma extinsă are exact câmpurile `name`, `commands` și `scenes`. Prezența câmpului `scenes` selectează forma extinsă. `name` rămâne eticheta de cel mult 64 de unități UTF-16. `scenes` este un obiect cu 0–50 de definiții, indexate după ID. Fiecare definiție are exact câmpul `commands`. ID-urile respectă sintaxa `[a-z][a-z0-9-]{0,63}`.

Fiecare listă `commands` conține 1–50 de elemente. Un element este o comandă obișnuită sau o referință cu exact două câmpuri, `{"type":"scene-ref","sceneId":"warm"}`. Referințele se rezolvă numai în `scenes` din aceeași cerere. Pot indica definiții declarate mai jos în JSON. Fiecare definiție trebuie să fie accesibilă de la rădăcină, iar `name` nu introduce o definiție implicită.

```json
{
  "name": "evening",
  "commands": [
    {"type":"scene-ref","sceneId":"warm"},
    {"type":"set-target-temperature","deviceId":"bedroom-thermostat","temperature":18},
    {"type":"scene-ref","sceneId":"warm"}
  ],
  "scenes": {
    "warm": {"commands":[{"type":"scene-ref","sceneId":"bedroom"}]},
    "bedroom": {"commands":[{"type":"set-target-temperature","deviceId":"bedroom-thermostat","temperature":22.1}]}
  }
}
```

Parcurgerea este în adâncime, de la stânga la dreapta. Fiecare referință execută din nou subscena la poziția ei. Exemplul aplică pe rând 22,1°C, 18°C și 22,1°C. Răspunsul este 200 cu `executedCommands: 3`, iar snapshot-ul final arată 22,1°C la `bedroom-thermostat`.

### Limite și validare

- Adâncimea maximă este 5, cu rădăcina la nivelul 1. O referință adaugă un nivel. O comandă obișnuită se execută la nivelul scenei sale.
- După expandare se acceptă maximum 50 de comenzi obișnuite. Fiecare apariție contează, inclusiv când aceeași subscenă este reutilizată. Referințele nu se adaugă la `executedCommands`.
- Mai întâi se validează JSON-ul și structura rădăcinii, a registrului și a listelor. Apoi se parcurg elementele în ordinea de execuție, verificând forma și valorile comenzilor, rezolvarea referințelor și limitele. La o referință se verifică existența, apoi ciclul, apoi adâncimea. Se verifică apoi că toate definițiile sunt accesibile. La final se validează inventarul și compatibilitatea dispozitivelor în ordinea expandată.
- Un ciclu apare când o referință duce la o scenă aflată deja pe traseul activ. Regula prinde atât autoreferințele, cât și ciclurile indirecte. Două referințe către aceeași scenă pe trasee distincte sunt valide.
- Prima eroare din fiecare etapă oprește validarea, fără a aplica vreo comandă. Prima eroare de structură fără localizare este suficientă dacă mai multe liste au structură invalidă.

| HTTP | Cod | Condiție |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | JSON invalid, câmpuri sau tipuri invalide, ID cu sintaxă invalidă |
| 400 | `INVALID_SCENE` | Nume blank sau prea lung, listă de comenzi invalidă, peste 50 de definiții ori definiție inaccesibilă |
| 400 | `INVALID_COMMAND` | Tip de comandă necunoscut |
| 400 | `UNKNOWN_SCENE` | ID valid sintactic, fără definiție în cerere |
| 400 | `SCENE_CYCLE` | Referință către o scenă activă |
| 400 | `SCENE_LIMIT_EXCEEDED` | Referință care ar intra la nivelul 6 sau a 51-a comandă expandată |
| 404 | `UNKNOWN_DEVICE` | Dispozitiv de comandă necunoscut |
| 422 | `INVALID_TEMPERATURE` | Temperatură de comandă în afara intervalului sau cu precizie invalidă |
| 422 | `INCOMPATIBLE_DEVICE` | Comandă incompatibilă cu dispozitivul ales |

Erorile la un element al formei extinse includ `error.commandPath`, o listă de indici de la zero. Primul index este din lista rădăcinii. Fiecare index intermediar indică o referință, iar ultimul indică elementul care a produs eroarea. La o eroare a întregii cereri, inclusiv a unei liste de definiție înainte de expandare, `commandPath` lipsește. Forma plată continuă să folosească `commandIndex`. Cele două câmpuri nu apar împreună.

Dacă ultima comandă din definiția `bedroom` a exemplului are `temperature: 22.25`, prima apariție produce următorul răspuns 422. Intervalul și precizia se validează înainte de verificarea inventarului.

```json
{"ok":false,"error":{"code":"INVALID_TEMPERATURE","message":"temperature must be 5–35 Celsius in steps of 0.1","commandPath":[0,0,0]}}
```

Codul, statusul și localizarea sunt contractuale. Mesajele sunt informative.

## Automatizarea temperaturii

| Metodă și cale | Cerere | Răspuns 200 |
| --- | --- | --- |
| `POST /api/v1/automation/temperature-mode` | `{"mode":"comfort"}` sau `{"mode":"eco"}` | `{"ok":true,"mode":"comfort"}` sau `{"ok":true,"mode":"eco"}` |
| `POST /api/v1/sensors/temperature` | `{"sensorId":"living-temperature","temperature":16.0}` | `{ok, executedCommands, snapshot}` |

Modul inițial este `comfort`. Selectarea unui mod se păstrează în sesiunea casei până la următoarea selecție sau până la restart. Selectarea nu schimbă temperatura țintă. La pornire, ambele termostate au ținta de 20,0°C, iar `living-temperature` nu are o citire anterioară. Senzorul este adresabil prin endpointul de citire și nu apare în `devices`.

Fiecare citire acceptată, inclusiv cea repetată, aplică politica modului activ pentru `living-thermostat`. Citirile sunt numere JSON finite, în Celsius, între 5,0 și 35,0 inclusiv, în pași de 0,1°C. Valorile nu se rotunjesc la intrare.

| Mod | Citire sub prag | Citire egală cu pragul sau mai mare |
| --- | --- | --- |
| Confort (`comfort`) | Sub 21,0°C, setează ținta la 22,0°C | De la 21,0°C, păstrează ținta curentă |
| Eco (`eco`) | Sub 17,0°C, setează ținta la 18,0°C | De la 17,0°C, păstrează ținta curentă |

Când politica setează ținta, folosește comanda existentă `set-target-temperature` și răspunsul are `executedCommands: 1`, inclusiv când ținta avea deja acea valoare. Când politica păstrează ținta, răspunsul are `executedCommands: 0`. `snapshot` este starea imediat după citire, capturată sub lock. Citirile nu schimbă `bedroom-thermostat` și nu declanșează regula de mișcare. Citirile de mișcare și controlul manual își păstrează comportamentul.

De exemplu, pentru 16,0°C în modul `eco` răspunsul este 200 cu următorul snapshot, dacă restul inventarului este în starea inițială.

```json
{
  "ok": true,
  "executedCommands": 1,
  "snapshot": {
    "devices": [
      {"id":"bedroom-thermostat","kind":"thermostat","targetTemperature":20.0},
      {"id":"living-light","kind":"light","power":false},
      {"id":"living-thermostat","kind":"thermostat","targetTemperature":18.0}
    ]
  }
}
```

Aceeași citire în `comfort` întoarce 200 și ținta de 22,0°C. O citire de 17,0°C în `eco` întoarce 200 cu `executedCommands: 0` și păstrează ținta de 18,0°C stabilită mai sus.

| HTTP | Cod | Condiție |
| --- | --- | --- |
| 400 | `INVALID_REQUEST` | Mod necunoscut, câmp lipsă, nul sau suplimentar, tip greșit, JSON sau ID invalid |
| 404 | `UNKNOWN_DEVICE` | ID de senzor valid sintactic, dar necunoscut |
| 422 | `INVALID_TEMPERATURE` | Număr în afara intervalului, nefinit după conversie sau cu precizie mai mare de o zecimală |

Se validează JSON-ul, câmpurile și tipurile, apoi valoarea temperaturii și existența senzorului. De exemplu, `{"sensorId":"living-temperature","temperature":"16.0"}` întoarce 400 `INVALID_REQUEST`. O cerere respinsă păstrează modul și starea casei. Toate cererile POST folosesc `Content-Type: application/json`, conform contractului v1.
