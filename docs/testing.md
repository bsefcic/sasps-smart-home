# Verificări S1 — echivalență și separare arhitecturală

Acest document corespunde taskului [S1-10 din ClickUp](https://app.clickup.com/t/1500510000000520/1ampjurnxmg),
numit „Revizuirea suitei comune și protejarea separării engines” în secțiunea 07 din
[ghidul local S1](tasks-sprint-1.md). Specificația rămâne [contractul v1](contracts-v1.md).

## Cum se rulează

Din rădăcina repository-ului, cu JDK 21 și `JAVA_HOME` configurat:

```sh
./mvnw spotless:apply
./mvnw clean verify
```

Pe Windows: `.\mvnw.cmd spotless:apply`, respectiv `.\mvnw.cmd clean verify`.
CI execută același `clean verify` pe Java 21, inclusiv guard-ul arhitectural.
Rapoartele sunt în `*/target/surefire-reports` și în artifact-ul CI cu rapoarte de teste.

Pentru contracte și suita core, inclusiv guard, fără pornirea testelor HTTP:

```sh
./mvnw -pl acceptance-tests -am test
```

`HomeAcceptanceTest` rulează fiecare caz cu `BaselineHome::new` și `PatternsHome::new`.
`BaselineHttpTest` și `PatternsHttpTest` moștenesc aceeași clasă `HttpContractTest`,
cu servere reale pe porturi aleatoare și proprietăți de variantă diferite.
Nu există suite funcționale divergente pentru cele două engines.

## Matricea acoperită

| Comportament | Contracte Java | Suita core comună | Suita HTTP comună |
| --- | --- | --- | --- |
| Inventar inițial, ordine după ID, separarea câmpurilor light/thermostat | Records de snapshot | `controlsTemperatureAndPreservesSnapshots` | `exposesHealthAndAnImmutableSnapshot` |
| Bec on/off și stare păstrată între apeluri | `SetLightPower` | `appliesPowerCommands` | `keepsStateAcrossRequestsAndCapturesCommandSnapshot`, `executesMixedScenesInOrderAndReturnsFinalSnapshot` |
| Temperatură 5/35°C, pas 0.1 și repetarea aceleiași valori | `acceptsExactTemperatureBoundsAndTenths` | `controlsTemperatureAndPreservesSnapshots` | `temperaturePersistsAcrossRequests` |
| Temperatură în afara intervalului și precizie invalidă | `rejectsInvalidTemperature` | `rejectsWrongTypesAndUnknownThermostats` | `invalidTemperatureIs422`: 422, fără modificări |
| NaN și ±Infinity | `rejectsInvalidTemperature`: `IllegalArgumentException` | `rejectsWrongTypesAndUnknownThermostats`: respingere la construirea comenzii | `rejectsNonJsonNumbersAndNumericStrings`: tokeni JSON nevalizi sau string-uri → 400; `invalidTemperatureIs422`: ±1e309 → 422 după conversia numerică, fără modificări |
| ID invalid, null, spații și limita 64/65 | `rejectsInvalidIds`, `deviceIdsHaveA64CharacterLimit`, pentru ambele comenzi | Validarea are loc în constructor, înaintea engine-ului | Shape-ul JSON este verificat de `rejectsMalformedOrWrongShapeWithoutMutating`; limitele exhaustive ale ID-ului sunt testate la nivel de contract |
| Dispozitiv necunoscut și ambele incompatibilități | Rezultate comune `CommandResult` | `rejectsUnknownDeviceWithoutSideEffects`, `rejectsWrongTypesAndUnknownThermostats` | `unknownDeviceIs404AndDoesNotMutate`, `inventoryErrorsDoNotMutateState` |
| Scene mixte, ordine, ultima setare câștigă | `SceneRequest` | `executesMixedScenesInOrder` | `executesMixedScenesInOrderAndReturnsFinalSnapshot` |
| Scene de 1/50 comenzi; respingerea 0/51 | `scenesDefensivelyCopyAndHaveFixedBounds` | `executesMixedScenesInOrder` | `acceptsOneAndFiftyCommandsButRejectsFiftyOne`, `validatesEverySceneInputBeforeCallingEngine` |
| Erori de inventar la început/mijloc/final, primul index invalid, fără efecte parțiale | `CommandResult.atCommand` | `prevalidatesEverySceneCommand`, cu rezultate așteptate explicite | `sceneInventoryErrorsAreIndexedAndHaveNoEffects` |
| Input invalid într-o scenă, înaintea execuției | `rejectsMissingSceneCommandsAndNullElements` | Constructorul respinge lista invalidă | `validatesEverySceneInputBeforeCallingEngine`: 422, `commandIndex=1`, fără modificări |
| Nume null/blank, păstrarea spațiilor și limita de 64 unități UTF-16 | `rejectsMissingOrBlankSceneNames`, `sceneNamesPreserveWhitespaceAndCountUtf16Units` | Validare în `SceneRequest` | Matricea exhaustivă a numelor este verificată la nivel de contract |
| Liste copiate defensiv și nemodificabile | `snapshotsDefensivelyCopyTheirDeviceList`, `scenesDefensivelyCopyAndHaveFixedBounds` | `protectsSnapshotsAndInstances`, `controlsTemperatureAndPreservesSnapshots`: snapshot vechi stabil și case independente | HTTP verifică snapshot-ul rezultat și starea ulterioară; imutabilitatea obiectelor Java este verificată în contracte/core |
| Acces concurent și snapshot aferent propriei comenzi | — | Engines sunt folosite serial | `serializesConcurrentCommandsWithTheirOwnResultSnapshot`: lock-ul comun din server |

Clasele de contract sunt în `shared/contracts/src/test/java`, cele core în
`acceptance-tests/src/test/java`, iar cele HTTP în `server/src/test/java`.
Testele unitare pentru simulator, Adapter, Command, SceneCommand și Household/Proxy
rămân în modulele lor. Matricea de mai sus descrie verificările comune ale contractului S1;
nu pretinde că orice regulă este repetată la toate nivelurile.

## Guard pentru dependențe directe

`ArchitectureBoundaryTest.productionModulesRespectDirectDependencyBoundaries` citește
`src/main/java/**/*.java` și `pom.xml` din cele patru module:
`shared/contracts`, `shared/simulators`, `implementations/baseline`, `implementations/patterns`.
Locația repository-ului este transmisă explicit de Surefire prin `repositoryRoot`,
relativ la directorul modulului acceptance-tests. Lipsa unui POM, a directorului de surse
sau a fișierelor Java face verificarea să eșueze, în loc să treacă fără să inspecteze nimic.

Sunt respinse:

- Importurile normale, statice și wildcard din `org.springframework.*` sau
  `com.fasterxml.jackson.*` în production code din shared/engines.
- Importurile din package-ul patterns în baseline și invers.
- Dependențele Maven cu grup Spring/Jackson, cu orice scope diferit de `test`.
- Dependența `smart-home-patterns` în baseline și `smart-home-baseline` în patterns.
  Verificarea folosește numele artifact-ului pentru a nu depinde de forma groupId-ului.

Guard-ul citește atât dependențele directe ale proiectului, cât și pe cele declarate
în profilele POM-ului, inclusiv profile momentan inactive. Comentariile Java și XML,
sursele `src/test/java`, dependențele cu scope `test`, BOM-urile din dependencyManagement
și pluginurile de build nu sunt tratate ca dependențe production directe.
Spring și Jackson rămân permise în server; modulul acceptance-tests poate depinde de
ambele engines pentru suita comună.

Celelalte teste din aceeași clasă folosesc fișiere temporare pentru a demonstra că
importurile interzise în ambele direcții, importurile statice/wildcard și dependențele
Spring/Jackson sau între engines sunt detectate. Un caz verifică profilele și un caz
verifică acceptarea importurilor neutre, comentariilor și dependențelor de test.
Nu se adaugă biblioteci sau un analizor AST.

**Limite:** este un guard pentru declarații directe, nu o demonstrație completă a
arhitecturii. Nu calculează modelul Maven efectiv, proprietățile interpolate, dependențele
moștenite sau tranzitive. Nu analizează utilizări Java cu nume complet calificate fără
import, reflecție, bytecode, surse generate, Unicode escapes sau sintaxa din string-uri/text blocks.
Nici copierea logicii între engines ori orchestrarea ascunsă în shared nu sunt detectate;
acestea cer review. Comentariile sunt eliminate printr-o expresie regulată simplă.

## Verificare locală S1-10

Rulare din 13 noiembrie 2026, pe macOS 15.7.7 arm64, OpenJDK Homebrew 21.0.11, pe branch-ul
`feat/s1-10-architecture-equivalence`, pornit din main `2c97080`:

- `./mvnw spotless:apply`: succes.
- `./mvnw clean verify`: succes; 235 teste, zero failures/errors/skipped.
- Exemplele din `docs/http/smart-home.http`, trimise către JAR-ul împachetat pentru
  patterns:8080 și baseline:8081: health și inventar 200; light-on, temperature-22 și
  leave-home 200; unknown-device 404; invalid-middle 422 cu `commandIndex=1`.
  Starea este neschimbată după ambele cereri respinse și identică între variante.

Comenzile de pornire pentru repetarea verificării manuale:

```sh
java -jar server/target/smart-home-server.jar
java -jar server/target/smart-home-server.jar --smart-home.variant=baseline --server.port=8081
```

Se rulează în două terminale. Exemplele HTTP se trimit întâi pe portul 8080, apoi pe 8081.
Rezultatele locale nu înlocuiesc CI și cele două aprobări de la colegi diferiți de autor.
Integrarea S1-06 poate reorganiza testele HTTP în jurul fixtures, păstrând comportamentele
din această matrice; nu este o dependență pentru guard sau pentru suita core S1-10.
