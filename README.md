# TaskTracker v1.1 - Midnight Glass & User Overhaul

### 🎯 Project Scope
A massive architectural and visual evolution. This version introduces a professional relational database schema, a premium "Midnight Glass" aesthetic, and a personalized Guest Identity system[cite: 12, 17, 21].

### 🏗️ Technical Architecture
* **Backend**: Spring Boot (JPA/Hibernate) with a dedicated **DTO Layer** (Data Transfer Objects) to prevent recursion and decouple API from the DB[cite: 2, 12].
* **Database**: PostgreSQL (Dockerized) using **UUIDs** for all primary keys to support secure, unique identity[cite: 10, 12].
* **Frontend**: React with **Glassmorphism CSS** and **Inter** typography[cite: 17].
* **Identity System**: Anonymous Guest Auto-Registration using `localStorage` and custom `X-Guest-User-Id` headers[cite: 9, 11].

### 💎 Key Visual & UX Improvements
* **Glassmorphism UI**: High-contrast frosted glass panels using `backdrop-filter: blur(25px)` over a deep midnight radial gradient[cite: 17, 30].
* **Masonry Grid**: Optimized board area using `display: grid` with `grid-auto-flow: dense`, allowing lists to stack vertically and fill gaps[cite: 24].
* **Information Density**: Reduced list widths and "Hover-to-Expand" card logic to maximize screen real estate[cite: 25, 27, 28].
* **Keyboard Mastery**: Conflict-free **Alt-key** shortcuts (`Alt+N`, `Alt+Shift+B`) and `Esc` mapping for high-speed navigation[cite: 30, 31, 32].

### 🏗️ Architectural Changes
* **User-Level Entity**: Every board is now linked to a specific `User` UUID, ensuring users only see their own data[cite: 12, 13].
* **Guest Auto-Registration**: The backend dynamically creates a `User` entity the first time it sees a new Guest UUID[cite: 14, 15].
* **JPA Cascades**: Deleting a board now automatically and efficiently cleans up all nested data via database-level cascading[cite: 15].

### ⚡ Frictionless Flow
* Removed "Are you sure?" confirmation pop-ups for Lists and Cards to facilitate instant, power-user interactions[cite: 33].

---

## 🚀 Running Locally

### 1. Start PostgreSQL (via Docker Compose)
```bash
cd tasktracker-backend
docker compose up -d
```
*PostgreSQL will be available at `localhost:5555`.*

### 2. Start Backend (Spring Boot)
```bash
cd tasktracker-backend
./mvnw spring-boot:run
```
*(On Windows Command Prompt / PowerShell, use `./mvnw.cmd spring-boot:run`)*
*The backend connects to PostgreSQL on `localhost:5555` and listens on port `8080`.*

### 3. Start Frontend (React + Vite)
```bash
cd tasktracker-frontend
npm install
npm run dev
```
*The frontend will run on `http://localhost:5173` and proxy `/api` requests to `http://localhost:8080`.*

---

## 🌐 Deployment Configuration

The backend is configured via standard environment variables with automatic local fallbacks:

| Variable | Description | Local Default | Production Example |
| :--- | :--- | :--- | :--- |
| `PORT` | Backend HTTP port | `8080` | `10000` (auto-assigned by platform) |
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5555/tasktracker_db` | `jdbc:postgresql://db-host:5432/tasktracker_db?sslmode=require` |
| `DATABASE_USERNAME` | Database username | `root` | `prod_user` |
| `DATABASE_PASSWORD` | Database password | `password` | `prod_secure_password` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:5173,http://127.0.0.1:5173` | `https://your-tasktracker.vercel.app` |
| `JPA_DDL_AUTO` | Hibernate schema mode | `update` | `update` / `validate` |
| `SHOW_SQL` | Log SQL queries | `true` | `false` |
