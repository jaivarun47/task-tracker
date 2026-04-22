# TaskTracker v1.1 - Midnight Glass & User Overhaul

### 🎯 Project Scope
A massive architectural and visual evolution. [cite_start]This version introduces a professional relational database schema, a premium "Midnight Glass" aesthetic, and a personalized Guest Identity system[cite: 12, 17, 21].

### 🏗️ Technical Architecture
* [cite_start]**Backend**: Spring Boot (JPA/Hibernate) with a dedicated **DTO Layer** (Data Transfer Objects) to prevent recursion and decouple API from the DB[cite: 2, 12].
* [cite_start]**Database**: PostgreSQL (Dockerized) using **UUIDs** for all primary keys to support secure, unique identity[cite: 10, 12].
* [cite_start]**Frontend**: React with **Glassmorphism CSS** and **Inter** typography[cite: 17].
* [cite_start]**Identity System**: Anonymous Guest Auto-Registration using `localStorage` and custom `X-Guest-User-Id` headers[cite: 9, 11].

### 💎 Key Visual & UX Improvements
* [cite_start]**Glassmorphism UI**: High-contrast frosted glass panels using `backdrop-filter: blur(25px)` over a deep midnight radial gradient[cite: 17, 30].
* [cite_start]**Masonry Grid**: Optimized board area using `display: grid` with `grid-auto-flow: dense`, allowing lists to stack vertically and fill gaps[cite: 24].
* [cite_start]**Information Density**: Reduced list widths and "Hover-to-Expand" card logic to maximize screen real estate[cite: 25, 27, 28].
* [cite_start]**Keyboard Mastery**: Conflict-free **Alt-key** shortcuts (`Alt+N`, `Alt+Shift+B`) and `Esc` mapping for high-speed navigation[cite: 30, 31, 32].

### 🏗️ Architectural Changes
* [cite_start]**User-Level Entity**: Every board is now linked to a specific `User` UUID, ensuring users only see their own data[cite: 12, 13].
* [cite_start]**Guest Auto-Registration**: The backend dynamically creates a `User` entity the first time it sees a new Guest UUID[cite: 14, 15].
* [cite_start]**JPA Cascades**: Deleting a board now automatically and efficiently cleans up all nested data via database-level cascading[cite: 15].

### ⚡ Frictionless Flow
* [cite_start]Removed "Are you sure?" confirmation pop-ups for Lists and Cards to facilitate instant, power-user interactions[cite: 33].
