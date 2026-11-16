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

Rulează `python3 docs/http/replay-sprint-1.py` (pe Windows, `py -3 docs/http/replay-sprint-1.py`). Secvența implicită urmează [slide-ul 8](https://docs.google.com/presentation/d/1BR46JtfRzYi-L0v9NTtwSrE2J5pq3d83h9XBfdAA8ts/edit#slide=id.p8). Scriptul folosește doar biblioteca standard Python, afișează statusul și JSON-ul fiecărei cereri, verifică rezultatele și compară răspunsurile între variante. Alternativ, trimite manual cererile de mai jos din [fișierul .http](http/smart-home.http), întâi pe 8080, apoi pe 8081.

| Pas | Cerere sub `/api/v1` | Rezultat observat pe ambele servere |
| --- | --- | --- |
| Inventar | `GET /home` | 200, `living-light` oprit, `living-thermostat` la 20°C |
| Away from Home (`leave-home`) | `POST /scenes/execute` | 200, două comenzi executate, bec oprit, 18°C |
| Dispozitiv incompatibil în scenariu (`incompatible-device-after-valid`) | `POST /scenes/execute` | 422 `INCOMPATIBLE_DEVICE`, `commandIndex=1` |
| Inventar final | `GET /home` | 200, becul rămâne oprit, termostatul rămâne la 18°C |

În scena incompatibilă, prima comandă ar aprinde becul, dar a doua încearcă să-i seteze temperatura. Cererea trece validarea HTTP; implementarea respinge întreaga scenă la verificarea compatibilității. Inventarul final confirmă că nu s-a executat nici prima comandă.

Pentru o repetiție mai amplă, adaugă `--extended` la comanda de replay. Modul include health, controlul separat al becului și termostatului, plus `unknown-device` (404 fără index), `invalid-middle` (422, index 1) și `unknown-device-after-valid` (404, index 2). După fiecare respingere, scriptul recitește inventarul.

Încheie cu [nota de progres](progress-sprint-1.md): explică unde se află Adapter, Command și Composite și arată o valoare LOC și o mediană pentru fiecare variantă. Benchmark-ul se rulează separat de demo, fără servere pornite; la prezentare folosește rezultatele capturate.

**Rezervă pentru prezentare:** [transcript HTTP](results/sprint-1/http.txt), [LOC](results/sprint-1/loc.json) și [timpii pe runde](results/sprint-1/runtime.json). Două repetiții locale, cu ambele procese repornite între ele, au produs același transcript.
