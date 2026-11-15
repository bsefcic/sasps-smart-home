# Demo REST — Sprint 1, 16 noiembrie 2026

Același JAR, două procese: `patterns` pe 8080 și `baseline` pe 8081. În 4–5 minute arătăm că acceptă aceleași cereri, ajung în aceeași stare și resping scenele invalide fără efecte parțiale.

Aplicația demonstrată a fost construită din commitul `1d0f425dbd03aa312109cc20bfc5845542bf412e`, cu S1-06, S1-07, S1-08 și S1-10 integrate. [Captura HTTP](results/sprint-1/http.txt) provine din acel build. Scriptul de mai jos este livrat odată cu acest ghid; pe revizia respectivă fără ghid, folosește [cererile din clientul HTTP](http/smart-home.http).

## Pregătire

Din rădăcina repository-ului, cu JDK 21 activ și porturile 8080 și 8081 libere:

```sh
git rev-parse HEAD
./mvnw clean verify
```

Pe Windows folosește `.\mvnw.cmd clean verify`. Pornește serverele în două terminale separate, din rădăcina repo:

```sh
java -jar server/target/smart-home-server.jar
```

```sh
java -jar server/target/smart-home-server.jar --smart-home.variant=baseline --server.port=8081
```

Așteaptă pornirea ambelor procese. Ele țin starea în memorie, separat. Repornește-le înainte de fiecare repetiție.

## Ordinea demonstrației

Rulează `python3 docs/http/replay-sprint-1.py` (pe Windows, `py -3 docs/http/replay-sprint-1.py`). Scriptul folosește doar biblioteca standard Python, afișează statusul și JSON-ul fiecărei cereri, verifică rezultatele și compară răspunsurile între variante. Alternativ, trimite manual cererile indicate mai jos din [fișierul .http](http/smart-home.http), întâi pe 8080, apoi pe 8081. După `leave-home` și după fiecare eroare, citește din nou starea cu `GET /api/v1/home`.

| Pas | Cerere sub `/api/v1` | Rezultat observat pe ambele servere |
| --- | --- | --- |
| Health | `GET /health` | 200, `{"status":"UP"}` |
| Inventar | `GET /home` | 200, `living-light` oprit, `living-thermostat` la 20°C |
| `light-on` | `POST /commands` | 200, o comandă executată, bec pornit, 20°C |
| `temperature-22` | `POST /commands` | 200, o comandă executată, bec pornit, 22°C |
| `leave-home` | `POST /scenes/execute` | 200, două comenzi executate, bec oprit, 18°C |
| `unknown-device` | `POST /commands` | 404 `UNKNOWN_DEVICE`, fără `commandIndex`, stare neschimbată |
| `invalid-middle` | `POST /scenes/execute` | 422 `INVALID_TEMPERATURE`, `commandIndex=1`, stare neschimbată |
| `unknown-device-after-valid` | `POST /scenes/execute` | 404 `UNKNOWN_DEVICE`, `commandIndex=2`, stare neschimbată |

La ultimele două scene, prima comandă ar aprinde becul. După respingere, `GET /home` dovedește că becul a rămas oprit și termostatul la 18°C. Prima scenă arată validarea valorilor la intrarea HTTP; a doua trece de forma JSON și arată validarea inventarului în engine.

Încheie cu [nota de progres](progress-sprint-1.md): explică unde se află Adapter, Command și Composite și arată o valoare LOC și o mediană pentru fiecare variantă. Benchmark-ul se rulează separat de demo, fără servere pornite; la prezentare folosește rezultatele capturate.

**Rezervă pentru prezentare:** [transcript HTTP](results/sprint-1/http.txt), [LOC](results/sprint-1/loc.json) și [timpii pe runde](results/sprint-1/runtime.json). Două repetiții locale, cu ambele procese repornite între ele, au produs același transcript.
