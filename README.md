# Smart Home — Design Patterns Comparative Study

Proiect SASPS: simulator de casă inteligentă, dezvoltat de patru persoane în **TypeScript**, în două variante echivalente funcțional:

- **Baseline:** control centralizat și apeluri directe.
- **Patterns:** responsabilități separate, cu patterns justificate de problemă.

Comparăm extensibilitatea, complexitatea, corectitudinea și costul la execuție. Ulterior evaluăm implementarea acelorași features de către același agent AI, în condiții controlate. Nu presupunem că varianta cu patterns câștigă la toate metricile.

## Quick start

Necesită **Node.js 24** și npm. Versiunea majoră este în `.nvmrc`; poți folosi `nvm install` / `nvm use` dacă ai nvm. Pe Windows, instalează Node 24 prin managerul preferat. Nu sunt necesare API keys, hardware, Docker sau baze de date.

```sh
git clone https://github.com/bsefcic/sasps-smart-home.git
cd sasps-smart-home
npm ci
npm run check
npm run demo -- baseline
npm run demo -- patterns
```

Starterul pornește un bec simulat, `living-light`, și afișează rezultatul în JSON. Ambele variante trebuie să producă același output. Un argument de variantă necunoscut este respins cu exit code nenul.

## Comenzi

| Comandă                    | Rol                                              |
| -------------------------- | ------------------------------------------------ |
| `npm run demo -- baseline` | Rulează exemplul baseline                        |
| `npm run demo -- patterns` | Rulează exemplul cu Command și Adapter           |
| `npm test`                 | Teste comune prin test runner-ul Node            |
| `npm run typecheck`        | Verificarea strictă a tipurilor                  |
| `npm run lint`             | ESLint                                           |
| `npm run format`           | Formatare                                        |
| `npm run check`            | Typecheck, lint, teste și verificarea formatării |

Node execută direct TypeScript cu sintaxă erasable; verificarea tipurilor este separată. Nu există încă un build de producție sau un parser general de scenarii.

## Structura repository-ului

| Director                   | Responsabilitate                                        |
| -------------------------- | ------------------------------------------------------- |
| `implementations/baseline` | Implementarea directă, funcțională și plauzibilă        |
| `implementations/patterns` | Implementarea cu patterns                               |
| `shared/contracts`         | Contracte și DTO-uri comune, fără logica comparată      |
| `shared/simulators`        | API-uri fictive de producători                          |
| `tests/acceptance`         | Aceleași teste pentru ambele variante                   |
| `tests/fixtures`           | Inputuri sintetice pentru CLI, de adăugat în S1         |
| `scripts`                  | Puncte de intrare și utilitare                          |
| `benchmarks`               | Metodologie și scripturi de măsurare, planificate în S1 |
| `experiments`              | Experimente AI, planificate în S2–S3                    |
| `docs`                     | Scope, arhitectură, plan și protocol de evaluare        |
| `.github`                  | CI și template-uri de colaborare                        |

## Ce este implementat

- Setup TypeScript strict, lint, formatter și CI.
- Un bec și un producător simulat.
- Control on/off în ambele variante; Command și Adapter în varianta patterns.
- Teste comune pentru control, dispozitiv necunoscut și izolarea stării.

## Ce urmează

Sprint 1 adaugă termostatul, validarea inputului, scenarii plate, CLI pe fișiere și măsurători inițiale. Sprint 2 urmărește integrarea celui de-al doilea producător, scenarii imbricate, senzori, Observer și Strategy. Nu există încă rezultate de benchmark sau experimente AI.

Extensii precum State, Decorator, undo și dashboard sunt opționale. Scope-ul obligatoriu nu include integrări reale sau infrastructură distribuită.

## Începe un task

1. Citește [planul și backlog-ul Sprint 1](docs/plan.md), apoi [scope-ul](docs/scope.md).
2. Alege un issue ale cărui dependențe sunt integrate și anunță că îl preiei. Membrii și assignees se adaugă ulterior.
3. Urmează [CONTRIBUTING.md](CONTRIBUTING.md): branch separat, verificări și review de la un coleg.
4. Pentru AI citește [AGENTS.md](AGENTS.md) și [workflow-ul AI](docs/ai-workflow.md).

## Evaluare și documentație

- [Arhitectură și decizii](docs/architecture.md)
- [Metodologia comparației și experimentul AI](docs/evaluation.md)
- [Planul celor trei sprinturi](docs/plan.md)

Metrici vizate: LOC și complexitate, suprafața schimbărilor, regresii, durată de execuție și, unde metoda permite, memorie. În experimentul AI: timp până la trecerea testelor, tokeni real raportați, încercări și rezultate incomplete. Testele externe sunt protejate prin izolare, nu doar prin instrucțiuni.

Raportul va urma structura din materialele profesorului: introducere, metodologie, analiza modelelor, eficiență și optimizare, studiu comparativ, impactul anti-pattern-urilor, concluzii și bibliografie.
