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

# TaskTracker v2.0 - The Cloud & Identity Era (Future Upgrages and Aspirations)

### 🎯 Project Scope
Version 2.0 marks the transition from anonymous local-first sessions to a fully authenticated cloud ecosystem. This release focuses on data sovereignty, secure cross-platform synchronization, and a "Bring Your Own Storage" (BYOS) architecture, giving users ultimate control over where their task data resides.

### 🏗️ Technical Architecture
* **Authentication**: Multi-modal system implementing **OAuth2** (Google/GitHub) and passwordless **OTP** (One-Time Password) via email.
* **Identity Mapping**: Backend logic that bridges existing `v1.1` Guest UUIDs to permanent authenticated User accounts.
* **Data Sovereignty (BYOS)**: A decoupled storage layer allowing users to link personal S3 buckets or cloud drives (Google Drive/Dropbox) as the primary data store for board metadata and attachments.
* **Security**: Implementation of **JWT (JSON Web Tokens)** for secure, stateless communication between the Web Dashboard and the Browser Extension.

### 🚀 Key Features
* **OAuth2 Integration**: One-tap secure login. On first sign-in, the system prompts to migrate any "Guest Mode" boards created in `v1.1` to the new permanent cloud profile.
* **OTP Passwordless Login**: A secure, friction-free entry method using 6-digit email verification codes, eliminating the need for traditional password management.
* **BYOS (Bring Your Own Storage)**: 
    * Users can provide their own API keys/Bucket details to host their task data.
    * Allows for "Local-Only" or "Private-Cloud" configurations while using the TaskTracker UI.
* **Cross-Platform Sync**: Real-time synchronization between the web application and the browser extension using a unified user identity.

### 🏗️ Architectural Changes
* **Storage Adapters**: Introduced a Spring Boot `StorageService` interface to handle seamless switching between the internal PostgreSQL database and external BYOS providers.
* **Token Lifecycle Management**: Advanced handling of JWT refresh tokens to maintain secure sessions across the web and extension popup.
* **Identity Migration Service**: Logic to re-parent `v1.1` database entities (Boards/Lists/Cards) to a new authenticated User ID upon OAuth completion.

### ⚡ Power-User Enhancements
* **Global Hotkeys**: Custom `Alt` shortcuts are now synchronized to the user profile and work identically in the browser extension.
* **Encrypted BYOS**: Optional client-side encryption for sensitive data being sent to user-owned cloud storage providers.
