# TaskTracker v1.0 - Classic Foundation

### 🎯 Project Scope
This version represents the initial MVP (Minimum Viable Product) of the TaskTracker application. [cite_start]It was designed to demonstrate core task management capabilities using a flat data model and standard web components before the implementation of the User-level entity and JPA relationship overhaul[cite: 5, 20].

### 🏗️ Technical Architecture
* [cite_start]**Backend**: Spring Boot 3.x using a direct-to-entity approach[cite: 5].
* [cite_start]**Database**: Initial support for H2 (In-Memory) for rapid prototyping and PostgreSQL (Dockerized) using standard `Long` (BigInt) primary keys[cite: 5].
* [cite_start]**Frontend**: React 18+ with **Bootstrap 5** for a responsive, high-visibility layout[cite: 16].
* [cite_start]**Data Structure**: A straightforward hierarchy consisting of **Boards** -> **Lists** -> **Cards**[cite: 1, 5].

### 🚀 Key Features
* [cite_start]**Standard Horizontal Layout**: A traditional board view where lists grow horizontally, appended to the right side of the screen[cite: 16, 24].
* [cite_start]**Global Access**: No authentication or user separation; all data is served globally from the database, meaning every user sees the same content[cite: 5, 6].
* [cite_start]**Visual Style**: Clean, high-contrast UI featuring standard Bootstrap cards, buttons, and light/dark theme defaults[cite: 16, 17].
* [cite_start]**Manual CRUD Operations**: Full ability to create, read, update, and delete boards, lists, and cards[cite: 1].
* [cite_start]**Safety Confirmation**: Standard browser `confirm()` dialogs used for all deletion actions to prevent accidental data loss[cite: 33].

### 🔌 API Communication (v1.0)
The API in this version communicated directly with JPA entities without an abstraction layer:
* [cite_start]`GET /api/boards`: Fetched all boards available in the system[cite: 2].
* [cite_start]`POST /api/boards`: Created a new board with basic title/description strings[cite: 2].
* [cite_start]`DELETE /api/boards/{id}`: Removed a board; child deletion (lists/cards) was handled via manual loops in the Service layer[cite: 2].

### 📉 Limitations (Pre-Overhaul)
* [cite_start]**Lack of Privacy**: No `User` entity existed to manage data ownership; any visitor could modify any board[cite: 5, 6].
* [cite_start]**UI Inefficiency**: The horizontal-only board growth made it difficult to manage high volumes of lists[cite: 24].
* [cite_start]**Serialization Risks**: Directly returning entities posed risks for infinite recursion loops in JSON responses before the introduction of DTOs[cite: 2].
