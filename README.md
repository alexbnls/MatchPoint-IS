<div align="center">
  <img src="docs/logo_matchpoint.png" alt="MatchPoint Logo" width="600">
</div>

**Piattaforma per l'organizzazione e la gestione di eventi sportivi amatoriali.**

MatchPoint è un sistema software progettato per connettere appassionati di sport, permettendo loro di organizzare partite, trovare compagni di squadra e valutare l'affidabilità e la sportività degli altri utenti.

---

## Indice
1. [Funzionalità Principali](#-funzionalità-principali)
2. [Tecnologie Utilizzate](#-tecnologie-utilizzate)
3. [Architettura e Design Pattern](#-architettura-e-design-pattern)
4. [Installazione e Configurazione](#-installazione-e-configurazione)
5. [Testing e Quality Assurance](#-testing-e-quality-assurance)
6. [Team di Sviluppo](#-team-di-sviluppo)

---

## Funzionalità Principali

### 👤 Gestione Utenti
* **Registrazione & Login:** Sistema sicuro con hashing delle password (SHA-256).
* **Profilo Utente:** Visualizzazione delle statistiche personali (Rating Abilità, Affidabilità, Sportività).

### ⚽ Gestione Eventi
* **Creazione Evento:** L'organizzatore può creare partite specificando sport, data, ora e luogo.
* **Geolocalizzazione:** Integrazione con **OpenStreetMap** per convertire indirizzi in coordinate (e viceversa).
* **Iscrizione:** Gli utenti possono iscriversi agli eventi fino al raggiungimento del numero massimo di partecipanti.
* **Annullamento:** L'organizzatore può annullare l'evento; il sistema notifica automaticamente gli iscritti.

### ⭐ Sistema di Feedback
* **Valutazione:** Al termine di un evento, i partecipanti possono valutarsi a vicenda su tre metriche:
    * Abilità
    * Affidabilità
    * Sportività
* **Calcolo Media:** Algoritmo di media ponderata per aggiornare il rating utente.

---

## Tecnologie Utilizzate

* **Linguaggio:** Java 17 o superiore
* **Framework:** Spring Boot 3.x
* **Database:**
    * MySQL 8.0
* **ORM:** Hibernate / Spring Data JPA
* **Build Tool:** Maven
* **Testing:** JUnit 5, Mockito
* **API Documentation:** Swagger

---

## Architettura e Design Pattern

Il progetto segue un'architettura a tre livelli (**Interface - Application - Storage**) per garantire la separazione delle responsabilità. Sono stati implementati i seguenti **Design Pattern**:

### 1. Facade Pattern (`MappeServiceFacade`)
Utilizzato per isolare la complessità dell'interazione con le API esterne di **OpenStreetMap (Nominatim)**. Il sistema non dipende direttamente dalle chiamate HTTP, ma da un'interfaccia semplificata.

### 2. Observer Pattern
Implementato per la gestione delle notifiche. Quando un evento cambia stato (es. viene **ANNULLATO**), il sistema notifica automaticamente tutti gli osservatori (gli utenti iscritti) via email.

---

## ⚙️ Installazione e Configurazione

### Prerequisiti
* JDK 17 o superiore installato.
* MySQL Server installato e attivo.
* Maven installato.

### 1. Configurazione Database
Crea un database vuoto su MySQL:
```sql
CREATE DATABASE matchpoint_db;
```

### 2. Configurazione `application.properties`
Modifica il file `src/main/resources/application.properties` con le tue credenziali:
```
spring.datasource.url=jdbc:mysql://localhost:3306/matchpoint_db?createDatabaseIfNotExist=true
spring.datasource.username=IL_TUO_USERNAME
spring.datasource.password=LA_TUA_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

### 3. Avvio dell'applicazione
Da terminale, nella root del progetto:
```
mvn spring-boot:run
```
L'applicazione sarà disponibile su: `http://localhost:8080`

### (Opzionale) 4. Documentazione API con Swagger
Una volta avviato, puoi testare le API direttamente da browser: http://localhost:8080/swagger-ui/index.html

## Testing e Quality Assurance
Il progetto include una suite di **Unit Test** e **Integration Test** realizzati con JUnit 5 e Mockito.

### Eseguire i test:
```
mvn test
```
### Copertura del Codice
La copertura dei test è stata monitorata tramite IntelliJ Coverage Runner.
- **Business Logic (Service Layer)**: > 95% di copertura funzionale. 
- I test unitari isolano le dipendenze esterne (DB, API Mappe) tramite **Mock**, garantendo velocità e affidabilità.

## Team di Sviluppo
Progetto realizzato per il corso di **Ingegneria del Software** (Prof. Carmine Gravino) - Università degli Studi di Salerno.
- **Gaetano Aprile** 
- **Luigi Artuso** 
- **Alessandro De Bonis** 
- **Marco Galdi**
## 📄 Licenza
Questo progetto è stato sviluppato esclusivamente a scopo didattico e accademico. Tutti i marchi e le tecnologie citate appartengono ai rispettivi proprietari.