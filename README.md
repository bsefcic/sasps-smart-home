# Smart Home — Design Patterns Comparative Study

Proiect SASPS: simulator de casă inteligentă, dezvoltat de patru persoane în **Java**, în două variante echivalente funcțional:

- **Baseline:** control centralizat și apeluri directe.
- **Patterns:** responsabilități separate și patterns justificate.

Comparăm extensibilitatea, complexitatea, corectitudinea și costul la execuție. Ulterior evaluăm implementarea acelorași features de către același agent AI, în condiții controlate. Nu presupunem un câștigător.

## Quick start

Necesită **JDK 17+**. Verifică `java -version`; configurează `JAVA_HOME` către JDK dacă este necesar. Maven Wrapper descarcă versiunea 3.9.11 și dependențele la prima rulare, deci este necesar acces la internet. Maven nu trebuie instalat separat.

### macOS / Linux

```sh
git clone https://github.com/bsefcic/sasps-smart-home.git
cd sasps-smart-home
./mvnw clean verify
java -jar app/target/smart-home.jar baseline
java -jar app/target/smart-home.jar patterns
```

### Windows PowerShell

```powershell
git clone https://github.com/bsefcic/sasps-smart-home.git
cd sasps-smart-home
.\mvnw.cmd clean verify
java -jar app/target/smart-home.jar baseline
java -jar app/target/smart-home.jar patterns
```

În IDE importă `pom.xml` din rădăcină ca proiect Maven. Nu sunt necesare Spring, hardware, Docker, baze de date sau chei API. Comenzile presupun integrarea bootstrap-ului Java în main; înainte de integrare, folosește branch-ul PR-ului Java.

## Ce arată demo-ul

Ambele variante pornesc becul simulat `living-light` și afișează același JSON:

```json
{"result":{"ok":true},"snapshot":{"devices":[{"id":"living-light","kind":"light","power":true}]}}
```

O variantă necunoscută produce exit code nenul. Acesta este un demo fix, nu un parser general de scenarii JSON. CLI-ul complet este un task din Sprint 1.

## Comenzi uzuale

| Comandă macOS/Linux | Rol |
| --- | --- |
| `./mvnw clean verify` | Compilare strictă, teste JUnit, JAR și verificarea formatării Java |
| `./mvnw test` | Compilare și teste, fără toate verificările din verify |
| `./mvnw spotless:apply` | Formatare automată Java |
| `./mvnw spotless:check` | Verifică formatarea fără modificări |
| `java -jar app/target/smart-home.jar baseline` | Demo baseline, după build |
| `java -jar app/target/smart-home.jar patterns` | Demo patterns, după build |

Pe Windows înlocuiește `./mvnw` cu `.\mvnw.cmd`. Rulează comenzile Maven din rădăcină: reactorul construiește modulele în ordinea dependențelor. CI verifică JDK 17 și 21. Versiunile dependențelor directe și pluginurilor sunt stabilite în POM; nu există un lockfile tranzitiv complet.

## Structura repository-ului

| Director / modul Maven | Responsabilitate |
| --- | --- |
| `shared/contracts` | Interfețe și records comune, fără motorul comparat |
| `shared/simulators` | API fictiv al producătorului |
| `implementations/baseline` | Control direct, corect și plauzibil |
| `implementations/patterns` | Implementarea cu patterns |
| `app` | CLI și JAR executabil |
| `acceptance-tests` | Aceleași teste JUnit pentru ambele variante |
| `benchmarks` | Metodologie și scripturi de măsurare planificate |
| `experiments` | Experimente AI planificate pentru S2–S3 |
| `docs` | Scope, arhitectură, plan și protocolul evaluării |
| `.github` | CI și template-uri de colaborare |

Fiecare modul folosește directoarele Java standard `src/main/java` și `src/test/java`. Fișierele JSON comune se adaugă în `acceptance-tests/src/test/resources/fixtures`.

## Implementat în bootstrap

- Java 17+, Maven Wrapper și proiect modular, fără framework runtime.
- Un bec și un producător simulat.
- Control on/off în ambele variante; Command și Adapter în patterns.
- Records imuabile pentru snapshots și teste comune pentru control, erori și izolarea stării.
- Teste CLI, formatare Java și CI.

## Ce urmează

S1 adaugă termostatul, validarea inputului, scenarii plate, CLI pentru fișiere și primele măsurători. S2 urmărește al doilea producător, scenarii imbricate, senzori, Observer și Strategy. Nu există încă rezultate de benchmark sau experimente AI.

State, Decorator, undo, încuietori, jaluzele și dashboard rămân opționale. Nu sunt necesare integrări reale sau infrastructură distribuită.

## Începe un task

1. Citește [planul și backlog-ul](docs/plan.md), apoi [scope-ul](docs/scope.md).
2. Alege un issue cu dependențele integrate și anunță că îl preiei. Membrii se atribuie ulterior.
3. Urmează [CONTRIBUTING.md](CONTRIBUTING.md): branch, verificări și review de la alt coleg.
4. Pentru AI citește [AGENTS.md](AGENTS.md), instrucțiunile locale și [workflow-ul AI](docs/ai-workflow.md).

## Evaluare și documentație

- [Arhitectură și decizii](docs/architecture.md)
- [Metodologie și experiment AI](docs/evaluation.md)
- [Planul sprinturilor](docs/plan.md)

Metrici: LOC, complexitate, modificări, regresii, execuție și, unde metoda permite, memorie. AI: timp până la trecerea testelor, tokeni real raportați, încercări și rezultate incomplete. Evaluatorul extern este protejat prin izolare, nu doar prin instrucțiuni.

Raportul urmează structura din materialele profesorului: introducere, metodologie, analiza modelelor, eficiență și optimizare, studiu comparativ, impactul anti-pattern-urilor, concluzii și bibliografie.
