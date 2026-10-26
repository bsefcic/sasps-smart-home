# Arhitectură și decizii inițiale

## Două implementări, aceeași interfață

Proiect Maven cu șase module, într-un singur repository:

| Modul | Rol și dependențe |
| --- | --- |
| shared/contracts | Interfețe și records pentru cereri/rezultate; fără implementări |
| shared/simulators | API fictiv Vendor A; fără orchestration sau politici |
| implementations/baseline | Control centralizat; depinde numai de contracts și simulators |
| implementations/patterns | Command și Adapter în starter; aceleași dependențe comune |
| app | CLI și JAR executabil; poate încărca ambele variante |
| acceptance-tests | Suită JUnit parametrizată pe ambele implementări |

Codul Java este în src/main/java, testele în src/test/java, fixtures în src/test/resources. Niciuna dintre variante nu trebuie să importe sau să declare dependență pe cealaltă. Nu partajăm motorul de comenzi, scenarii sau automatizări, fiindcă acestea sunt comparate.

## Starter

BaselineHome și PatternsHome implementează SmartHome și pornesc independent cu becul living-light oprit. HomeCommand/SetLightPower sunt DTO-uri; Command/SetLightPowerCommand reprezintă acțiunea executabilă GoF. Adapter traduce boolean în enum-ul API-ului Vendor A. HomeSnapshot și LightSnapshot sunt records imuabile, cu lista copiată defensiv.

Demo-ul produce JSON fix pentru un bec; nu este parser general și nu serializează input arbitrar. CLI-ul pentru fișiere este un task separat. Nu există Spring, server HTTP, baze de date sau hardware.

## Decizii

1. Java 17 minimum, fără preview. CI rulează pe JDK 17 și 21. Records/sealed interfaces sunt standard; extinderea tipurilor de comandă se face în contractul v1.
2. Maven Wrapper 3.9.11 și versiuni explicite pentru pluginuri/dependențele directe; un singur `clean verify` verifică toate modulele. Acesta nu este un lockfile complet pentru dependențele tranzitive.
3. JUnit 5 pentru teste; Spotless/google-java-format pentru stil; javac -Xlint:all -Werror pentru warnings. Compilarea și testele nu reprezintă o verificare statică exhaustivă a designului.
4. Un repository pentru colaborare; workspace-uri separate și neutre pentru experimentul AI. Nu expunem agentului cealaltă variantă sau soluția de referință.
5. Un proces local; infrastructură minimă. Enum-urile, if/switch și metodele directe nu sunt automat anti-pattern-uri. Măsurăm consecințele concrete.

Orice schimbare importantă documentează contextul, alegerea, alternativele și consecințele. Baseline rămâne plauzibil și corect; patterns nu sunt presupuse superioare la toate metricile.
