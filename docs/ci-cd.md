# CI și protecția main

## Pipeline

Workflow-ul [.github/workflows/ci.yml](../.github/workflows/ci.yml) rulează la PR către main, push pe main și manual din Actions.

- Java 21: Maven clean verify, formatare și teste.
- Comparație între demo-urile celor două engines și refuzul unei variante CLI invalide.
- Pornirea JAR-ului Spring, health și citirea stării prin HTTP. Testele Maven acoperă separat HTTP pe ambele engines.
- Rapoarte Surefire și loguri de smoke test descărcabile, inclusiv la eșec, păstrate 14 zile.
- La succesul jobului Java 21, JAR executabil, COMMIT.txt și checksum SHA-256 descărcabile ca artifact. Pentru o versiune validată, alegeți o rulare cu jobul check (21) verde; pentru ultima versiune integrată, rularea de pe main.
- Timeout 15 minute/job; un PR actualizat anulează rularea sa anterioară.

Din GitHub: Actions → CI → rulare → Artifacts → smart-home-server-.... Descărcați și extrageți arhiva, apoi:

```sh
java -jar smart-home-server.jar
java -jar smart-home-server.jar --smart-home.variant=baseline --server.port=8081
```

Acesta este build-ul livrabil automat. Nu există încă hosting sau deployment automat: o etapă de deploy se adaugă după alegerea mediului, fără a schimba engines.

## Politica cerută pentru main

Aceste instrucțiuni documentează setările; fișierele din repo nu activează branch protection.

În Settings → Branches → regula pentru main:

- Require a pull request before merging.
- Required approvals: **2**. Autorul nu își aprobă propriul PR.
- Dismiss stale pull request approvals when new commits are pushed.
- Require status checks to pass before merging: **check (21)**.
- Require branches to be up to date before merging.
- Require conversation resolution before merging.
- Do not allow bypassing the above settings (inclusiv administratori).
- Allow force pushes: dezactivat.
- Allow deletions: dezactivat.

Nu se schimbă politica de merge pentru celelalte branches. Un PR nou pentru main trebuie să aibă două aprobări actuale și jobul check (21) verde înainte de merge. Numele jobului este stabil; actualizați regula dacă va fi redenumit.

Starea efectivă a protecției se verifică în Settings; nu o deduceți din existența acestui document.

Referință pentru artifacts: https://docs.github.com/en/actions/tutorials/store-and-share-data
