# TreadTask

A full-stack task management application built with React, Spring Boot, and PostgreSQL, designed around a flexible Kanban-style workflow.

🌐 **Live Application:** https://treadtask.vercel.app

---

## ✨ Features

### 📋 Kanban Task Management

- Create and manage multiple boards
- Create lists within boards
- Create, edit, and delete cards
- Move cards within the same list
- Move cards between lists
- Reorder cards using drag and drop
- Reorder lists using drag and drop
- Persistent card and list ordering

### 🎨 Modern UI

- Luxury Blue visual system
- Light and dark themes
- Smooth theme transitions
- Responsive desktop, tablet, and mobile layouts
- Masonry-style board layout
- Collapsible sidebar
- Responsive list and card sizing
- Touch-friendly controls

### ⚡ UX

- Optimistic UI updates
- Rollback on failed mutations
- Keyboard shortcuts
- Keyboard-friendly dialogs
- Automatic focus when creating cards
- Timed Undo notifications for deletions
- Collapsible long lists
- Empty-board handling
- Accessible interactive controls

### 🔐 Authentication

TreadTask uses lightweight session-based authentication.

A session is created through the backend and the resulting token is stored client-side. API requests automatically include the session token when required.

---

## 🏗️ Architecture

```text
                    ┌──────────────────────┐
                    │      TreadTask       │
                    │    React + Vite      │
                    └──────────┬───────────┘
                               │ HTTPS
                               ▼
                    ┌──────────────────────┐
                    │    Spring Boot API   │
                    │       :8080          │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │      PostgreSQL      │
                    │        RDS           │
                    └──────────────────────┘

### Frontend

- React
- Vite
- Custom CSS design system
- Context-based application state
- Native HTML5 drag and drop

### Backend

- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- DTO-based API layer
- Session-based authentication
- Global exception handling

### Infrastructure

- AWS Elastic Beanstalk
- AWS RDS PostgreSQL
- Nginx
- Let's Encrypt
- DuckDNS
- Vercel

---

## 🗂️ Project Structure

```text
TreadTask/
├── tasktracker-backend/
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── .platform/
│   │   └── nginx/
│   ├── eb-deploy/
│   └── pom.xml
│
├── tasktracker-frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── context/
│   │   ├── hooks/
│   │   └── styles/
│   ├── public/
│   ├── package.json
│   └── vite.config.js
│
└── README.md

---

## 🚀 Running Locally

### Prerequisites

Make sure you have:

- Java 17+
- Maven Wrapper
- Node.js
- npm
- Docker

---

### 1. Start PostgreSQL

From the backend directory:

```bash
cd tasktracker-backend
docker compose up -d
```

PostgreSQL is configured for:

```
localhost:5555
```

---

### 2. Start the Backend

From `tasktracker-backend`:

#### Linux / macOS

```bash
./mvnw spring-boot:run
```

#### Windows

```bash
mvnw.cmd spring-boot:run
```

The backend runs on:

```
http://localhost:8080
```

---

### 3. Start the Frontend

Open another terminal:

```bash
cd tasktracker-frontend
npm install
npm run dev
```

The frontend runs on:

```
http://localhost:5173
```

Vite proxies API requests to the local Spring Boot backend.

---

## ⚙️ Configuration

The backend uses environment variables with local development fallbacks.

| Variable | Description | Local Default |
| --- | --- | --- |
| `PORT` | Backend HTTP port | `8080` |
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5555/tasktracker_db` |
| `DATABASE_USERNAME` | Database username | `root` |
| `DATABASE_PASSWORD` | Database password | `password` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:5173,http://127.0.0.1:5173` |
| `JPA_DDL_AUTO` | Hibernate schema mode | `update` |
| `SHOW_SQL` | Enable SQL logging | `true` |

Production values should be provided through the deployment environment rather than committed to the repository.

---

## 🔌 API Overview

### Boards

```
GET    /api/boards
POST   /api/boards
PUT    /api/boards/{boardId}
DELETE /api/boards/{boardId}
```

### Lists

```
GET    /api/boards/{boardId}/lists
POST   /api/boards/{boardId}/lists
PUT    /api/boards/{boardId}/lists/{listId}
DELETE /api/boards/{boardId}/lists/{listId}

PATCH  /api/boards/{boardId}/lists/{listId}/move
```

### Cards

```
GET    /api/lists/{listId}/cards
POST   /api/lists/{listId}/cards
PUT    /api/lists/{listId}/cards/{cardId}
DELETE /api/lists/{listId}/cards/{cardId}

PATCH  /api/lists/{listId}/cards/{cardId}/move
```

### Sessions

```
POST /api/sessions
```

---

## 🔀 Ordering & Movement

Cards and lists use persistent zero-based positions.

```
0 → first
1 → second
2 → third
...
```

The backend handles:

- Position insertion
- Position normalization
- Same-list reordering
- Cross-list movement
- Cross-board list movement
- Position gap closing after deletion
- Existing-data position migration

The frontend uses optimistic updates so drag-and-drop interactions feel immediate while still supporting rollback if the server rejects an operation.

---

## 🧪 Testing

Backend tests can be run with:

```bash
./mvnw test
```

Windows:

```bash
mvnw.cmd test
```

Frontend lint:

```bash
npm run lint
```

Frontend production build:

```bash
npm run build
```

---

## 🌐 Deployment

### Frontend

The React frontend is deployed through Vercel.

Production:

```
https://treadtask.vercel.app
```

### Backend

The Spring Boot backend is deployed on AWS Elastic Beanstalk and uses:

- AWS RDS PostgreSQL
- Nginx reverse proxy
- HTTPS via Let's Encrypt
- DuckDNS API hostname

Production API:

```
https://tasktracker-api.duckdns.org
```

The frontend communicates with the backend exclusively over HTTPS in production.

---

## 📦 Deployment Bundle

The Elastic Beanstalk deployment bundle contains the application JAR, Procfile, and persistent Nginx configuration:

```
Procfile
TaskTracker-0.0.1-SNAPSHOT.jar

.platform/
└── nginx/
    └── conf.d/
        └── https.conf
```

Keeping the `.platform` configuration in the deployment bundle ensures the Nginx HTTPS configuration is reproducible across Elastic Beanstalk deployments.

---

## 🛠️ Tech Stack

| Layer | Technology |
| --- | --- |
| Frontend | React |
| Build Tool | Vite |
| Styling | Custom CSS |
| Backend | Spring Boot |
| ORM | Hibernate / JPA |
| Database | PostgreSQL |
| Authentication | Session-based token authentication |
| Cloud | AWS |
| Backend Hosting | Elastic Beanstalk |
| Database Hosting | RDS |
| Reverse Proxy | Nginx |
| TLS | Let's Encrypt |
| DNS | DuckDNS |
| Frontend Hosting | Vercel |

---

## 📌 Release History

### v1.3.4

Mobile responsiveness and final UX refinement.

- Responsive mobile/tablet layouts
- Touch-friendly controls
- Mobile sidebar improvements
- Responsive masonry behavior
- Mobile modal improvements
- Mobile keyboard handling
- Improved card/list interactions
- Empty-board UX refinement
- Light/dark theme preserved across responsive layouts

### v1.3.3

Visual and theme overhaul.

- Luxury Blue design system
- Light/dark theme support
- Theme persistence
- Smooth theme transitions
- Updated visual hierarchy
- Refined sidebar, cards, lists, modals, and controls

### v1.3.2

Major frontend structural and UX overhaul.

- Horizontal Kanban layout
- Collapsible sidebar
- Native drag-and-drop
- Optimistic UI updates
- Rollback handling
- Custom modal/toast system
- Improved component architecture
- Bootstrap removed

### v1.3

Backend movement and persistent ordering.

- Persistent card/list positions
- Card reordering
- Card movement between lists
- List reordering
- List movement between boards
- Position migration for existing data
- Integration and service tests

---

## 📄 License

This project is currently intended as a personal portfolio project.

