# Lucrul cu AI în echipă

## Dezvoltare obișnuită

Orice coleg poate folosi instrumentul preferat. Issue-ul și documentele repo-ului sunt sursa cerințelor. Agentul nu primește libertatea de a extinde scope-ul. Autorul citește și verifică rezultatul înaintea PR-ului.

Prompt de pornire:

> Citește AGENTS.md, instrucțiunile locale, docs/scope.md și issue-ul indicat. Explică pe scurt planul, implementează numai criteriile de acceptanță, păstrează echivalența variantelor și rulează ./mvnw clean verify. Nu modifica reguli sau teste pentru a ascunde defecte. Raportează schimbările, comenzile rulate și limitele rămase.

- Începe dintr-un branch actualizat și lucrează la un singur issue.
- Atașează criteriile și dependențele, nu tot istoricul conversațiilor.
- Nu permite modificarea celeilalte componente în paralel fără coordonare.
- Nu introduce chei, date reale sau fișiere locale în prompturi/commit-uri.
- Menționează în PR instrumentul folosit și ce a verificat autorul. Nu este necesar transcriptul complet pentru fiecare task obișnuit.

## Experiment controlat

Acesta este separat de dezvoltare. Nu folosim rulările obișnuite ca rezultate științifice. Modelul, instrucțiunile, bugetele și evaluatorul sunt stabilite înainte. Vezi `evaluation.md`; nu rula experimente finale înaintea înghețării versiunilor și implementării izolării.
