# DayGuard

DayGuard is a browser-based, one-to-one chat application. It combines a Spring Boot backend with a Vite frontend, uses STOMP over SockJS for live communication, and routes per-user messages through RabbitMQ.

Text messages are encrypted in the browser before they are sent. The server stores public pre-key material in PostgreSQL and forwards ciphertext, but does not persist conversations.

> [!IMPORTANT]
> DayGuard is an experimental project, not an audited secure messenger. The current key agreement is inspired by X3DH, but it does not implement the Signal protocol or a Double Ratchet. Attachments are sent as unencrypted Base64 data. See [Security and privacy](#security-and-privacy) before using the application with sensitive information.

## What works

- Account registration and login with either username or email
- Stateless authentication with access and refresh JWTs in HTTP-only cookies
- One-to-one, real-time text messaging over STOMP/SockJS
- Browser-side text encryption with identity keys, signed pre-keys, and one-time pre-keys
- Online-user presence and unread-message counters
- Image, PDF, text, DOC, and DOCX attachments up to 10 MB per file
- Responsive web interface
- Docker Compose definitions for development, tests, and deployment
- Backend JUnit and frontend Mocha test sources

## Current limitations

- Only text message bodies are encrypted. Attachment contents and metadata are not.
- Messages and attachments are transient: RabbitMQ delivers them to connected users, but there is no conversation history database.
- Private encryption keys are stored in the browser's IndexedDB, with `localStorage` as a fallback. They are not protected by a separate passphrase or synchronized between devices.
- Clearing site data, changing browsers, or using another device can make existing sessions impossible to decrypt.
- There are no group chats, delivery/read receipts, push notifications, account recovery, or key verification UI.
- The cryptographic design and implementation have not been independently audited.

## How it is built

| Area | Implementation |
| --- | --- |
| Backend | Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA |
| Realtime transport | STOMP over SockJS/WebSocket |
| Message routing | RabbitMQ with per-user queues created for connected users |
| Database | PostgreSQL for accounts and public pre-key material; H2 in tests |
| Frontend | Vite, vanilla JavaScript, Tailwind CSS |
| Browser cryptography | libsodium.js |
| Production edge | Nginx with TLS and reverse proxying |

Message flow:

```text
Browser A                         DayGuard backend                    Browser B
---------                         ----------------                    ---------
fetch B's pre-key bundle  ----->  PostgreSQL
derive a session key
encrypt text locally
publish ciphertext        ----->  STOMP endpoint
                                  RabbitMQ per-user queue  -------->  decrypt locally
```

The backend exposes REST endpoints under `/api/v1`, accepts SockJS connections at `/ws`, and accepts application messages at `/app/*`. The Vite development server proxies `/api` and `/ws` to the backend on port `8080`.

## Run locally

### Prerequisites

- Java 21
- Maven 3.9+ (the checked-in `mvnw` file is not executable)
- Node.js 22 and npm
- Docker with Docker Compose

### 1. Start the backend

Start the two services used by the backend:

```bash
docker compose --profile dev up -d postgres-dev rabbitmq-dev
```

Then run Spring Boot with its automatic Compose launch disabled. The datasource override also corrects the malformed JDBC URL currently present in `application-dev.properties`:

```bash
SPRING_DOCKER_COMPOSE_ENABLED=false \
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dayguard \
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Wait until Spring reports that the application has started on port `8080`.

Do not launch the entire `dev` profile for this revision. Its Redis service references `bitnami/redis:8.0.2`, an image tag that is no longer available, and Redis is not active in the application anyway.

### 2. Start the frontend

In a second terminal:

```bash
cd frontend
npm ci
npm run dev
```

Open <http://localhost:3000>. Register two accounts in separate browser profiles or one normal and one private window to test a conversation. Separate browser storage is important because encryption keys are stored locally.

### 3. Stop local services

Stop both application processes, then remove the development containers:

```bash
docker compose --profile dev down
```

Development PostgreSQL uses `create-drop`, so its schema is recreated when the backend restarts. The development Compose profile does not mount a PostgreSQL data volume.

## Configuration

The default Spring profile is `prod`; always select `dev` explicitly for local work. Profile-specific settings live in:

- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`
- `src/test/resources/application-test.properties`

Production Compose reads these values from a root-level `.env` file:

| Variable | Purpose | Example |
| --- | --- | --- |
| `POSTGRES_USER` | PostgreSQL username | `dayguard` |
| `POSTGRES_PASSWORD` | PostgreSQL password | a long random value |
| `POSTGRES_DB` | PostgreSQL database | `dayguard` |
| `RABBIT_USER` | RabbitMQ username | `dayguard` |
| `RABBIT_PASSWORD` | RabbitMQ password | a long random value |
| `REDIS_PASSWORD` | Redis container password | a long random value |
| `DOMAIN_NAME` | Deployment hostname passed to the Nginx container | `chat.example.com` |
| `ALLOWED_ORIGINS` | Allowed browser origin pattern | `https://chat.example.com` |

Create the file from the provided template and replace every placeholder:

```bash
cp .env.example .env
```

Do not commit `.env`, private keys, or real TLS certificates.

## Production deployment

The included production setup is tailored to the project's existing host layout. Before using it, review `compose.yaml` and `nginx.conf` carefully:

- Nginx expects the built frontend at `/home/deploy/app` on the host.
- `nginx.conf` currently hard-codes `server_name dayguard`; update it for your hostname. The `DOMAIN_NAME` environment variable does not rewrite this mounted file.
- TLS files must exist as `ssl/fullchain.pem` and `ssl/privkey.pem`.
- Only HTTPS port `443` is published.
- The backend image is pulled from `ghcr.io/day-fit/dayguard:latest`.
- Watchtower automatically checks for and replaces the backend container every 30 seconds.
- PostgreSQL data is written to `./pgdata`.

Build the frontend and place `frontend/dist` at the configured Nginx path, then start the stack:

```bash
cd frontend
npm ci
npm run build
cd ..

docker compose --profile prod up -d
```

The `nginx-free` profile does not publish the backend port in the current Compose file. If you want to provide your own reverse proxy, add an explicit loopback port mapping (for example `127.0.0.1:8080:8080`) or connect that proxy to the Compose network.

## Tests and checks

Backend tests are configured to use H2 plus RabbitMQ and Redis containers from the Compose `test` profile:

```bash
mvn test
```

Frontend commands:

```bash
cd frontend
npm ci
npm test
npm run build
```

At the time of this README rewrite, `npm run build` succeeds. The test suites have known code/configuration issues: backend tests fail during context startup because the unavailable `bitnami/redis:8.0.2` image prevents the `test` Compose profile from starting, and `npm test` fails because `frontend/src/main.js` accesses `document` in Mocha's Node environment. These are current project limitations, not extra setup steps.

## API and WebSocket overview

The browser client is the reference consumer. This table is intended as an orientation, not a stable public API contract.

| Method or destination | Purpose |
| --- | --- |
| `POST /api/v1/auth/register` | Create an account and upload its initial public pre-keys |
| `POST /api/v1/auth/login` | Authenticate and set access/refresh cookies |
| `POST /api/v1/auth/refresh` | Replace the access-token cookie |
| `POST /api/v1/auth/logout` | Expire both authentication cookies |
| `GET /api/v1/get-user-details` | Return the authenticated user's details |
| `GET /api/v1/active-users` | List currently connected users |
| `GET /api/v1/encryption/user/{id}/get-pre-key-bundle` | Consume a user's available public pre-key bundle |
| `POST /api/v1/encryption/upload-spk` | Replace the authenticated user's signed pre-key |
| `POST /api/v1/encryption/upload-opk-keys` | Add one-time public pre-keys |
| `/ws` | SockJS/STOMP handshake endpoint |
| `SEND /app/publish/text` | Route an encrypted text payload |
| `SEND /app/publish/attachment` | Route an attachment payload |

## Security and privacy

- Passwords are hashed with BCrypt at cost 12.
- Access and refresh tokens are placed in HTTP-only cookies. Production cookies are `Secure` and use `SameSite=Strict`; development cookies are intentionally relaxed for localhost.
- JWT signing keys are generated in memory and rotated daily. Restarting the backend invalidates existing tokens.
- Identity public keys, signed public pre-keys, signatures, and one-time public pre-keys are stored by the backend. Private keys remain in browser storage.
- Text encryption uses libsodium primitives after an X3DH-style key derivation. It does not currently provide Double Ratchet forward secrecy for an ongoing conversation.
- The server can observe account information, presence, sender/receiver routing, timestamps, attachment data, and message sizes.

Please report security problems privately to the maintainer instead of publishing exploitable details in a public issue.

## FAQ

### Is DayGuard ready for production use?

No. It is suitable for development, learning, and experimentation. The cryptography is unaudited, attachment encryption is missing, and operational hardening is incomplete.

### Are all messages end-to-end encrypted?

Text bodies are encrypted and decrypted in the browser. Attachments are not encrypted, even when sent together with text.

### Does the server store my conversations?

No conversation history is persisted. Messages pass through RabbitMQ queues created for active users. PostgreSQL stores accounts and public encryption-key material, not chat history.

### Can I message an offline user?

Not through the current UI. The recipient list contains connected users, and per-user queues are tied to active WebSocket sessions. Treat DayGuard as an online-only chat.

### Why did my old messages disappear after refreshing?

Message history only exists in the current page's memory. Refreshing or closing the page clears it.

### Can I use the same account on multiple devices?

Authentication may work, but encryption keys are stored per browser and are not synchronized. Multi-device use is not supported and may lead to decryption failures.

### What happens if I clear browser storage?

The private identity and signed pre-keys can be lost. The client may generate replacement material, but messages or sessions associated with the old keys may no longer decrypt.

### Why are there no other users in the sidebar?

Only currently connected users are listed. Open another browser profile, register or log in with a second account, and keep both sessions connected.

### Why does local login fail or the WebSocket stay disconnected?

Check that the backend is running with the `dev` profile and the overrides shown in [Run locally](#run-locally), PostgreSQL and RabbitMQ are healthy, the frontend is served from port `3000`, and cookies are enabled. Also confirm that ports `5432`, `5672`, and `8080` are not occupied by unrelated services.

### Why does `./mvnw` return “permission denied”?

The wrapper is committed without its executable bit. Use the installed `mvn` command, run `bash mvnw`, or make the wrapper executable in your local checkout with `chmod +x mvnw`.

### Why does the default backend startup try production settings?

`application.properties` selects `prod` by default. For local development, pass `-Dspring-boot.run.profiles=dev` as shown above.

### Is Redis required?

Redis services are declared in the Compose profiles and cache annotations exist in the backend, but the current application does not enable a Redis-backed Spring cache. Do not rely on Redis for application state in this revision.

### Where is the RabbitMQ management UI?

With the `dev` profile it is available at <http://localhost:15672>. The development image uses RabbitMQ's default local credentials unless you override them.

### How large can an attachment be?

The frontend accepts up to 10 MB per file and allows JPEG, PNG, GIF, WebP, PDF, plain text, DOC, and DOCX. Because files are Base64-encoded and sent through the messaging path, their transmitted payload is larger than the original file.

## Contributing

Keep changes focused and include tests where practical. Before opening a pull request, run the backend tests and the frontend test/build commands above, and describe any known failures honestly.

## License

DayGuard is available under the [BSD 3-Clause License](LICENSE).
