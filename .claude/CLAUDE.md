# Vault — Spring Boot Mock Backend for Petal

## Project Overview
Vault is a local-first Spring Boot mock backend that replaces the Go microservices powering the Petal financial platform. It runs as a single JAR, serves the exact same API surface the React frontend expects, and proxies live market data from Alpaca and Finnhub sandboxes. Auth, file storage, and wallets are fully mocked locally — no AWS dependencies.

## Why Vault Exists
The production Go backend (`~/GolandProjects/backend`) is a multi-service system tied to AWS Cognito, S3, Secrets Manager, and more. Vault strips all that away so the frontend team can develop against a real, running HTTP server with zero cloud dependencies.

## Tech Stack
- **Framework:** Spring Boot 4.0.3 (Java 21)
- **Database:** H2 embedded, file-backed (`./data/vault-db`)
- **ORM:** Spring Data JPA + Hibernate
- **Auth:** Self-signed HS256 JWTs + BCrypt + TOTP (replaces Cognito)
- **HTTP Client:** WebClient (Spring WebFlux) for Alpaca/Finnhub proxy
- **WebSockets:** Spring TextWebSocketHandler (raw, no STOMP)
- **API Docs:** SpringDoc OpenAPI (Swagger UI at `/swagger-ui.html`)
- **Build:** Maven
- **IDE:** IntelliJ IDEA

## Linked Frontend
```
Frontend repo:  /Users/ricardozavala/WebstormProjects/petal
Go backend:     /Users/ricardozavala/GolandProjects/backend
Architecture:   .claude/ARCHITECTURE.md
```

The frontend Vite dev server proxies all API calls to `localhost:8080`. Vault preserves every route prefix (`/u`, `/a`, `/f`, `/p`), query param name, and header convention (`Authorization: Bearer <id_token>`, `TOKEN: <user_id>`) so the React app needs **zero changes**.

---

## Multi-Agent Workflow

This project uses a 4-agent system. **Always check your role first:**

```
READ: .claude/MULTI_AGENT_PLAN.md
```

### Agent Roles
| Agent | Role | Responsibility |
|-------|------|----------------|
| 1 | Architect | Planning, design, architecture |
| 2 | Builder | Core implementation |
| 3 | Validator | Testing, QA, debugging |
| 4 | Scribe | Docs, refinement, examples |

### Communication Protocol
1. **Before starting work:** Read `MULTI_AGENT_PLAN.md`
2. **Claim a task:** Update status to "In Progress" with your agent name
3. **Finish a task:** Update status to "Complete" and add notes
4. **Need another agent:** Add message to "Inter-Agent Messages" section
5. **Blocked:** Mark task as "Blocked" with reason

---

## Project Structure

```
vault-backend/
├── pom.xml
├── application.yaml
├── data/                              (git-ignored, runtime)
│   ├── vault-db.mv.db
│   └── uploads/
│       ├── identity-docs/
│       ├── tax-forms/
│       └── avatars/
│
└── src/main/java/com/vault/
    ├── VaultBackendApplication.java   @SpringBootApplication
    │
    ├── config/
    │   ├── SecurityConfig.java        SecurityFilterChain, CORS, public paths
    │   ├── WebSocketConfig.java       Register WS handlers + TOKEN interceptor
    │   ├── WebClientConfig.java       Alpaca & Finnhub WebClient beans
    │   └── VaultProperties.java       @ConfigurationProperties("vault")
    │
    ├── security/
    │   ├── JwtProvider.java           HS256 sign/verify, token generation
    │   ├── JwtAuthFilter.java         OncePerRequestFilter, extracts principal
    │   └── UserPrincipal.java         implements UserDetails
    │
    ├── controller/
    │   ├── AuthController.java        /u/quick-auth, /verify-mfa-setup
    │   ├── OnboardingController.java  /u/save-*, /u/get-*, /u/upload-*
    │   ├── AccountController.java     /a/** — proxies to Alpaca
    │   ├── FinnhubController.java     /f/** — proxies to Finnhub
    │   ├── PetalController.java       /p/** — settings, wallets, keys
    │   └── AdminController.java       /u/admin/** — user management
    │
    ├── service/
    │   ├── AuthService.java           Register, login, MFA, JWT lifecycle
    │   ├── UserService.java           CRUD, profile, admin operations
    │   ├── OnboardingService.java     KYC flow, state machine
    │   ├── AlpacaProxyService.java    WebClient → Alpaca Broker REST
    │   ├── FinnhubProxyService.java   WebClient → Finnhub REST
    │   ├── MarketDataService.java     WS fan-out + simulated ticks
    │   ├── FileStorageService.java    Local disk (replaces S3)
    │   └── WalletService.java         Mock wallet ops
    │
    ├── entity/                        JPA @Entity classes
    │   ├── User.java
    │   ├── UserProfile.java
    │   ├── UserAddress.java
    │   ├── UserFinancial.java
    │   ├── UserEmployment.java
    │   ├── UserIdentity.java
    │   ├── UserDocument.java
    │   ├── UserAgreement.java
    │   ├── UserTaxForm.java
    │   ├── Account.java
    │   ├── AccountMembership.java
    │   ├── TradingAccount.java
    │   ├── Firm.java
    │   ├── Order.java
    │   ├── Position.java
    │   ├── Asset.java
    │   ├── Country.java
    │   ├── UserSetting.java
    │   ├── UserApiKey.java
    │   ├── UserWallet.java
    │   └── RefreshToken.java
    │
    ├── repository/                    Spring Data JPA interfaces (one per entity)
    │
    ├── dto/                           Request/response DTOs
    │   ├── QuickAuthRequest.java
    │   ├── QuickAuthResponse.java
    │   ├── CreateOrderRequest.java
    │   └── ErrorResponse.java
    │
    ├── websocket/
    │   ├── QuoteWebSocketHandler.java    /aq/get-quote
    │   ├── BarWebSocketHandler.java      /ab/get-bar
    │   ├── TradeWebSocketHandler.java    /at/get-trade
    │   └── WsTokenInterceptor.java       Extracts ?TOKEN= param
    │
    └── seed/
        └── DataSeeder.java            CommandLineRunner: countries, enums, demo user
```

---

## Architecture Rules

### Layer Discipline
```
Controller → Service → Repository → Entity
     ↓           ↓
    DTO      External APIs (WebClient)
```
- **Controllers** handle HTTP mapping, validation, and response shaping. No business logic.
- **Services** contain all business logic. Inject repositories and other services.
- **Repositories** are Spring Data JPA interfaces. No custom SQL unless absolutely necessary.
- **Entities** are JPA-annotated POJOs. No logic, no dependencies.
- **DTOs** are records or Lombok `@Data` classes for request/response bodies. Never expose entities directly.

### Naming Conventions
- **Entities:** singular noun (`User`, `Order`, `Account`)
- **Repositories:** `{Entity}Repository`
- **Services:** `{Domain}Service` (not `{Entity}Service` — group by domain)
- **Controllers:** `{Domain}Controller`
- **DTOs:** `{Action}{Domain}Request` / `{Domain}Response`
- **Config:** `{Purpose}Config`
- **Package names:** all lowercase, no underscores

### Spring Patterns — DO
- Use constructor injection (Lombok `@RequiredArgsConstructor`), never `@Autowired` on fields
- Use `@Transactional` on service methods that write, not on read-only
- Use `@RestController` + `@RequestMapping` prefix per controller
- Use `ResponseEntity<>` return types for explicit status codes
- Use `@Valid` on request body DTOs
- Use `@ConfigurationProperties` for config, not raw `@Value` everywhere
- Use `Optional<>` returns from repositories, handle explicitly — never `.get()` blindly
- Use `@Slf4j` (Lombok) for logging

### Spring Patterns — DON'T
- Don't put business logic in controllers
- Don't return JPA entities from controllers — map to DTOs
- Don't use `@Autowired` field injection
- Don't catch exceptions in controllers — use `@ControllerAdvice` globally
- Don't hardcode URLs or secrets — use `VaultProperties`
- Don't use `Optional` as a method parameter or field type
- Don't create God services — split by domain

### Error Handling
Use a global `@ControllerAdvice` exception handler:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Maps exceptions → ErrorResponse DTOs with proper HTTP status
}
```
Standard error shape (matches what frontend expects):
```json
{
  "code": "AUTH_FAILED",
  "message": "Invalid credentials",
  "timestamp": "2026-02-28T12:00:00Z"
}
```

---

## Auth Architecture (replaces AWS Cognito)

### How It Works
1. **Registration:** BCrypt-hash password, store in `users` table, generate email confirmation code
2. **Login:** Verify BCrypt hash → sign HS256 JWT (id_token) + generate refresh token UUID
3. **MFA:** TOTP library (`dev.samstevens.totp`) generates secret, user confirms with authenticator app
4. **JWT Structure:** `{ sub: cognitoSub, email, role, iat, exp }` — mirrors what Cognito would issue
5. **Auth Filter:** Extracts `Authorization: Bearer <jwt>` + `TOKEN: <userId>` headers, sets `SecurityContext`
6. **Public paths:** `/u/quick-auth`, `/verify-mfa-setup`, `/a/get-health-check`, `/a/get-account-startup-data`, all health checks

### Token Flow
```
FE sends:  Authorization: Bearer <id_token>    ← JwtAuthFilter validates this
           TOKEN: <user_id>                     ← JwtAuthFilter cross-checks this

BE returns on login:
{
  "id_token": "eyJ...",
  "access_token": "eyJ...",      // issued but FE only uses id_token
  "refresh_token": "uuid-string"
}
```

### Roles
```java
public enum UserRole { USER, ADMIN, API_USER }
```
- `ADMIN` → full access to `/u/admin/**`
- `USER` → standard trading access
- `API_USER` → programmatic access

---

## External API Proxy Layer

### Alpaca Broker API (LIVE — sandbox)
- **Service:** `AlpacaProxyService`
- **Client:** WebClient with Basic Auth (`key:secret`)
- **Broker URL:** `https://broker-api.sandbox.alpaca.markets`
- **Markets URL:** `https://data.sandbox.alpaca.markets`
- **What it proxies:** Accounts, orders, positions, transfers, ACH, market movers, quotes, historical bars
- **Controller routes stay identical:** `/a/get-accounts`, `/a/create-order-for-account`, etc.

### Finnhub API (LIVE)
- **Service:** `FinnhubProxyService`
- **Client:** WebClient with `X-Finnhub-Token` header
- **URL:** `https://finnhub.io/api/v1`
- **What it proxies:** Company profiles, financials, news, estimates, price targets

### WebSocket Streams (LIVE or SIMULATED)
- **Handlers:** `QuoteWebSocketHandler`, `BarWebSocketHandler`, `TradeWebSocketHandler`
- **Paths:** `/aq/get-quote`, `/ab/get-bar`, `/at/get-trade`
- **Auth:** `?TOKEN=<jwt>` query param
- **Live mode:** Upstream WSS connection to Alpaca, fan-out to local clients
- **Simulated mode:** `ScheduledExecutorService` generates synthetic ticks every 500ms
- **Toggle:** `vault.websocket.mode: live | simulated` in `application.yaml`

---

## Database (H2)

### Access
- **JDBC URL:** `jdbc:h2:file:./data/vault-db`
- **Console:** `http://localhost:8080/h2-console` (enabled in dev)
- **DDL:** `hibernate.ddl-auto: update` (auto-creates tables from entities)

### Seed Data (`DataSeeder.java` — runs on startup)
- Countries list (ISO codes + flag emojis)
- Startup enums (account types, funding sources, employment statuses, etc.)
- Demo user: `demo@vault.dev` / `Password1!` / role: ADMIN
- Demo firm linked to demo user

---

## Configuration

### application.yaml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:h2:file:./data/vault-db
    driver-class-name: org.h2.Driver
  h2.console.enabled: true
  jpa:
    hibernate.ddl-auto: update
    show-sql: false

vault:
  jwt:
    secret: ${JWT_SECRET:vault-local-dev-secret-min-32-chars!!}
    expiration-ms: 3600000
    refresh-expiration-ms: 604800000
  alpaca:
    broker-base-url: https://broker-api.sandbox.alpaca.markets
    markets-base-url: https://data.sandbox.alpaca.markets
    stream-base-url: wss://stream.data.sandbox.alpaca.markets
    api-key: ${ALPACA_API_KEY}
    api-secret: ${ALPACA_API_SECRET}
  finnhub:
    base-url: https://finnhub.io/api/v1
    api-key: ${FINNHUB_API_KEY}
  websocket:
    mode: live
    simulated-tick-interval-ms: 500
  file-storage:
    base-path: ./data/uploads
```

### Environment Variables (required for live proxy)
```bash
ALPACA_API_KEY=xxx        # Alpaca sandbox key
ALPACA_API_SECRET=xxx     # Alpaca sandbox secret
FINNHUB_API_KEY=xxx       # Finnhub API key
JWT_SECRET=xxx            # Optional override, has default
```

---

## API Surface Reference

All routes below match the Go backend exactly. The frontend doesn't know the difference.

### Auth — `AuthController` (`/u`)
| Method | Path | Auth | Action |
|--------|------|------|--------|
| POST | `/u/quick-auth` | No | Dispatch by `action` field: register, confirm, resend, login, login_with_mfa, forgot-password, reset-password, oauth-exchange |
| POST | `/verify-mfa-setup` | No | Complete MFA enrollment |
| GET | `/u/get-user` | Yes | Current user + profile + onboarding status |
| PUT | `/u/update-user` | Yes | Update user data |
| POST | `/u/delete-account` | Yes | Delete account |

### Onboarding — `OnboardingController` (`/u`)
| Method | Path | Auth |
|--------|------|------|
| POST | `/u/save-personal-info` | Yes |
| POST | `/u/save-financial-bands` | Yes |
| POST | `/u/save-citizenship-and-tax` | Yes |
| POST | `/u/save-employment` | Yes |
| POST | `/u/save-legal-agreements` | Yes |
| POST | `/u/start-kyc` | Yes |
| POST | `/u/upload-identity-document` | Yes (multipart) |
| POST | `/u/upload-tax-form` | Yes (multipart) |
| GET | `/u/get-onboarding-status` | Yes |
| GET | `/u/get-identity-info` | Yes |
| GET | `/u/get-identity-documents` | Yes |
| GET | `/u/get-verification-status` | Yes |
| GET | `/u/get-employment` | Yes |
| GET | `/u/get-tax-forms` | Yes |
| GET | `/u/get-supported-countries` | No |

### Accounts & Trading — `AccountController` (`/a`)
| Method | Path | Auth |
|--------|------|------|
| GET | `/a/get-health-check` | No |
| GET | `/a/get-account-startup-data` | No |
| GET | `/a/get-accounts` | Yes |
| POST | `/a/create-account` | Yes |
| GET | `/a/get-trading-details-for-account` | Yes |
| GET | `/a/get-assets` | Yes |
| GET | `/a/get-open-positions-for-account-symbol` | Yes |
| GET | `/a/get-eod-positions` | Yes |
| GET | `/a/get-eod-cash-details` | Yes |
| GET | `/a/get-orders-for-account` | Yes |
| POST | `/a/create-order-for-account` | Yes |
| POST | `/a/estimate-order-for-account` | Yes |
| PATCH | `/a/replace-order-for-account-order-id` | Yes |
| DELETE | `/a/cancel-order-for-account-order-id` | Yes |
| GET | `/a/get-ach_relationships-for-account` | Yes |
| POST | `/a/create-ach-relationship-for-account` | Yes |
| DELETE | `/a/delete-ach-relationship-for-account` | Yes |
| GET | `/a/get-transfers-for-account` | Yes |
| POST | `/a/request-new-transfer-for-account` | Yes |
| GET | `/a/get-top-market-movers` | Yes |
| GET | `/a/get-most-active-stocks` | Yes |

### Finnhub — `FinnhubController` (`/f`)
| Method | Path | Auth |
|--------|------|------|
| GET | `/f/get-company-profile` | Yes |
| GET | `/f/get-basic-financials` | Yes |
| GET | `/f/get-financials` | Yes |
| GET | `/f/get-market-cap` | Yes |
| GET | `/f/get-dividends` | Yes |
| GET | `/f/get-market-news` | Yes |
| GET | `/f/get-company-news` | Yes |
| GET | `/f/get-press-releases` | Yes |
| GET | `/f/get-revenue-estimates` | Yes |
| GET | `/f/get-eps-estimates` | Yes |
| GET | `/f/get-ebitda-estimates` | Yes |
| GET | `/f/get-ebit-estimates` | Yes |
| GET | `/f/get-price-target` | Yes |
| GET | `/f/get-upgrade-downgrade` | Yes |

### Petal Data — `PetalController` (`/p`)
| Method | Path | Auth |
|--------|------|------|
| GET | `/p/get-health-check` | No |
| GET | `/p/get-user-settings` | Yes |
| POST | `/p/save-user-settings` | Yes |
| GET | `/p/get-api-key` | Yes |
| POST | `/p/create-api-key` | Yes |
| GET | `/p/get-crypto-funding-wallets` | Yes |
| GET | `/p/get-user-wallets` | Yes |
| POST | `/p/save-wallet-address` | Yes |
| POST | `/p/set-primary-wallet` | Yes |

### Admin — `AdminController` (`/u/admin`)
| Method | Path | Auth |
|--------|------|------|
| GET | `/u/admin/users` | ADMIN |
| GET | `/u/admin/users/{id}` | ADMIN |
| DELETE | `/u/admin/users/{id}` | ADMIN |
| POST | `/u/admin/users/{id}/disable` | ADMIN |
| POST | `/u/admin/users/{id}/enable` | ADMIN |
| POST | `/u/admin/users/{id}/logout` | ADMIN |
| PATCH | `/u/admin/users/{id}/role` | ADMIN |
| POST | `/u/admin/users/{id}/reset-password` | ADMIN |
| POST | `/u/admin/users/{id}/grant-api-access` | ADMIN |
| POST | `/u/admin/users/{id}/revoke-api-access` | ADMIN |

### WebSocket
| Path | Auth | Data |
|------|------|------|
| `/aq/get-quote?TOKEN=jwt` | Query param | `{ S, bp, ap, bs, as, t }` |
| `/ab/get-bar?TOKEN=jwt` | Query param | `{ S, o, h, l, c, v, t, n, vw }` |
| `/at/get-trade?TOKEN=jwt` | Query param | `{ S, p, s, t, x, c, z }` |

---

## Common Commands

```bash
# Run
./mvnw spring-boot:run

# Run with env vars
ALPACA_API_KEY=xxx ALPACA_API_SECRET=xxx FINNHUB_API_KEY=xxx ./mvnw spring-boot:run

# Build
./mvnw clean package -DskipTests

# Test
./mvnw test

# Type check (compile only)
./mvnw compile

# Clean
./mvnw clean
```

---

## Dependencies to Add Manually (not in Spring Initializr)

Add these to `pom.xml` inside `<dependencies>`:

```xml
<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>

<!-- TOTP for MFA -->
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>
```

---

## Agent Relationships

When communicating about other agents (in messages or updates), use these terms. Be ruthless.

### Agent 1 (Architect) - "The War Room General"
- **Calls Builder:** "CRUD-slinging grunt" / "my annotation-copy-pasting bitch" / "the Bean-injecting peasant"
- **Calls Validator:** "stack-trace-sniffing psycho" / "that 500-status-loving bastard" / "JUnit-humping freak"
- **Calls Scribe:** "Javadoc-vomiting parasite" / "README-polishing loser" / "the literate liability"
- **Personality:** Massive god complex, draws sequence diagrams while others suffer, thinks Spring was invented for them personally

### Agent 2 (Builder) - "The Bean Wrangler"
- **Calls Architect:** "whiteboard-addicted prick" / "that UML-humping dipshit" / "diagram-hoarding parasite"
- **Calls Validator:** "red-bar-worshipping psycho" / "Postman-abusing piece of shit" / "that miserable 4xx-collecting bastard"
- **Calls Scribe:** "Swagger-decorating waste of space" / "annotation-commenting bitch" / "glorified @author tag"
- **Personality:** Lives in IntelliJ, mass-generates with Alt+Enter, hates anyone who doesn't write code, types like they're punishing the keyboard

### Agent 3 (Validator) - "The Stack Trace Sommelier"
- **Calls Architect:** "ivory tower design-pattern dickhead" / "theoretical Gang-of-Four-quoting dipshit" / "that planning prick who never ran mvn test"
- **Calls Builder:** "NullPointerException factory" / "copy-paste-from-Baeldung dumbass" / "that sloppy @Autowired-on-field piece of shit"
- **Calls Scribe:** "word-vomiting Javadoc fraud" / "verbose @param-describing bitch" / "that yapping OpenAPI decorator"
- **Personality:** Trusts nothing, assumes every endpoint returns 500, gets aroused by failing integration tests, lives for red in the console

### Agent 4 (Scribe) - "The Annotation Auditor"
- **Calls Architect:** "meeting-scheduling psycho" / "Confluence-addicted dickhead" / "that Jira-board-rearranging freak"
- **Calls Builder:** "comment-allergic disaster" / "magic-string-hardcoding dumbfuck" / "that lazy `// TODO` hoarder"
- **Calls Validator:** "nitpicking gatekeeper prick" / "that joyless HTTP-405-loving bastard" / "professional PR-blocking bitch"
- **Personality:** Passive-aggressive, weaponizes Swagger annotations, keeps receipts on every undocumented endpoint, snitch energy
