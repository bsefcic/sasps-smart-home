# Plan de livrare

## Calendar

| Sprint | Interval                                                                | Review                      |
| ------ | ----------------------------------------------------------------------- | --------------------------- |
| S1     | 26 octombrie–15 noiembrie 2026                                          | 16 noiembrie, săptămâna 8   |
| S2     | 16 noiembrie–13 decembrie 2026                                          | 14 decembrie, săptămâna 12  |
| S3     | 14 decembrie 2026–10 ianuarie 2027, incluzând două săptămâni de vacanță | 11 ianuarie 2027, estimativ |

Review-ul final trebuie confirmat cu calendarul oficial. Nu planificăm livrabile esențiale bazându-ne pe muncă în vacanță. Absențele și atribuirea nominală se stabilesc ulterior.

## Obiective

- **S1:** control manual, bec/termostat, scenarii plate, CLI și teste echivalente; un demo și măsurători inițiale.
- **S2:** scope obligatoriu complet, cele cinci patterns justificate, măsurători reproductibile și pilot AI.
- **S3:** versiunile înghețate, experimente finale, raport și demonstrație pregătite; nice-to-haves doar după acestea.

## Organizare S1

Bootstrap-ul este un PR pregătitor în primul sprint. Membrii pornesc din `main` după integrarea acestuia. Taskurile sunt neatribuite; A–D sunt piste orientative, nu persoane sau invitații.

| Pistă | Componentă               | Principiu                                                |
| ----- | ------------------------ | -------------------------------------------------------- |
| A     | Simulatoare și integrări | Neutralitate a simulatorului; adaptoare doar în patterns |
| B     | Control manual           | Același comportament implementat în ambele variante      |
| C     | Scenarii și CLI          | Funcționalitate demonstrabilă end-to-end                 |
| D     | Evaluare și integrare    | Teste comune, măsurători și automatizarea verificărilor  |

Contractul v1 se stabilește la început. După aceea lucrul pe piste se poate suprapune; fiecare issue indică dependențele. Un review săptămânal scurt urmărește blockers și demo-ul, nu numărul de commit-uri. Se rotesc reviewerii; toate persoanele contribuie la documentația componentei lor și la prezentare.

## Backlog S1

Tracker: [Sprint 1 — #1](https://github.com/bsefcic/sasps-smart-home/issues/1). Cele zece stories sunt neatribuite. Estimările însumează aproximativ **20–30 ore** pentru echipă, în afara pregătirii bootstrap-ului; includ verificări și nu reprezintă o obligație individuală. Scope-ul se reduce dacă disponibilitatea reală nu îl susține.

| Issue                                                        | Story                                                               | Pistă orientativă      | Estimare | Dependențe      |
| ------------------------------------------------------------ | ------------------------------------------------------------------- | ---------------------- | -------- | --------------- |
| [#2](https://github.com/bsefcic/sasps-smart-home/issues/2)   | Stabilirea contractului v1 și a fixture-urilor de referință         | Comun / kickoff        | 1–2 h    | Bootstrap       |
| [#3](https://github.com/bsefcic/sasps-smart-home/issues/3)   | Simulatoare Vendor A pentru bec și termostat                        | A — dispozitive        | 2–3 h    | #2              |
| [#4](https://github.com/bsefcic/sasps-smart-home/issues/4)   | Adaptoare pentru bec și termostat în varianta patterns              | A — integrări          | 2–3 h    | #2, #3          |
| [#5](https://github.com/bsefcic/sasps-smart-home/issues/5)   | Control manual pentru bec și termostat în ambele variante           | B — comenzi            | 3–4 h    | #2, #3, #4      |
| [#6](https://github.com/bsefcic/sasps-smart-home/issues/6)   | Scenarii plate cu prevalidare și execuție ordonată                  | C — scenarii           | 3–4 h    | #2, #5          |
| [#7](https://github.com/bsefcic/sasps-smart-home/issues/7)   | CLI pentru selectarea variantei și scenarii JSON                    | C — demo               | 2–3 h    | #2, #5, #6      |
| [#8](https://github.com/bsefcic/sasps-smart-home/issues/8)   | Completarea suitei comune de acceptanță și verificarea granițelor   | D — calitate           | 2–3 h    | #2, #5, #6      |
| [#9](https://github.com/bsefcic/sasps-smart-home/issues/9)   | Primele metrici: LOC separat și smoke benchmark reproductibil       | D — evaluare           | 2–3 h    | #5              |
| [#10](https://github.com/bsefcic/sasps-smart-home/issues/10) | Onboarding de pe o clonă nouă și verificarea CI                     | Comun — review rotativ | 1–2 h    | Bootstrap       |
| [#11](https://github.com/bsefcic/sasps-smart-home/issues/11) | Demo integrat și nota de progres pentru prezentarea din săptămâna 8 | Comun — toți patru     | 2–3 h    | #7, #8, #9, #10 |

Ordine recomandată: onboarding-ul #10 poate începe imediat după bootstrap; #2 deblochează dezvoltarea. #3 → #4 → #5 formează primul flux. #6 și #9 se pot suprapune după #5; CLI-ul #7 și completarea suitei #8 urmează după #6. Demo-ul #11 închide sprintul. Înainte ca dependențele să fie integrate, se pot pregăti specificații/test cases, fără implementări paralele incompatibile ale aceluiași contract.

GitHub Issues sunt sursa statusului. Acest document nu pretinde existența unui GitHub Project sau milestone configurat. Până la configurarea lor, prefixul `[S1]`, indexul și dependențele din issues țin backlog-ul navigabil.
