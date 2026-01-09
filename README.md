```markdown
# Messenger-like Chat System (Java) — Auth + Real‑Time Chat (System Design Project)

A Messenger-style chat system built in Java.  
This repo is intentionally split into services (auth vs chat) to reflect production architecture & design decisions.

---

## Architecture (High Level)

```
Client
  |
  | 1) Register/Login (HTTP)
  v
auth-service (Spring Boot)
  - BCrypt password hashing
  - JWT issuance + JWT validation (Spring Security filter)
  |
  v
PostgreSQL (users)

Client
  |
  | 2) WebSocket Connect + JWT (handshake)
  v
chat-service (Spring Boot)
  - WebSocket endpoint
  - JWT validation during handshake
  - socket session ↔ userId mapping
  - real-time message routing
  - message persistence
  |
  v
PostgreSQL (messages)

Redis 
  - presence (online/offline)
  - delivery/read receipts state
```

**Design principle:** auth-service owns identity; chat-service owns message delivery + storage.  
Services share identity via JWT, not by sharing databases.

---

## Tech Stack

- Java 17
- Spring Boot 4.x
- Spring Security (JWT validation in auth-service)
- WebSockets (chat-service)
- PostgreSQL
- Docker + Docker Compose
- Redis 

---

## Repository Structure

```
.
├── docker-compose.yml
└── backend
    ├── auth-service
    │   ├── Dockerfile
    │   └── ...
    └── chat-service
        ├── Dockerfile
        └── ...
```


## Services

### auth-service (HTTP)
Responsibilities:
- Register user
- Hash passwords with BCrypt
- Login
- Issue JWT (`sub = userId`)
- Validate JWT via `JwtAuthenticationFilter`
- Provide `/me` endpoint to verify auth end-to-end

Base URL: `http://localhost:8080`

### chat-service (WebSocket)
Responsibilities:
- WebSocket endpoint: `/ws/chat`
- Authenticate WebSocket handshake via JWT
- Map socket session to `userId`
- Route messages user → user
- Persist messages in DB

Base URL: `ws://localhost:8081`

---

## Configuration: Shared JWT Secret

Both services must share the same JWT signing secret so chat-service can validate tokens issued by auth-service.

## Quick Start (Docker)

### 1) Build JARs 
Each Dockerfile copies `target/*.jar`, so build first:

```bash
cd backend/auth-service
mvn clean package -DskipTests

cd ../chat-service
mvn clean package -DskipTests

cd ../../
```

### 2) Start everything
```bash
docker compose up --build
```

### 3) Confirm containers
```bash
docker compose ps
```

Expected:
- `postgres` running
- `auth-service` running on `localhost:8080`
- `chat-service` running on `localhost:8081`
- `redis` running on `localhost:6379`

---

## Auth API (auth-service)

### Register
```bash
curl -i -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### Login (get JWT)
```bash
curl -i -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

Response:
```json
{ "token": "eyJhbGciOi..." }
```

### Verify JWT end‑to‑end (`/me`)
```bash
curl -i http://localhost:8080/me \
  -H "Authorization: Bearer <PASTE_JWT_HERE>"
```

Expected:
```json
{"email":"test@example.com"}
```

Notes:
- `/me` (and any non-auth endpoint) requires a valid JWT

---

## Chat (chat-service)

### WebSocket Authentication Approach
Browsers cannot reliably set custom `Authorization` headers for native WebSocket connections, so chat-service supports passing JWT via query param:

```
ws://localhost:8081/ws/chat?token=<JWT>
```

### Test with wscat
Install:
```bash
npm i -g wscat
```

Connect:
```bash
wscat -c "ws://localhost:8081/ws/chat?token=<PASTE_JWT_HERE>"
```

### Send a message (JSON protocol)
Client sends:
```json
{ "to": "<receiverUuid>", "content": "hello" }
```

Example:
```json
{"to":"1458f069-b7a0-49ad-821a-19b016cd6710","content":"hello"}
```

Expected:
- Sender receives an ACK (e.g. status + messageId)
- Receiver receives the message in real time if connected
- Message is persisted into Postgres (`messages` table)

---

## Data Model

### users (auth-service)
- `id` (UUID)
- `email`
- `password_hash`
- `created_at`

### messages (chat-service)
- `id` (UUID)
- `sender_id` (UUID)
- `receiver_id` (UUID)
- `content`
- `created_at`

**Note:** chat-service stores user references as UUIDs without foreign keys to auth-service tables. This keeps services decoupled.

---

## Troubleshooting

### Postgres role/db does not exist after changing env vars
Postgres only applies `POSTGRES_USER/DB/PASSWORD` on first initialization. Reset dev volumes:

```bash
docker compose down -v
docker compose up --build
```

### Docker build fails: `COPY target/*.jar ... no such file`
Build the JARs first:

```bash
mvn clean package -DskipTests
```

### WebSocket connect fails in browser
Use query param auth:
```
/ws/chat?token=<JWT>
```

---

## Roadmap

Next enhancements:
- Message history endpoint with pagination
- Presence (Redis): online/offline tracking + broadcasts
- Delivered/read receipts (deliveredAt/readAt)
- Ordering guarantees + idempotency handling
- Offline delivery + reconnect synchronization
- Multi-instance scaling + failure scenarios documentation

---