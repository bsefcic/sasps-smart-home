# Benchmark-uri

Output-ul temporar este ignorat în `output/`.

Rularea măsoară, pentru ambele engines (`baseline` și `patterns`):

- **LOC** (`loc`): numărul de linii de cod din `implementations/baseline`, `implementations/patterns` și `shared/*`, adică linii fizice din fișierele `.java` care conțin cod în afara comentariilor. Liniile goale și cele doar cu comentarii nu se numără.
- **Timpul de execuție** (`runtime`): durata apelurilor directe către engine, pentru trei workloads: aprinderea/stingerea luminii, schimbarea temperaturii țintă și executarea scenelor. Pentru fiecare se raportează timpii bruți ai fiecărei runde, mediana, minimul și maximul (ms), plus mediana per pas (ns/comandă sau ns/scenă).

## Rulare

Toate comenzile se rulează din rădăcina repository-ului. Este nevoie doar de JDK 21; nu trebuie pornit niciun server, deoarece engine-urile sunt apelate direct, fără Spring/HTTP.

1. Construiește proiectul (compilează, rulează testele, verifică formatarea și creează JAR-ul):

   ```powershell
   .\mvnw.cmd clean verify
   ```

   Pe Linux/macOS: `./mvnw clean verify`.

2. Rulează măsurătorile:

   ```powershell
   java -jar benchmarks/target/smart-home-benchmarks.jar            # loc, apoi runtime
   java -jar benchmarks/target/smart-home-benchmarks.jar loc        # doar LOC
   java -jar benchmarks/target/smart-home-benchmarks.jar runtime    # doar runtime
   ```

   Din alt director, adaugă `--root <cale-către-repo>` la final.

   Exemplu rulare:

   java -jar benchmarks/target/smart-home-benchmarks.jar        
    Physical lines in .java files with code outside comments; blank and comment-only lines excluded (not logical LOC).
    baseline      217 lines in    4 files
    patterns      381 lines in   14 files
    shared        176 lines in   14 files
    total         774 lines in   32 files
    Commit: d9a708fed9ee917f56b9f824c9b3d552e2e679ce (.git)
    Wrote C:\sasps-smart-home\benchmarks\output\results_141126_205333\loc.json
    Running runtime benchmark Config[warmupSteps=200000, rounds=10, stepsPerRound=1000000] ...
    light-toggle:
    baseline   median 8.130 ms (min 6.791, max 11.467), 8.1 ns/command
    patterns   median 8.303 ms (min 5.048, max 16.157), 8.3 ns/command
    thermostat:
    baseline   median 11.035 ms (min 10.520, max 12.366), 11.0 ns/command
    patterns   median 101.037 ms (min 96.861, max 123.481), 101.0 ns/command
    scene:
    baseline   median 19.784 ms (min 19.045, max 37.329), 19.8 ns/scene
    patterns   median 111.320 ms (min 107.459, max 126.705), 111.3 ns/scene
    Commit: d9a708fed9ee917f56b9f824c9b3d552e2e679ce (build, uncommitted changes)
    Wrote C:\sasps-smart-home\benchmarks\output\results_141126_205333\runtime.json

    Exemplu rezultate:
    LOC:
    {
        "mode" : "loc",
        "provenance" : {
            "commitSha" : "d9a708fed9ee917f56b9f824c9b3d552e2e679ce",
            "commitShaSource" : ".git",
            "commitDirty" : null,
            "timestampUtc" : "2026-11-14T18:53:33.460Z",
            "arguments" : [ ],
            "javaVersion" : "21.0.12.1",
            "javaVendor" : "Oracle Corporation",
            "javaVmName" : "Java HotSpot(TM) 64-Bit Server VM",
            "osName" : "Windows 11",
            "osVersion" : "10.0",
            "osArch" : "amd64",
            "availableProcessors" : 20
        },
        "results" : {
            "definition" : "Physical lines in .java files with code outside comments; blank and comment-only lines excluded (not logical LOC).",
            "categoryRoots" : {
            "baseline" : [ "implementations/baseline/src/main/java" ],
            "patterns" : [ "implementations/patterns/src/main/java" ],
            "shared" : [ "shared/contracts/src/main/java", "shared/simulators/src/main/java" ]
            },
            "categories" : {
            "baseline" : {
                "files" : 4,
                "codeLines" : 217
            },
            "patterns" : {
                "files" : 14,
                "codeLines" : 381
            },
            "shared" : {
                "files" : 14,
                "codeLines" : 176
            }
            },
            "total" : {
            "files" : 32,
            "codeLines" : 774
            }
        }
    }
    
    Runtime:
    {
        "mode" : "runtime",
        "provenance" : {
            [...]
        },
        "results" : {
            "config" : {
            "warmupSteps" : 200000,
            "rounds" : 10,
            "stepsPerRound" : 1000000
            },
            "workloads" : {
            "light-toggle" : {
                "description" : "execute(SetLightPower(living-light)) alternating on/off, starting with on; final state: light off, thermostat unchanged",
                "stepUnit" : "command",
                "warmupOrder" : [ "baseline", "patterns" ],
                "rounds" : [ {
                "round" : 1,
                "order" : [ "baseline", "patterns" ],
                "ms" : {
                    "baseline" : 11.4667,
                    "patterns" : 16.1568
                }}
                [...]],
                "engines" : {
                "baseline" : {
                    "rawMs" : [ 11.4667, 9.4253, 9.2587, 9.3749, 9.9323, 6.9479, 7.0016, 6.8057, 6.9048, 6.7912 ],
                    "medianMs" : 8.13015,
                    "minMs" : 6.7912,
                    "maxMs" : 11.4667,
                    "medianNsPerStep" : 8.13015
                },
                "patterns" : {
                    "rawMs" : [ 16.1568, 8.2661, 8.486, 8.3396, 9.3329, 9.7468, 5.3352, 5.186, 5.1705, 5.0483 ],
                    "medianMs" : 8.30285,
                    "minMs" : 5.0483,
                    "maxMs" : 16.1568,
                    "medianNsPerStep" : 8.30285
                }}},
                [...]}}}

3. Rezultatele sunt scrise în `benchmarks/output/results_DDMMYY_HHMMSS/` (ora locală), în câte un fișier `loc.json` și `runtime.json`. Consola afișează un sumar pentru fiecare mod și commit-ul asociat.

Observații:

- `runtime` folosește parametrii impliciți: warm-up de 200 000 de pași, apoi 10 runde × 1 000 000 de pași pentru fiecare engine și workload. Durează mai mult decât `loc`.
- Pentru `runtime`, commit-ul este cel încorporat în JAR la build; pentru `loc`, este HEAD-ul citit din `.git`. Un rezultat citabil trebuie produs dintr-un build fără modificări necomise (`commitDirty: false`).
- Rebuild-ul este necesar după orice modificare a engine-urilor; altfel JAR-ul măsoară codul vechi.

## Fișiere sursă

Cod (`src/main/java/ro/upb/sasps/smarthome/benchmarks/`):

| Fișier | Descriere |
| --- | --- |
| `MetricsMain.java` | Punctul de intrare. Citește argumentele (`loc`, `runtime`, `--root`), rulează modurile cerute, afișează sumarul și scrie rapoartele JSON în folderul rulării. |
| `LocCounter.java` | Numără liniile de cod din `implementations/baseline`, `implementations/patterns` și `shared/*` (doar `src/main/java`). Exclude liniile goale și pe cele care conțin doar comentarii. |
| `RuntimeBenchmark.java` | Măsoară timpul de execuție: warm-up, apoi runde pe instanțe noi, cu ordinea engine-urilor alternată între runde. Calculează mediana, minimul și maximul în ms și mediana în ns/pas. |
| `Workload.java` | Interfața unui scenariu de măsurat: rulează bucla de pași (singura parte cronometrată) și verifică starea finală. |
| `LightToggle.java` | Workload: aprinde/stinge alternativ `living-light`; un pas este o comandă. |
| `ThermostatChange.java` | Workload: setează alternativ temperatura țintă la 20.5 și 21.5 °C; un pas este o comandă. |
| `SceneToggle.java` | Workload: execută alternativ scenele `arrive-home` și `leave-home` (lumină + temperatură); un pas este o scenă. |
| `HomeChecks.java` | Verificări comune ale stării casei (lumină, temperatură), făcute în afara cronometrării. |
| `Stats.java` | Statistici descriptive pe valorile brute: mediană, minim, maxim. |
| `Provenance.java` | Contextul fiecărui rezultat: commit SHA (din JAR sau din `.git`), momentul rulării, argumentele, JDK, sistem de operare, arhitectură și numărul de procesoare. |

Teste (`src/test/java/ro/upb/sasps/smarthome/benchmarks/`):

| Fișier | Descriere |
| --- | --- |
| `StatsTest.java` | Verifică mediana pentru un număr impar și par de valori, plus minimul și maximul. |
| `LocCounterTest.java` | Numără liniile unui fișier Java fictiv, fix, cu comentarii, linii goale și un string care conține `//`; se așteaptă 6 linii de cod. |
