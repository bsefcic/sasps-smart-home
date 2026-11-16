# Plan de livrare

## Calendar

| Sprint | Interval | Review |
| --- | --- | --- |
| S1 | 26 octombrie–15 noiembrie 2026 | 16 noiembrie, săptămâna 8 |
| S2 | 16 noiembrie–13 decembrie 2026 | 14 decembrie, săptămâna 12 |
| S3 | 14 decembrie 2026–10 ianuarie 2027, incluzând două săptămâni de vacanță | 11 ianuarie 2027, estimativ |

Data finală se confirmă cu calendarul oficial. Disponibilitatea și absențele se stabilesc ulterior; nu bazăm livrabile obligatorii pe munca în vacanță.

## Decizii deja luate

Java 21, Spring Boot 3.5.16 în server, Maven Wrapper, două engines independente și o interfață REST comună. [Contractul v1](contracts-v1.md) fixează endpoints, forme JSON, erori, inventar, scenarii și API Vendor A pentru întreg Sprintul 1. [Ghidul taskurilor](tasks-sprint-1.md) conține pași și rezultate, fără taskuri de «alegere a contractelor».

React rămâne opțional. Pe commitul verificat pentru review, serverul oferă controlul becului și termostatului și execută scenarii plate în ambele variante. [Demo-ul REST](demo-sprint-1.md) și [nota de progres](progress-sprint-1.md) documentează ce a fost verificat și măsurat.

## Obiective

- S1: control bec/termostat, scenarii plate, API REST complet, teste comune, metrici inițiale și demo.
- S2: al doilea producător, scenarii imbricate, senzori și automatizări, două politici de temperatură, măsurători reproductibile și pilot AI. Extensiile de contract pentru S2 se fixează la planificarea acelui sprint.
- S3: versiuni înghețate, experimente finale, raport și prezentare; nice-to-haves numai după acestea.

## Backlog S1

Statusurile curente sunt în [ClickUp — Smart Home SASPS](https://app.clickup.com/1500510000000520/v/l/1ampjurgbg8-332). Trackerul GitHub #1 a fost șters; tabelul păstrează numerotarea istoricului de planificare. În ClickUp, [S1-09](https://app.clickup.com/t/1500510000000520/1ampjurh9cj) este demo-ul, iar [S1-10](https://app.clickup.com/t/1500510000000520/1ampjurnxmg) este verificarea echivalenței și separării.

| Issue | Task | Pistă | Ore orientative | Dependențe |
| --- | --- | --- | --- | --- |
| [#2](https://github.com/bsefcic/sasps-smart-home/issues/2) | Contract v1 decis și implementat în bootstrap-ul Spring | Livrat prin bootstrap | 0 h rămase după merge | PR Spring |
| [#3](https://github.com/bsefcic/sasps-smart-home/issues/3) | Implementarea simulatorului VendorAThermostat după contract | A — dispozitive | 2–3 | #2 |
| [#4](https://github.com/bsefcic/sasps-smart-home/issues/4) | Thermostat și VendorAThermostatAdapter în patterns | A — integrări | 2–3 | #3 |
| [#5](https://github.com/bsefcic/sasps-smart-home/issues/5) | Control manual complet pentru bec și termostat în ambele engines | B — comenzi | 3–4 | #3, #4 |
| [#6](https://github.com/bsefcic/sasps-smart-home/issues/6) | Scenarii plate cu validare completă înainte de execuție | C — scenarii | 3–4 | #5 |
| [#7](https://github.com/bsefcic/sasps-smart-home/issues/7) | Exemple REST și teste HTTP finale pentru contractul v1 | C — integrare web | 2–3 | #5, #6 |
| [#8](https://github.com/bsefcic/sasps-smart-home/issues/8) | Revizuirea suitei comune și protejarea separării engines | D — calitate | 2–3 | #5, #6 |
| [#9](https://github.com/bsefcic/sasps-smart-home/issues/9) | Runner Java pentru LOC și benchmark direct pe engines | D — evaluare | 2–3 | #5 |
| [#10](https://github.com/bsefcic/sasps-smart-home/issues/10) | Onboarding independent pentru serverul Spring | Comun — review rotativ | 1–2 | #2 |
| [#11](https://github.com/bsefcic/sasps-smart-home/issues/11) | Demo REST și nota de progres pentru 16 noiembrie | Comun — toți patru | 2–3 | #7, #8, #9, #10 |

După bootstrap: #3 și #10 pot începe; se pot pregăti în paralel cazuri de test din specificația fixă. #4 urmează simulatorului, #5 integrează controlul, iar #6 implementează scenele. #9 poate fi lucrat după #5; #7/#8 după #6. #11 închide sprintul. Estimările includ verificări și se ajustează la capacitatea reală a echipei.

## Pornire S2

- Folosim commitul de referință din [nota de progres](progress-sprint-1.md) pentru comparația cu S1. Păstrăm rezultatele brute și rerulăm aceleași fluxuri pe noul commit, în același mediu, înainte de a adăuga măsurători pentru fluxuri noi.
- Fluxul Observer de mișcare este deja integrat prin #25. Următorul pas este alinierea contractului REST S2 cu fluxul existent înainte de extinderea inventarului, scenariilor și politicilor de temperatură.
- Păstrăm verificările comune și guard-ul S1-10 în CI. Pilotul AI rămâne separat, pe același task pentru ambele variante și cu rezultate identificate prin SHA.
