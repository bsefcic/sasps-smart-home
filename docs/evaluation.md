# Evaluarea comparativă

## Întrebări

Ce cost structural și la execuție introduc abstracțiile? Cât de localizate și corecte sunt schimbările pentru aceeași cerință nouă? Cum diferă efortul aceluiași agent AI în condițiile testate? Nu presupunem un câștigător.

## Echivalență

Aceleași cerințe, simulatoare, fixtures, comportamente și teste de acceptanță. Efecte și rezultate verificate înainte de măsurarea performanței. Baseline rămâne plauzibil și corect: nu adăugăm intenționat întârzieri, bug-uri sau duplicări.

## Metrici și proveniență

- LOC aplicație separat de teste, simulatoare comune, configurare și cod generat. Definim exact instrumentul și excluderile.
- Complexitate pe funcție și agregate descriptive, fără a confunda numărul claselor cu calitatea.
- Fișiere și linii modificate față de commit-ul inițial; regresii și criterii de acceptanță trecute.
- Execuție: workload identic, instanțe resetate, warm-up și rulări repetate, fără console output în secțiunea cronometrată. Raportăm distribuții și mediul, nu un singur timp.
- Memoria procesului nu reprezintă automat memoria exclusivă a variantei. Dacă o măsurăm, folosim procese separate și documentăm runtime-ul/GC și limitele metodei.

Fiecare rezultat include commit SHA, comandă, data, OS/CPU, versiuni Node/npm, parametri, rezultate brute și un sumar. Nu versionăm volume mari de output sau secrete. Benchmark-ul complet și runner-ul AI sunt taskuri viitoare; bootstrap-ul nu publică rezultate.

## Protocol AI propus (Sprint 2–3)

1. Înghețăm ambele variante și definim feature-urile, testele externe, modelul disponibil și bugetul. Pilotul este separat de features finale.
2. Pregătim workspace-uri neutre A/B, fără celălalt cod, istoricul git complet, soluția de referință, rezultatele anterioare sau documente care indică soluția. Instrucțiunile experimentale comune înlocuiesc instrucțiunile arhitecturale ale dezvoltării.
3. Evaluatorul rulează în afara workspace-ului editabil; agentul nu poate citi/modifica testele externe. Nu este suficientă o instrucțiune în AGENTS.md. Expunem aceleași verificări locale ambelor variante.
4. Același model/snapshot când este disponibil, prompt funcțional, instrumente, limite și reguli privind intervenția umană. Documentăm diferențele inevitabile. Nu cerem explicit unui agent să folosească un anumit pattern.
5. Minimum trei rulări independente pe variantă dacă bugetul permite, ordine alternată/randomizată, reset complet la fiecare rulare. Nu ștergem eșecurile din rezultate.
6. Înregistrăm elapsed time, tokens input/output și cached când sunt disponibili, apeluri, feedback, diff și rezultatul evaluatorului. Tokenii se iau din usage real, nu se estimează din lungimea transcriptului. Nu declarăm precizie pe care instrumentul nu o oferă.
7. Timpul de succes este timpul până la prima versiune care trece criteriile. Rulările care ating bugetul se raportează ca incomplete. Prezentăm mediana, intervalul și rata de succes; nu generalizăm la toți developerii sau modelele.

## Structura raportului de curs

Introducere; Metodologie; Analiza modelelor; Eficiență și optimizare; Studiu comparativ; Impactul anti-pattern-urilor; Concluzii; Bibliografie. Calendarul și obiectivele detaliate ale sprinturilor sunt propuneri ale echipei, nu cerințe suplimentare atribuite profesorului.
