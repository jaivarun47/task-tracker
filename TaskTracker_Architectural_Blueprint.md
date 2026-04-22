# Project TaskTracker: Architectural Blueprint & Roadmap (2026)

## 🏗️ Phase 1: Foundations & Infrastructure
### 1. Database Persistence (The "Immortal" Data)
* **Transition:** Shifted from in-memory **H2** to persistent **PostgreSQL** via **Docker**.
* **Infrastructure:** Database runs in a containerized environment with a dedicated volume for data persistence.
* **Connectivity:** Spring Boot backend communicates with Postgres on port `5432` using the `PostgreSQLDialect`.

### 2. Transactional Integrity & Cascading
* **The Issue:** Attempting to delete parent entities (Boards/Lists) failed due to foreign key constraints (orphaned children).
* **The Fix:** * Applied `@Transactional` to service layers to ensure "all-or-nothing" execution.
    * Implemented **JPA Cascading** (`CascadeType.ALL` + `orphanRemoval = true`) to allow automatic cleanup of child entities when a parent is deleted.

---

## 🌳 Phase 2: The "User Era" (Current Objective)
### 1. Hierarchical Restructuring
Moving from an orphaned collection of boards to a strict **User-Centric Tree**:
**User** → **Board** → **CardList** → **Card**

### 2. Object References vs. Flat IDs
* **Concept:** Replacing scalar `Long boardId` with direct `Board board` object references in Java.
* **Technical Challenge:** **Circular Logic/Infinite Recursion**. If a Board points to a List and the List points to a Board, JSON serialization (Jackson) will loop infinitely and crash the server.
* **Solution (Architectural):** **DTO (Data Transfer Object) Pattern**. Separating the "Database Entity" (complex relationships) from the "API Response" (clean, flat data for the UI).

---

## 🛡️ Phase 3: Security & Data Sovereignty
### 1. Modern Authentication
* **Focus:** OAuth2 (Google/GitHub) and Passwordless (Magic Links/Passkeys).
* **Logic:** Avoiding the security liability of managing raw passwords.
* **Identity:** Using OAuth tokens to establish both user identity and cloud storage permissions simultaneously.

### 2. BYOS: Bring Your Own Storage
* **Vision:** A privacy-first model where the user's data isn't just on your server, but backed up to their **personal Google Drive**.
* **Hybrid Model:**
    * **Managed Tier:** Fast, temporary storage on the app's Postgres DB.
    * **BYOS Tier:** Permanent, user-controlled storage via "App Data" folders in the user's personal cloud.

---

## 📜 Coding Commandments
1.  **Separation of Concerns:** Never expose Database Entities directly to the Frontend; always map to DTOs.
2.  **Mandatory Ownership:** No Board or List should ever be created without a valid `User` reference (`nullable = false`).
3.  **Transactionality:** Every service method modifying more than one table must be `@Transactional`.
4.  **Forward Compatibility:** Design the API for the *new* frontend, focusing on efficient, nested data loading.
