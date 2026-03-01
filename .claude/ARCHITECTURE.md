# Vault — Java Spring Boot Mock Backend

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           VAULT SPRING BOOT MOCK API                            │
│                          localhost:8080 (single JAR)                             │
└─────────────────────────────────────────────────────────────────────────────────┘

 ┌──────────────────────────────────────────────────────────────────────────────┐
 │                          GATEWAY / FILTER CHAIN                              │
 │                                                                              │
 │  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  ┌───────────────────┐   │
 │  │ CORS Filter │→ │ JWT Auth     │→ │ Role-Based │→ │ Request Logger /  │   │
 │  │ (allow *)   │  │ Filter       │  │ Access     │  │ Metrics Filter    │   │
 │  │             │  │              │  │ (RBAC)     │  │                   │   │
 │  │ Passes all  │  │ Parses:      │  │            │  │ Logs method, path │   │
 │  │ origins in  │  │ Authorization│  │ @PreAuth   │  │ duration, status  │   │
 │  │ dev mode    │  │ + TOKEN hdrs │  │ checks on  │  │                   │   │
 │  └─────────────┘  │              │  │ /admin/**  │  └───────────────────┘   │
 │                    │ Skips:       │  └────────────┘                          │
 │                    │ /u/quick-auth│                                          │
 │                    │ /a/get-*-    │                                          │
 │                    │   startup-*  │                                          │
 │                    │ /*/health-*  │                                          │
 │                    └──────────────┘                                          │
 └──────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                             REST CONTROLLERS
═══════════════════════════════════════════════════════════════════════════════════

 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                                                                             │
 │  ┌─────────────────────┐   AUTH CONTROLLER  ── /u/quick-auth               │
 │  │  AuthController      │                                                   │
 │  │  prefix: /u          │   Unified auth dispatch by "action" field:        │
 │  │                      │                                                   │
 │  │  POST /u/quick-auth  │   ┌──────────────┬───────────────────────────┐   │
 │  │  POST /verify-mfa-   │   │ action       │ behavior                  │   │
 │  │       setup          │   ├──────────────┼───────────────────────────┤   │
 │  │  GET  /u/get-user    │   │ register     │ create user + send code   │   │
 │  │  PUT  /u/update-user │   │ confirm      │ verify email code         │   │
 │  │  POST /u/delete-     │   │ resend       │ resend confirmation       │   │
 │  │       account        │   │ login        │ password auth → tokens    │   │
 │  │  GET  /u/get-user-   │   │ login_with_  │ TOTP verify → tokens      │   │
 │  │       profile        │   │   mfa        │                           │   │
 │  │  GET  /u/get-user-   │   │ forgot-      │ send reset code           │   │
 │  │       contact-info   │   │   password   │                           │   │
 │  │  GET  /u/get-user-   │   │ reset-       │ confirm new password      │   │
 │  │       broker-info    │   │   password   │                           │   │
 │  │  GET  /u/get-user-   │   │ oauth-       │ exchange code → tokens    │   │
 │  │       account-summary│   │   exchange   │                           │   │
 │  │                      │   └──────────────┴───────────────────────────┘   │
 │  │  GET  /u/get-        │                                                   │
 │  │   onboarding-status  │   Tokens: self-signed HS256 JWTs                  │
 │  │                      │   id_token  = { sub, email, role, exp }           │
 │  │  GET  /u/get-        │   access_token (unused by FE, issued anyway)      │
 │  │   supported-countries│   refresh_token = opaque UUID                     │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │                                                                             │
 │  ┌─────────────────────┐   ONBOARDING CONTROLLER                           │
 │  │  OnboardingController│                                                   │
 │  │  prefix: /u          │   POST /u/save-personal-info                     │
 │  │                      │   POST /u/save-financial-bands                    │
 │  │                      │   POST /u/save-citizenship-and-tax               │
 │  │                      │   POST /u/save-employment                        │
 │  │                      │   POST /u/save-legal-agreements                  │
 │  │                      │   POST /u/start-kyc                              │
 │  │                      │   POST /u/upload-identity-document  (multipart)  │
 │  │                      │   POST /u/upload-tax-form           (multipart)  │
 │  │                      │   GET  /u/get-identity-info                      │
 │  │                      │   GET  /u/get-identity-documents                 │
 │  │                      │   GET  /u/get-verification-status                │
 │  │                      │   GET  /u/get-employment                         │
 │  │                      │   GET  /u/get-tax-forms                          │
 │  │                      │   GET  /u/download-tax-form                      │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │                                                                             │
 │  ┌─────────────────────┐   ACCOUNT CONTROLLER  ── Alpaca proxy             │
 │  │  AccountController   │                                                   │
 │  │  prefix: /a          │   GET  /a/get-health-check                       │
 │  │                      │   GET  /a/get-account-startup-data  (no auth)    │
 │  │                      │   GET  /a/get-accounts                           │
 │  │                      │   POST /a/create-account                         │
 │  │                      │   GET  /a/get-trading-details-for-account        │
 │  │                      │   GET  /a/get-assets                             │
 │  │                      │                                                   │
 │  │                      │   ── Positions ──                                 │
 │  │                      │   GET  /a/get-open-positions-for-account-symbol   │
 │  │                      │   GET  /a/get-eod-positions                      │
 │  │                      │   GET  /a/get-eod-cash-details                   │
 │  │                      │                                                   │
 │  │                      │   ── Orders ──                                    │
 │  │                      │   GET  /a/get-orders-for-account                 │
 │  │                      │   POST /a/create-order-for-account               │
 │  │                      │   POST /a/estimate-order-for-account             │
 │  │                      │   PATCH /a/replace-order-for-account-order-id    │
 │  │                      │   DELETE /a/cancel-order-for-account-order-id    │
 │  │                      │                                                   │
 │  │                      │   ── ACH / Transfers ──                           │
 │  │                      │   GET  /a/get-ach_relationships-for-account      │
 │  │                      │   POST /a/create-ach-relationship-for-account    │
 │  │                      │   DELETE /a/delete-ach-relationship-for-account  │
 │  │                      │   GET  /a/get-transfers-for-account              │
 │  │                      │   POST /a/request-new-transfer-for-account       │
 │  │                      │                                                   │
 │  │                      │   ── Market Screeners ──                          │
 │  │                      │   GET  /a/get-top-market-movers                  │
 │  │                      │   GET  /a/get-most-active-stocks                 │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │                                                                             │
 │  ┌─────────────────────┐   FINNHUB CONTROLLER  ── Finnhub proxy            │
 │  │  FinnhubController   │                                                   │
 │  │  prefix: /f          │   GET  /f/get-company-profile                    │
 │  │                      │   GET  /f/get-basic-financials                   │
 │  │                      │   GET  /f/get-financials                         │
 │  │                      │   GET  /f/get-market-cap                         │
 │  │                      │   GET  /f/get-dividends                          │
 │  │                      │   GET  /f/get-market-news                        │
 │  │                      │   GET  /f/get-company-news                       │
 │  │                      │   GET  /f/get-press-releases                     │
 │  │                      │   GET  /f/get-revenue-estimates                  │
 │  │                      │   GET  /f/get-eps-estimates                      │
 │  │                      │   GET  /f/get-ebitda-estimates                   │
 │  │                      │   GET  /f/get-ebit-estimates                     │
 │  │                      │   GET  /f/get-price-target                       │
 │  │                      │   GET  /f/get-upgrade-downgrade                  │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │                                                                             │
 │  ┌─────────────────────┐   PETAL DATA CONTROLLER                           │
 │  │  PetalController     │                                                   │
 │  │  prefix: /p          │   GET  /p/get-health-check                       │
 │  │                      │   GET  /p/get-user-settings                      │
 │  │                      │   POST /p/save-user-settings                     │
 │  │                      │   GET  /p/get-api-key                            │
 │  │                      │   POST /p/create-api-key                         │
 │  │                      │   GET  /p/get-crypto-funding-wallets             │
 │  │                      │   GET  /p/get-crypto-funding-transfers-for-acct  │
 │  │                      │   GET  /p/get-user-wallets                       │
 │  │                      │   POST /p/save-wallet-address                    │
 │  │                      │   POST /p/set-primary-wallet                     │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 ├─────────────────────────────────────────────────────────────────────────────┤
 │                                                                             │
 │  ┌─────────────────────┐   ADMIN CONTROLLER                                │
 │  │  AdminController     │                                                   │
 │  │  prefix: /u/admin    │   GET    /u/admin/users                          │
 │  │                      │   GET    /u/admin/users/{id}                     │
 │  │  @PreAuthorize       │   DELETE /u/admin/users/{id}                     │
 │  │  ("ADMIN")           │   POST   /u/admin/users/{id}/disable            │
 │  │                      │   POST   /u/admin/users/{id}/enable             │
 │  │                      │   POST   /u/admin/users/{id}/logout             │
 │  │                      │   PATCH  /u/admin/users/{id}/role               │
 │  │                      │   POST   /u/admin/users/{id}/reset-password     │
 │  │                      │   POST   /u/admin/users/{id}/grant-api-access   │
 │  │                      │   POST   /u/admin/users/{id}/revoke-api-access  │
 │  └─────────────────────┘                                                    │
 │                                                                             │
 └─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                             WEBSOCKET HANDLERS
═══════════════════════════════════════════════════════════════════════════════════

 ┌─────────────────────────────────────────────────────────────────────────────┐
 │  Spring WebSocket (STOMP not needed — raw TextWebSocketHandler)             │
 │                                                                             │
 │  ┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐       │
 │  │  /aq/get-quote     │  │  /ab/get-bar       │  │  /at/get-trade     │      │
 │  │                    │  │                    │  │                    │       │
 │  │  Auth: ?TOKEN=jwt  │  │  Auth: ?TOKEN=jwt  │  │  Auth: ?TOKEN=jwt  │      │
 │  │                    │  │                    │  │                    │       │
 │  │  Client sends:     │  │  Client sends:     │  │  Client sends:     │      │
 │  │  { action:         │  │  { action:         │  │  { action:         │      │
 │  │    "subscribe",    │  │    "subscribe",    │  │    "subscribe",    │      │
 │  │    asset: "AAPL" } │  │    asset: "AAPL" } │  │    asset: "AAPL" } │      │
 │  │                    │  │                    │  │                    │       │
 │  │  Server pushes:    │  │  Server pushes:    │  │  Server pushes:    │      │
 │  │  { S, bp, ap,      │  │  { S, o, h, l, c,  │  │  { S, p, s, t,     │      │
 │  │    bs, as, t }     │  │    v, t, n, vw }   │  │    x, c, z }       │      │
 │  └───────────────────┘  └───────────────────┘  └───────────────────┘       │
 │                                                                             │
 │  DATA SOURCE STRATEGY (per mode):                                           │
 │                                                                             │
 │  Mode A — LIVE PROXY (default):                                             │
 │    Spring connects upstream to Alpaca WSS feeds using API keys.             │
 │    Fans out to subscribed local clients. Real market data.                  │
 │                                                                             │
 │  Mode B — SIMULATED:                                                        │
 │    ScheduledExecutorService generates synthetic ticks every 500ms.          │
 │    Random walk around last known price. No external dependency.             │
 │    Good for weekends / after hours / offline dev.                           │
 │                                                                             │
 │  Toggle via: application.yml → petal.websocket.mode: live | simulated      │
 └─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                               SERVICE LAYER
═══════════════════════════════════════════════════════════════════════════════════

 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                                                                             │
 │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐  │
 │  │  AuthService       │  │  UserService       │  │  OnboardingService       │  │
 │  │                    │  │                    │  │                          │  │
 │  │  - register()      │  │  - getUser()       │  │  - savePersonalInfo()    │  │
 │  │  - confirmEmail()  │  │  - updateUser()    │  │  - saveFinancialBands()  │  │
 │  │  - login()         │  │  - deleteUser()    │  │  - saveCitizenshipTax()  │  │
 │  │  - verifyMfa()     │  │  - getProfile()    │  │  - saveEmployment()      │  │
 │  │  - refreshToken()  │  │  - getContactInfo()│  │  - saveLegalAgreements() │  │
 │  │  - forgotPassword()│  │  - getBrokerInfo() │  │  - startKyc()            │  │
 │  │  - resetPassword() │  │  - getAcctSummary()│  │  - getOnboardingStatus() │  │
 │  │  - generateJwt()   │  │  - listUsers()     │  │  - uploadDocument()      │  │
 │  │  - validateJwt()   │  │  - disableUser()   │  │  - uploadTaxForm()       │  │
 │  │                    │  │  - enableUser()    │  │  - getVerificationStatus()│ │
 │  └──────────────────┘  │  - updateRole()    │  └──────────────────────────┘  │
 │                         └──────────────────┘                                │
 │                                                                             │
 │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────────────┐  │
 │  │  AlpacaProxy       │  │  FinnhubProxy      │  │  MarketDataService       │  │
 │  │  Service           │  │  Service            │  │                          │  │
 │  │                    │  │                    │  │  - WebSocket fan-out      │  │
 │  │  WebClient calls   │  │  WebClient calls   │  │  - Subscription mgmt     │  │
 │  │  to real Alpaca    │  │  to real Finnhub   │  │  - Simulated tick gen    │  │
 │  │  Broker API        │  │  API               │  │  - Client session map    │  │
 │  │                    │  │                    │  │                          │  │
 │  │  Base URLs from    │  │  API key from      │  │  Upstream Alpaca WSS     │  │
 │  │  application.yml   │  │  application.yml   │  │  connection pool         │  │
 │  │                    │  │                    │  │                          │  │
 │  │  Auth: Basic       │  │  Auth: Header      │  │                          │  │
 │  │  (key:secret)      │  │  X-Finnhub-Token   │  │                          │  │
 │  └──────────────────┘  └──────────────────┘  └──────────────────────────┘  │
 │                                                                             │
 │  ┌──────────────────┐  ┌──────────────────┐                                │
 │  │  WalletService     │  │  FileStorage       │                                │
 │  │                    │  │  Service            │                                │
 │  │  - getWallets()    │  │                    │                                │
 │  │  - saveAddress()   │  │  Local disk store  │                                │
 │  │  - setPrimary()    │  │  instead of S3     │                                │
 │  │  - cryptoTransfers │  │                    │                                │
 │  │                    │  │  ./data/uploads/   │                                │
 │  │  (mock or Privy    │  │    identity-docs/  │                                │
 │  │   proxy, config)   │  │    tax-forms/      │                                │
 │  └──────────────────┘  │    avatars/        │                                │
 │                         └──────────────────┘                                │
 └─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                              PERSISTENCE LAYER
═══════════════════════════════════════════════════════════════════════════════════

 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                                                                             │
 │                    H2 DATABASE  (embedded, file-backed)                      │
 │                    jdbc:h2:file:./data/vault-db                              │
 │                    Console: http://localhost:8080/h2-console                 │
 │                                                                             │
 │  Spring Data JPA + Hibernate auto-DDL                                       │
 │                                                                             │
 │  ┌─────────────────────────────────────────────────────────────────────┐    │
 │  │                          JPA ENTITIES                               │    │
 │  │                                                                     │    │
 │  │  ── Identity & Auth ──────────────────────────────────────────────  │    │
 │  │  users              (id, cognito_sub, email, password_hash,         │    │
 │  │                      role, status, mfa_secret, mfa_enabled,         │    │
 │  │                      onboarding_status, created_at, updated_at)     │    │
 │  │  refresh_tokens     (token, user_id, expires_at)                    │    │
 │  │                                                                     │    │
 │  │  ── Profile & Onboarding ─────────────────────────────────────────  │    │
 │  │  user_profiles      (user_id FK, first_name, last_name, dob,       │    │
 │  │                      phone, nationality, tax_id_encrypted)          │    │
 │  │  user_addresses     (user_id FK, street, city, state, zip,         │    │
 │  │                      country)                                       │    │
 │  │  user_financial     (user_id FK, annual_income_min/max,            │    │
 │  │                      liquid_net_worth_min/max, funding_sources)     │    │
 │  │  user_employment    (user_id FK, status, employer, position,       │    │
 │  │                      address)                                       │    │
 │  │  user_identity      (user_id FK, id_type, id_number_encrypted,     │    │
 │  │                      country_code, verification_status)             │    │
 │  │  user_documents     (user_id FK, doc_type, file_path, mime,        │    │
 │  │                      uploaded_at)                                   │    │
 │  │  user_agreements    (user_id FK, agreement_type, signed_at,        │    │
 │  │                      ip_address, revision)                          │    │
 │  │  user_tax_forms     (user_id FK, form_type, file_path,            │    │
 │  │                      uploaded_at)                                   │    │
 │  │                                                                     │    │
 │  │  ── Accounts & Trading ───────────────────────────────────────────  │    │
 │  │  accounts           (id, account_number, status, currency,         │    │
 │  │                      buying_power, cash, portfolio_value,           │    │
 │  │                      enabled_assets, created_at)                    │    │
 │  │  account_memberships (user_id FK, account_id FK, role, status)     │    │
 │  │  trading_accounts   (user_id FK, alpaca_account_id, acct_number,   │    │
 │  │                      status)                                        │    │
 │  │                                                                     │    │
 │  │  ── Firm & KYB ──────────────────────────────────────────────────  │    │
 │  │  firms              (id, name, kyb_status, created_at)             │    │
 │  │  firm_beneficial_owners  (firm_id FK, name, ownership_pct)         │    │
 │  │  firm_authorized_persons (firm_id FK, name, role)                  │    │
 │  │  firm_documents     (firm_id FK, doc_type, file_path)              │    │
 │  │  firm_agreements    (firm_id FK, agreement_type, signed_at)        │    │
 │  │                                                                     │    │
 │  │  ── Trading Data (cached from Alpaca) ────────────────────────────  │    │
 │  │  positions          (account_id, symbol, qty, market_value,        │    │
 │  │                      unrealized_pl, cost_basis, timestamp)          │    │
 │  │  orders             (account_id, order_id, symbol, qty, side,      │    │
 │  │                      type, status, filled_qty, filled_avg_price,    │    │
 │  │                      created_at)                                    │    │
 │  │  assets             (symbol, name, exchange, tradable,             │    │
 │  │                      fractionable, status)                          │    │
 │  │                                                                     │    │
 │  │  ── Reference & Settings ─────────────────────────────────────────  │    │
 │  │  countries          (iso3, iso2, name, flag_emoji)                  │    │
 │  │  user_settings      (user_id FK, setting_name, setting_value)      │    │
 │  │  user_api_keys      (user_id FK, api_key, created_at)              │    │
 │  │  user_wallets       (user_id FK, address, chain, is_primary)       │    │
 │  │                                                                     │    │
 │  └─────────────────────────────────────────────────────────────────────┘    │
 │                                                                             │
 │  data.sql → Seeds: countries, startup enums, 1 demo user, 1 demo firm      │
 │                                                                             │
 └─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                          EXTERNAL INTEGRATIONS
═══════════════════════════════════════════════════════════════════════════════════

 ┌─────────────────────────────────────────────────────────────────────────────┐
 │                                                                             │
 │              LIVE (passthrough to real APIs using your keys)                 │
 │                                                                             │
 │  ┌──────────────────────────────────────────────────────────────────────┐   │
 │  │                                                                      │   │
 │  │   ┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐   │   │
 │  │   │ ALPACA      │     │ FINNHUB     │     │ ALPACA WSS          │   │   │
 │  │   │ Broker API  │     │ REST API    │     │ Market Streams      │   │   │
 │  │   │             │     │             │     │                     │   │   │
 │  │   │ Sandbox:    │     │ finnhub.io/ │     │ stream.data.        │   │   │
 │  │   │ broker-api. │     │ api/v1      │     │   sandbox.alpaca.   │   │   │
 │  │   │ sandbox.    │     │             │     │   markets            │   │   │
 │  │   │ alpaca.     │     │ Header:     │     │                     │   │   │
 │  │   │ markets     │     │ X-Finnhub-  │     │ Quotes, Bars,       │   │   │
 │  │   │             │     │ Token       │     │ Trades streams      │   │   │
 │  │   │ BasicAuth:  │     │             │     │                     │   │   │
 │  │   │ key:secret  │     │             │     │ BasicAuth:          │   │   │
 │  │   └──────┬──────┘     └──────┬──────┘     │ key:secret          │   │   │
 │  │          │                   │             └──────────┬──────────┘   │   │
 │  │          ▼                   ▼                        ▼              │   │
 │  │   AlpacaProxyService  FinnhubProxyService   MarketDataService       │   │
 │  │   (WebClient)         (WebClient)           (WebSocketClient)       │   │
 │  │                                                                      │   │
 │  └──────────────────────────────────────────────────────────────────────┘   │
 │                                                                             │
 │              MOCKED (no external calls needed)                              │
 │                                                                             │
 │  ┌──────────────────────────────────────────────────────────────────────┐   │
 │  │                                                                      │   │
 │  │   ┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐   │   │
 │  │   │ COGNITO     │     │ S3          │     │ PRIVY               │   │   │
 │  │   │ → replaced  │     │ → replaced  │     │ → replaced with     │   │   │
 │  │   │   by local  │     │   by local  │     │   local wallet      │   │   │
 │  │   │   AuthSvc   │     │   file disk │     │   table in H2       │   │   │
 │  │   │   + BCrypt   │     │   storage   │     │                     │   │   │
 │  │   │   + TOTP     │     │   ./data/   │     │                     │   │   │
 │  │   │   + HS256    │     │   uploads/  │     │                     │   │   │
 │  │   │   JWTs       │     │             │     │                     │   │   │
 │  │   └─────────────┘     └─────────────┘     └─────────────────────┘   │   │
 │  │                                                                      │   │
 │  └──────────────────────────────────────────────────────────────────────┘   │
 │                                                                             │
 └─────────────────────────────────────────────────────────────────────────────┘


═══════════════════════════════════════════════════════════════════════════════════
                           PROJECT STRUCTURE
═══════════════════════════════════════════════════════════════════════════════════

  mock-api/
  ├── pom.xml                            (or build.gradle.kts)
  ├── application.yml
  ├── data/                              (git-ignored, runtime)
  │   ├── vault-db.mv.db                 H2 file
  │   └── uploads/
  │       ├── identity-docs/
  │       ├── tax-forms/
  │       └── avatars/
  │
  └── src/main/java/com/vault/
      │
      ├── VaultApplication.java           @SpringBootApplication
      │
      ├── config/
      │   ├── SecurityConfig.java        SecurityFilterChain, CORS, public paths
      │   ├── WebSocketConfig.java       Register WS handlers + TOKEN interceptor
      │   ├── WebClientConfig.java       Alpaca & Finnhub WebClient beans
      │   └── AppProperties.java         @ConfigurationProperties("petal")
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
      │   ├── FirmBeneficialOwner.java
      │   ├── FirmAuthorizedPerson.java
      │   ├── Order.java
      │   ├── Position.java
      │   ├── Asset.java
      │   ├── Country.java
      │   ├── UserSetting.java
      │   ├── UserApiKey.java
      │   ├── UserWallet.java
      │   └── RefreshToken.java
      │
      ├── repository/                    Spring Data JPA interfaces
      │   ├── UserRepository.java
      │   ├── UserProfileRepository.java
      │   ├── AccountRepository.java
      │   ├── OrderRepository.java
      │   ├── ... (one per entity)
      │   └── RefreshTokenRepository.java
      │
      ├── dto/                           Request/response DTOs
      │   ├── QuickAuthRequest.java
      │   ├── QuickAuthResponse.java
      │   ├── CreateOrderRequest.java
      │   ├── OrderEstimationResponse.java
      │   ├── ... (mirrors frontend types)
      │   └── ErrorResponse.java
      │
      ├── websocket/
      │   ├── QuoteWebSocketHandler.java    /aq/get-quote
      │   ├── BarWebSocketHandler.java      /ab/get-bar
      │   ├── TradeWebSocketHandler.java    /at/get-trade
      │   └── WsTokenInterceptor.java       Extracts ?TOKEN= param
      │
      └── seed/
          └── DataSeeder.java            CommandLineRunner: countries, enums,
                                         demo user (demo@petal.com / Password1!)


═══════════════════════════════════════════════════════════════════════════════════
                          CONFIGURATION (application.yml)
═══════════════════════════════════════════════════════════════════════════════════

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
      secret: ${JWT_SECRET:local-dev-secret-min-32-chars-long!!}
      expiration-ms: 3600000          # 1 hour
      refresh-expiration-ms: 604800000 # 7 days

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
      mode: live                       # live | simulated
      simulated-tick-interval-ms: 500

    file-storage:
      base-path: ./data/uploads


═══════════════════════════════════════════════════════════════════════════════════
                         OPTIMIZATION DECISIONS
═══════════════════════════════════════════════════════════════════════════════════

  WHAT THE GO BACKEND HAS             WHAT THE SPRING BOOT MOCK DOES
  ─────────────────────────────────    ─────────────────────────────────────
  6 separate microservices             1 monolith JAR (single port)
  (cognito, petal, alpaca,             Controllers map to same route prefixes
   finnhub, privy, wallet)             (/u, /a, /f, /p) so FE needs zero changes

  AWS Cognito for auth                 Local auth: BCrypt passwords + TOTP
                                       library (dev.samstevens.totp) + HS256
                                       self-signed JWTs. No AWS dependency.

  AWS S3 for documents                 Local file storage (./data/uploads/)
                                       FileStorageService returns paths that
                                       serve via static resource handler.

  AWS Secrets Manager for config       application.yml + env vars.
                                       Spring profiles for overrides.

  MySQL via GORM                       H2 embedded (file-backed).
                                       Zero install, persists between restarts.
                                       H2 console at /h2-console for debugging.

  Casbin RBAC                          Spring Security @PreAuthorize with
                                       simple role enum (USER, ADMIN, API_USER).
                                       Enough for mock. No Casbin dep needed.

  Privy Web3 wallets                   Mocked in H2 table. No Privy calls.
                                       Optional: add Privy keys to proxy real.

  Alpaca Broker API                    LIVE passthrough via WebClient.
                                       Real orders, real positions, real data.
                                       Sandbox keys = no real money.

  Finnhub API                          LIVE passthrough via WebClient.
                                       Real financial data, news, estimates.

  7 WebSocket servers on               3 WS handlers in same process.
  separate ports                       Live mode: upstream Alpaca WSS proxy.
                                       Simulated mode: synthetic tick generator.

  Go shared SDK (restapi framework)    Spring Boot auto-config does it all.
                                       No custom framework needed.

  Restart loop on crash                Spring Boot actuator + devtools.
                                       Auto-restart on code change.


═══════════════════════════════════════════════════════════════════════════════════
                        REQUEST FLOW (example: place order)
═══════════════════════════════════════════════════════════════════════════════════

  React FE                  Spring Boot Mock API                 Alpaca Sandbox
  ────────                  ────────────────────                 ──────────────
      │                           │                                    │
      │  POST /a/create-order-    │                                    │
      │  for-account?account=X    │                                    │
      │  Authorization: Bearer jwt│                                    │
      │  TOKEN: user_id           │                                    │
      │  Body: { symbol, qty,     │                                    │
      │         side, type, tif } │                                    │
      │ ─────────────────────────>│                                    │
      │                           │                                    │
      │                    JwtAuthFilter                                │
      │                    validates jwt ──┐                            │
      │                                    │                            │
      │                    AccountController                            │
      │                    .createOrder() ─┘                            │
      │                           │                                    │
      │                    AlpacaProxyService                           │
      │                    .postOrder(account, body)                    │
      │                           │                                    │
      │                           │  POST /v1/trading/accounts/X/orders│
      │                           │  Authorization: Basic key:secret   │
      │                           │  Body: { symbol, qty, side, ... }  │
      │                           │ ──────────────────────────────────>│
      │                           │                                    │
      │                           │  200 { id, status: "accepted" }    │
      │                           │ <──────────────────────────────────│
      │                           │                                    │
      │  200 { data: Order }      │                                    │
      │ <─────────────────────────│                                    │
      │                           │                                    │


═══════════════════════════════════════════════════════════════════════════════════
                            DEPENDENCIES (pom.xml)
═══════════════════════════════════════════════════════════════════════════════════

  spring-boot-starter-web              HTTP server + Jackson
  spring-boot-starter-websocket        WebSocket support
  spring-boot-starter-data-jpa         Hibernate + Spring Data
  spring-boot-starter-security         Auth filters, @PreAuthorize
  spring-boot-starter-validation       @Valid, @NotBlank, etc.
  spring-boot-starter-webflux          WebClient for Alpaca/Finnhub calls
  spring-boot-devtools                 Auto-restart in dev
  h2                                   Embedded database
  jjwt-api + jjwt-impl + jjwt-jackson HS256 JWT signing/verification
  totp (dev.samstevens:totp:1.7.1)     TOTP MFA generation/verification
  lombok                               @Data, @Builder, @Slf4j
  springdoc-openapi-starter-webmvc-ui  Swagger UI at /swagger-ui.html
```

## How to run

```bash
cd mock-api
# Set your keys (or put in .env file)
export ALPACA_API_KEY=your-sandbox-key
export ALPACA_API_SECRET=your-sandbox-secret
export FINNHUB_API_KEY=your-finnhub-key

./mvnw spring-boot:run
# or: ./gradlew bootRun
```

Frontend `.env` stays the same — Vite proxy routes to `localhost:8080`.

## What changes in the frontend?

**Nothing.** Every route prefix (`/u`, `/a`, `/f`, `/p`), every query param name, every header (`Authorization`, `TOKEN`) is preserved exactly. The Spring Boot mock is a drop-in replacement.