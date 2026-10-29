# Scope

Echipă de patru, Java 21, Spring Boot la granița HTTP. Aplicație web locală, cu un singur proces și o singură casă per server, fără hardware, DB, autentificare sau React în MVP.

## Contractele sunt decise

Sursa exactă este docs/contracts-v1.md și OpenAPI-ul versionat. Inventar-țintă S1: living-light oprit și living-thermostat cu targetTemperature 20.0°C. Temperaturile sunt 5–35°C inclusiv, pas 0.1. Scenariile au 1–50 comenzi și se verifică integral înainte de execuție. Ordinea dispozitivelor este crescătoare după ID.

## Implementat în starter

Server HTTP cu health, status și controlul becului în ambele engines; sesiune sincronizată, strict JSON, erori uniforme, contracte Java, fixtures, exemple HTTP și teste. Temperatura validă și scenariile valide sunt explicit 501 NOT_IMPLEMENTED până la stories #5/#6. Schema nu înseamnă că implementarea feature-ului este gata.

## Sprint 1 — review 16 noiembrie 2026

Simulatorul și adaptorul termostatului, control manual complet, scenarii plate, testele HTTP finale, suita comună, metrici inițiale și demo. Nu se mai proiectează contractele. Povestea CLI a fost înlocuită de exemple și teste REST; CLI-ul vechi rămâne opțional. Pașii fiecărui task sunt în docs/tasks-sprint-1.md.

## Sprint 2 — review 14 decembrie 2026

Al doilea producător, scenarii imbricate cu detectarea ciclurilor, senzori și automatizări, două politici de temperatură, metrici reproductibile și pilot AI. Nucleu: Command, Adapter, Composite, Observer și Strategy. Extensionarea contractului v1 pentru funcțiile S2 se documentează explicit înainte de dezvoltarea S2; contractul fix de acum este cel complet pentru S1.

## Sprint 3 și opționale

Înghețarea versiunilor, experimentele finale, raportul și demonstrația. Un al treilea producător/o a treia politică/un consumator nou de evenimente sunt candidați rezervați evaluării, nu se implementează anticipat. React, State, Decorator, undo, jaluzele și încuietori sunt nice-to-haves după livrabilele obligatorii.
