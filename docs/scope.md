# Scope și criterii funcționale

## Context

Echipă de patru persoane; Java; simulator local, controlabil prin CLI. Obiectivul este studiul comparativ, nu integrarea unei case reale. Patterns nu sunt presupuse superioare la toate metricile. Nu există un număr minim de patterns dedus din cerințele profesorului.

## Ce există în bootstrap

`new BaselineHome()` sau `new PatternsHome()` construiește o instanță independentă cu becul `living-light`, inițial oprit. `execute(new SetLightPower(deviceId, power))` setează starea, iar `getSnapshot()` întoarce o stare imuabilă. Un ID necunoscut produce `UNKNOWN_DEVICE` fără efecte. Setarea repetată la aceeași valoare este permisă. Contractul v0 este Java; starterul nu este încă un parser pentru input JSON arbitrar.

## Sprint 1: obligatoriu pentru prezentarea din 16 noiembrie 2026

- Două tipuri: bec (pornit/oprit) și termostat (temperatură țintă între 5 și 35°C inclusiv, valori finite).
- Un producător simulat și inventar determinist, fără înregistrare dinamică sau persistență.
- Control manual și citirea stării, în ambele implementări.
- Scenarii plate: secvențe ordonate de comenzi. Prevalidarea tuturor comenzilor înaintea execuției; o cerere invalidă nu modifică starea. Defectele hardware și rollback-ul nu intră în acest sprint.
- CLI cu alegerea variantei și citirea unor scenarii JSON; output și exit codes documentate.
- Input necunoscut, tipuri greșite și valori invalide respinse explicit; termeni și erori identici între variante.
- Teste comune pentru căile normale și erori; o primă măsurare descriptivă, fără concluzii premature.

Contractul v1 va detalia forma termostatului, scenariilor și erorilor înaintea implementării. Intervalul de temperatură este o convenție a proiectului, nu o cerință a profesorului.

## Sprint 2: ținta pentru 14 decembrie 2026

Al doilea producător, scenarii imbricate cu detectarea ciclurilor, senzori de temperatură/mișcare, automatizări și două politici de temperatură. Command, Adapter, Composite, Observer și Strategy sunt nucleul propus. CLI, metrici și un experiment AI pilot funcționează până la prezentare.

## Rezervat evaluării / opțional

Un al treilea producător, o a treia politică și un consumator nou de evenimente sunt candidați pentru experiment; nu le implementăm anticipat. State, Decorator, undo, dashboard, încuietori și jaluzele sunt opționale și nu blochează livrarea.

Nu includem hardware, cloud, autentificare, aplicație mobilă, microservicii sau baze de date în MVP. Modificarea scope-ului se notează într-o decizie și actualizează backlog-ul.
