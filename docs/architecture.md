# Arhitectură

## Module

shared/contracts conține records/interfețe și constrângeri de input, fără Spring/Jackson și fără logica comparată. shared/simulators conține API-ul fictiv Vendor A. baseline și patterns depind doar de aceste două module și nu se importă reciproc.

server este granița Spring Boot comună: JSON → RequestMapper → HomeSession → SmartHome. HomeConfiguration injectează o instanță BaselineHome sau PatternsHome pe baza smart-home.variant. HomeSession serializează comenzile, scenariile și citirile și capturează snapshot-ul sub același lock; răspunsurile sunt imuabile. Nu creăm o casă nouă la fiecare request.

app păstrează demo-ul CLI opțional. acceptance-tests verifică engines direct; server/src/test verifică aceeași suită HTTP cu câte un server real pentru fiecare variantă. Fixture-urile din acceptance-tests/src/test/resources sunt comune și disting starterul de ținta finală S1.

## Decizii

1. Java 17+, Spring Boot 3.5.16 și Maven Wrapper 3.9.11. Spring BOM aliniază dependențele, inclusiv JUnit; pluginurile sunt versionate explicit. Fără preview, Lombok sau framework în engines.
2. O singură implementare per proces, selectată la startup; pentru comparație rulează două procese pe porturi diferite. Niciun header/body nu schimbă varianta.
3. HTTP parsing, strict shape și sincronizarea sunt infrastructură identică. Executarea/prevalidarea inventarului/scenariilor și politicile rămân separate în engines.
4. Contract v1 fix: docs/contracts-v1.md și OpenAPI. NOT_IMPLEMENTED delimitează explicit features încă de implementat, nu substituie rezultatul final.
5. Spring servește și OpenAPI static, fără generator sau UI. CORS este configurabil pentru un React local viitor; frontend-ul nu este scope curent.
6. Benchmark principal direct pe engines, separat de overhead-ul serverului. Nu declarăm orice if/switch anti-pattern și nu presupunem că patterns sunt mai rapide.

La extensii, documentează contextul, alegerea și consecințele. Nu pune DTO-uri de producător, comenzi executabile sau orchestration în contracte.
