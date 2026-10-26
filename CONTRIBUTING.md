# Contribuții

## Pornire

Instalează un JDK 17 sau mai nou și verifică `java -version`. JAVA_HOME trebuie să indice JDK-ul dacă mediul îl cere. Maven nu trebuie instalat separat: wrapper-ul din repo descarcă versiunea 3.9.11 la prima rulare. Este necesar internet pentru descărcarea Maven și a dependențelor inițiale.

- macOS/Linux: `./mvnw clean verify`
- Windows PowerShell: `.\mvnw.cmd clean verify`
- Formatare Java: `./mvnw spotless:apply` (Windows: `.\mvnw.cmd spotless:apply`).
- După build: `java -jar app/target/smart-home.jar baseline`, apoi aceeași comandă cu `patterns`.

În IntelliJ IDEA, Eclipse sau VS Code importă `pom.xml` din rădăcină ca proiect Maven. Nu sunt necesare Node.js, Docker, API keys, baze de date sau hardware.

## Un issue → un branch → un PR

Preia un issue ale cărui dependențe sunt integrate și anunță că lucrezi la el. Membrii se atribuie ulterior. Pleacă din main actualizat după integrarea bootstrap-ului Java. Nume de branch: `feat/<issue-number>-short-name`, `fix/...` sau `docs/...`. Nu lucra direct pe main. Până la integrarea bootstrap-ului, coordonează explicit pornirea din branch-ul Java; nu recrea structura independent.

Un task public acoperă ambele implementări, dacă issue-ul nu spune altceva. PR-ul descrie comportamentul, motivul, verificările și issue-ul (`Closes #...`). Review-ul este făcut de alt coleg. Nu include refactorizări fără legătură.

## Definition of Ready

Issue-ul are scop, criterii verificabile, variante afectate și dependențe. Contractul necesar este stabilit, iar dependențele sunt integrate. Orele sunt estimări, nu obligații individuale.

## Definition of Done

- Criteriile de acceptanță sunt demonstrate, cu teste relevante.
- `./mvnw clean verify` trece și CI este verde; orice verificare nerulată este explicată.
- Demo-urile și documentația afectată sunt actualizate.
- Un coleg a făcut review, observațiile importante sunt rezolvate și PR-ul este integrat.
- Măsurătorile au commit, comandă și condiții reale; nu inventăm cifre.

Aceste fișiere nu activează automat protecțiile de branch sau aprobările obligatorii.

## Cod și teste

Codul, package-urile și commit-urile sunt în engleză; documentația cursului în română. Folosește Java 17 standard, fără preview, cu interfețe mici și dependențe explicite. Nu introduce Spring sau Lombok în acest simulator mic.

Testele unitare stau în modulul testat. Aceeași suită parametrizată din acceptance-tests rulează pe ambele variante. Schimbările de contract se aplică coerent, cu review; în experimentele controlate, agentul nu poate modifica evaluatorul extern. Vezi docs/ai-workflow.md.
