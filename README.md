# Smart Home — Design Patterns Comparative Study

Proiect pentru disciplina **Stiluri arhitecturale și șabloane de proiectare software (SASPS)**.

## Scop

Dezvoltarea unui simulator de casă inteligentă în două variante echivalente funcțional:

- **Baseline:** implementare cu logică centralizată, condiționale și dependențe directe.
- **Patterns:** implementare care utilizează design patterns justificate de problemele aplicației.

Proiectul urmărește compararea impactului acestor abordări asupra extensibilității, complexității, testabilității și performanței.

O componentă experimentală va analiza implementarea acelorași funcționalități noi de către același model AI în ambele variante.

## Domeniul aplicației

Sistemul permite controlul unor dispozitive simulate și definirea unor automatizări pentru o casă inteligentă.

Nu este necesar hardware fizic. Interacțiunea se realizează prin CLI sau API.

### Funcționalități planificate

- Înregistrarea și controlul dispozitivelor:
  - becuri;
  - termostate;
  - jaluzele;
  - încuietori.
- Simularea senzorilor de temperatură și mișcare.
- Executarea comenzilor individuale.
- Definirea și executarea scenariilor, precum „Plec de acasă”.
- Gruparea comenzilor și includerea scenariilor în alte scenarii.
- Declanșarea automatizărilor pe baza evenimentelor senzorilor.
- Alegerea unor politici de control al temperaturii.
- Integrarea unor API-uri simulate de producători diferiți.

## Design patterns vizate

| Pattern | Utilizare |
|---|---|
| Command | Încapsularea și executarea acțiunilor asupra dispozitivelor |
| Composite | Construirea scenariilor din comenzi și alte scenarii |
| Adapter | Uniformizarea interfețelor diferiților producători |
| Observer | Notificarea automatizărilor la schimbarea valorilor senzorilor |
| Strategy | Alegerea politicilor de control al temperaturii |

Patterns suplimentare, precum State sau Decorator, pot fi introduse dacă funcționalitățile implementate le justifică.

## Metodologia comparației

Cele două variante vor utiliza:

- aceleași cerințe funcționale și reguli de validare;
- același limbaj și aceeași infrastructură relevantă;
- aceleași date și scenarii de test;
- aceleași teste de acceptanță;
- condiții comparabile pentru măsurarea performanței.

Varianta baseline trebuie să fie funcțională și plauzibilă. Problemele de proiectare și anti-pattern-urile analizate vor fi identificate explicit.

Nu presupunem că utilizarea design patterns îmbunătățește toate metricile.

## Metrici urmărite

### Structura codului

- Număr de linii de cod.
- Complexitate ciclomatică.
- Dependențe între module.
- Distribuția responsabilităților.

Codul aplicației, testele și codul generat vor fi raportate separat.

### Extensibilitate și corectitudine

- Fișiere modificate pentru o funcționalitate nouă.
- Linii adăugate și șterse.
- Regresii introduse.
- Rezultatele testelor de acceptanță.

### Execuție

- Timp de execuție pentru scenarii identice.
- Consum de memorie.
- Overhead introdus de abstracții.

## Experimentul cu AI

Același model AI va primi aceeași cerință de extindere pentru fiecare variantă, în sesiuni independente.

Vom păstra comparabile instrucțiunile, instrumentele și bugetele de execuție. Fiecare rulare va porni de la versiunea inițială stabilită.

Vom înregistra:

- timpul până la trecerea testelor de acceptanță;
- tokenii de input și output;
- numărul de încercări;
- fișierele și liniile modificate;
- testele trecute și regresiile;
- eventualele rulări incomplete.

Exemple de extensii:

- integrarea unui producător nou;
- adăugarea unei politici de temperatură;
- introducerea unui nou consumator al evenimentelor;
- implementarea anulării comenzilor reversibile.

Testele de acceptanță vor fi stabilite înaintea experimentului și protejate împotriva modificării de către agent.

## Livrabile

- Cele două implementări.
- Teste comune de acceptanță.
- Scenarii și scripturi de evaluare.
- Rezultatele experimentelor.
- Raport comparativ și demonstrație.

## Stadiu

Proiectul se află în etapa de definire a cerințelor și arhitecturii. Funcționalitățile și experimentele descrise sunt planificate.
