# FixNow — Project Context

Read this first. It gives an AI assistant (or a new teammate) everything needed to work on FixNow without re-reading the whole codebase.

## 1. What it is

**FixNow** is a Spring Boot server-rendered web app that works as a home-repair marketplace. Customers request an emergency repair (plumbing, electricity, AC, locksmith, appliance, general). The system matches or assigns a technician with the right skill, the technician accepts and completes the job, and the customer reviews it afterwards. There is also an admin dispatcher role and a Gemini-powered AI support widget for customers.

It is a university project (Java / Spring Boot coursework and software engineering project), labelled **v1.0-STABLE demo release**. Priorities are a working demo, clean layering and passing tests, not production hardening.

## 2. Tech stack

| Area | Choice |
|---|---|
| Language / build | Java 17, Maven (`mvnw` / `mvnw.cmd`) |
| Framework | Spring Boot **3.5.5** (`pom.xml`) |
| Web / views | Spring MVC + Thymeleaf (server-side rendering), plain HTML/CSS/JS |
| Persistence | Spring Data JPA / Hibernate |
| DB | H2, **file-backed** at `./data/fixnow` (dev and normal runs); in-memory `create-drop` for tests |
| Security | Spring Security with a custom **session-based** filter (form login disabled) |
| Validation | `spring-boot-starter-validation` (Jakarta annotations) |
| AI | Google Gemini REST API via Spring `RestClient` |
| Tests | JUnit 5 + MockMvc (`spring-boot-starter-test`) |

Notes:
- `mysql-connector-j` is in `pom.xml` but nothing uses it yet (future PostgreSQL/MySQL migration is on the roadmap).
- `HELP.md` links to Spring Boot **4.0.6** docs. That is boilerplate and does not match the actual 3.5.5 parent.
- Base package: `com.fix.fixnow`. Main class: `FixnowApplication`.

## 3. Running it

```bash
# Windows
.\mvnw.cmd spring-boot:run
# macOS / Linux
./mvnw spring-boot:run

# tests
./mvnw test
```

- App: `http://localhost:8080`. H2 console: `/h2-console` (JDBC URL `jdbc:h2:file:./data/fixnow`, user `sa`, empty password).
- AI assistant (optional): set the env var `GEMINI_API_KEY` before starting. `GEMINI_MODEL` overrides the default model (`gemini-3.8-flash`). Without a key the app runs normally and the assistant returns a 503 "not configured" error. **Never commit the key or put it in frontend code.**
- `data/` is git-ignored. It holds the persistent DB (`fixnow.mv.db`), so back it up if you want to keep accounts. Schema is managed with `ddl-auto=update`.

### Seeded demo accounts (`config/DataSeeder.java`, created on startup if missing)

| Role | Email | Password |
|---|---|---|
| ADMIN | `admin@fixnow.com` | `admin123` |
| CUSTOMER | `customer@fixnow.com` | `customer123` |
| TECHNICIAN (Plumbing) | `tech.plumber@fixnow.com` | `tech123` |
| TECHNICIAN (Electricity) | `tech.electric@fixnow.com` | `tech123` |

These are demo-only credentials. Change or remove them before any real deployment.

## 3a. Repository layout

```
src/main/java/com/fix/fixnow
├── config/        SecurityConfig, DataSeeder, WebConfig
├── controller/    Auth, Customer, Technician, Admin, Chat, CustomerAssistant,
│                  ErrorPage, GlobalModelAttributes
├── dto/           LoginDTO, RegisterDTO, UserDTO, ServiceRequestDTO, ReviewDTO,
│                  MessageDTO, MessageResponseDTO, RequestTimelineStep
├── exception/     GlobalExceptionHandler, BadRequestException, ResourceNotFoundException
├── model/         User, Technician, ServiceRequest, Review, Message, Role (enum)
├── repository/    UserRepo, TechnicianRepo, ServiceRequestRepo, ReviewRepo, MessageRepo
├── security/      SessionAuthenticationFilter, SessionAuthConstants
├── service/       AuthService, CustomerService, TechnicianService, DispatchService,
│                  TechnicianMatchingService, RequestTimelineService, ChatService,
│                  GeminiAssistantService
└── FixnowApplication.java

src/main/resources
├── application.properties        (default: file H2, ddl-auto=update, Gemini config)
├── application-dev.properties    (same DB, dev profile)
├── templates/                    login, register, customerDashboard, technicianDashboard,
│                                 adminDashboard, createRequest, requestDetails, addReview,
│                                 error/{403,404,500}, layout/{topbar-core,nav-bridge,base-footer}
└── static/{css/fixnow-theme.css, js/fixnow-ui.js}

src/test/java/com/fix/fixnow
├── FixnowApplicationTests.java              (MockMvc integration tests, ~15 scenarios)
└── service/GeminiAssistantServiceTest.java
src/test/resources/application.properties    (in-memory H2, create-drop)
```

## 4. Architecture

Strict layering: **Controller → Service → Repository → DB → View**. Controllers handle HTTP and session access. Business rules live in services. DTOs keep entities out of the view/API where they are used.

Caveat: some controllers (`CustomerController`, `AdminController`) inject repositories directly for simple reads and counts, so the "controllers never touch repos" claim in the README is not strictly true.

### Domain model

- **User** — `id, name, email (unique), password (BCrypt, @JsonIgnore), phone, role` (`Role` enum: `CUSTOMER | TECHNICIAN | ADMIN`).
- **Technician** — `id, name, skill, description, rating (0–5, recomputed from reviews), available (default true)`; 1:1 with a `User` via `user_id` (unique). The FK constraint is deliberately disabled (`NO_CONSTRAINT`, with a TODO) because of legacy rows.
- **ServiceRequest** — `id, description, category, location, urgency, status, user (customer), technician (nullable)`.
  - `status` is a **String**, not an enum. Constants live on the class: `PENDING, ASSIGNED, ACCEPTED, COMPLETED`, enforced with `@Pattern`. A TODO notes a future enum migration.
- **Review** — `rating (1–5), comment (≤255), user, technician, request`. **One review per request.**
- **Message** — `content, timestamp (epoch millis, Long), sender, receiver, request`.

### Request lifecycle (the core business logic)

```
PENDING ──(auto-match on create, or admin assign)──▶ ASSIGNED ──(technician accepts)──▶ ACCEPTED ──(technician completes)──▶ COMPLETED ──▶ customer review
   └────────────(technician self-claims a pending request)──────────────────────────────▶ ACCEPTED
```

- **Create** (`CustomerService.createRequest`): saved as `PENDING`. If an available technician matches the category, it is immediately set to `ASSIGNED` and that technician is marked `available=false`.
- **Admin assign** (`DispatchService.assignTechnician`): only for `PENDING` requests with no technician. The technician must be available and skill-matched. Sets `ASSIGNED` and `available=false`.
- **Accept** (`TechnicianService.acceptRequest`): an `ASSIGNED` request can only be accepted by its assigned technician. A `PENDING` one can be self-claimed if the technician is available and skill-matched. Result: `ACCEPTED`.
- **Complete** (`TechnicianService.completeRequest`): only `ACCEPTED`, only by the assigned technician. Sets `COMPLETED` and frees the technician (`available=true`).
- **Availability toggle**: a technician cannot set themselves available while holding `ASSIGNED`/`ACCEPTED` jobs.
- **Review** (`CustomerService.addReview`): request must be `COMPLETED`, belong to the reviewing customer, have a technician, and have no existing review. It then recomputes the technician's average rating.

### Skill matching (`TechnicianMatchingService`)

Both a technician's `skill` and a request's `category` go through `canonicalSkill()`: lowercased and normalised, then keyword-mapped to one of `plumbing`, `electricity`, `ac repair`, `locksmith`, `appliance repair`, `general maintenance`. Unknown values fall through as the normalised string. Matching means canonical equality. `findBestAvailableMatch` simply takes the **first** available match (no rating or distance ranking).

### Timeline (`RequestTimelineService`)

Builds four display steps (PENDING → ASSIGNED → ACCEPTED → COMPLETED) with `current` and `done` flags, plus "next step" title and description text for `requestDetails.html`. It is derived from the current status only; there is no stored history table despite the README calling it an "audit trail".

### Chat (`ChatService`, `/api/chat`)

- `POST /api/chat/send` and `GET /api/chat/{requestId}`.
- Only the request's customer and its assigned technician (via `technician.user`) can read or write. The sender comes from the **session**, never from the client.
- Returns `MessageResponseDTO`. Messages are ordered by timestamp.
- Backend only: no template calls these endpoints yet. The README's "real-time / polling" claim is not implemented in the UI.

### AI assistant (`GeminiAssistantService`, `CustomerAssistantController`)

- `POST /api/customer/assistant/chat` (customer-only). The widget is in `customerDashboard.html`, driven by `fixnow-ui.js`.
- Calls `generativelanguage.googleapis.com/v1beta/models/{model}:generateContent` server-side, with the key as a query param. Temperature 0.4, max 600 output tokens.
- Context sent: the user's message, the last 12 conversation turns (stored in the HTTP session), and up to 20 most recent request IDs and statuses. A system prompt tells it to stay honest about being an AI, not handle disputes, refunds or account actions, and not give step-by-step instructions for dangerous work.
- Rate limit: 10 messages per user per minute (in-memory map, `AiAssistantRateLimitException`, HTTP 429). Missing key or upstream failure gives `AiAssistantUnavailableException`, HTTP 503.
- Input limit: 2000 characters (`@Size`).

## 5. Security model

- **Login** (`POST /login`): `AuthService.login` looks up by email and checks BCrypt. On success the controller stores `AUTH_USER_ID`, `AUTH_NAME`, `AUTH_EMAIL`, `AUTH_ROLE` in the `HttpSession` (`SessionAuthConstants`) and redirects by role.
- **`SessionAuthenticationFilter`** runs before `UsernamePasswordAuthenticationFilter`. If the session has email and role, it builds a `UsernamePasswordAuthenticationToken` with authority `ROLE_<role>`.
- **`SecurityConfig` route rules**:
  - Public: `/`, `/login`, `/register`, `/access-denied`, `/error`, `/css/**`, `/js/**`, `/h2-console/**`.
  - `/admin/**` and `/api/admin/**` need ADMIN.
  - `/customer/**` and `/api/customer/**` need CUSTOMER.
  - `/technician/**` and `/api/technicians/**` need TECHNICIAN.
  - Everything else needs authentication (this covers `/api/chat/**`).
- Unauthenticated or forbidden requests get a **JSON** 401/403 for `/api/**` and a **redirect** to `/login` or `/access-denied` for pages.
- **Registration** (`POST /register`): public roles are CUSTOMER and TECHNICIAN only. ADMIN is rejected. A technician must supply `skill` and `description`, and a `Technician` profile is created (`AuthService.ensureTechnicianProfile`, default skill "General Maintenance"). Admins exist only through seeding.
- **Ownership checks** are done manually in controllers and services (customer owns request, technician owns job, chat participants). Keep doing this for any new endpoint.
- **Known demo-level shortcuts** (do not "fix" silently, but be aware):
  - **CSRF is disabled**.
  - H2 console is enabled and `permitAll` and frames are same-origin.
  - `spring.jpa.show-sql=true` and Spring Security DEBUG logging are on.
  - `UserRepo.findByEmailAndPassword` exists but is **unusable and should not be called**, because passwords are BCrypt-hashed. Use `findByEmail` plus `passwordEncoder.matches`.
  - `/api/auth/**` and `/api/technicians/**` appear in `SecurityConfig`, but **no controllers exist** for them (leftovers).

## 6. HTTP surface

| Method + path | Role | Purpose |
|---|---|---|
| GET `/`, `/login`, `/register` | public | Pages (redirect to the role dashboard if already signed in) |
| POST `/register`, `/login`, `/logout` | public / any | Auth actions |
| GET `/customer/dashboard` | CUSTOMER | Requests, counts, AI widget |
| GET/POST `/customer/request/new` | CUSTOMER | Create request form / submit |
| GET `/customer/request/{id}` | CUSTOMER (owner) | Details and timeline |
| GET/POST `/customer/review/new` | CUSTOMER | Review form / submit |
| GET `/technician/dashboard` | TECHNICIAN | Available and assigned jobs, availability |
| POST `/technician/availability` | TECHNICIAN | Toggle availability |
| POST `/technician/request/{id}/accept` and `/complete` | TECHNICIAN | Job transitions |
| GET `/admin/dashboard` | ADMIN | Counts, pending requests, matching technicians |
| POST `/admin/request/{requestId}/assign/{technicianId}` | ADMIN | Manual dispatch |
| POST `/api/chat/send`, GET `/api/chat/{requestId}` | authenticated participants | Per-request chat (JSON) |
| POST `/api/customer/assistant/chat` | CUSTOMER | Gemini assistant (JSON) |

Redirect `/customer/request/details` to `/customer/dashboard` lives in `WebConfig`.

**Error handling:** `GlobalExceptionHandler` (`@RestControllerAdvice`) returns a JSON `{status, message, ...}` body for `ResourceNotFound` (404), `BadRequest` (400), `AccessDenied` (403), `ResponseStatusException`, validation errors (400), and a catch-all 500 ("Something went wrong"). `ErrorPageController` renders the `error/403|404|500` templates. Whitelabel error pages are off and error details are hidden.

## 7. Frontend

- Thymeleaf pages share fragments from `templates/layout/` (`topbarCore(activePage)`, `navBridge(title, subtitle, currentLabel)`, `baseFooter`).
- `fixnow-ui.js` (~40 KB, vanilla JS) handles the **light/dark theme** and the **English/Arabic language toggle** (client-side translation dictionary `translationsAr`, plus RTL), both saved in `localStorage` (`fixnow-theme`, `fixnow-language`). It also drives the AI assistant widget via `fetch`.
- `fixnow-theme.css` (~28 KB) holds all styling.
- **Any new user-visible string must also be added to `translationsAr` in `fixnow-ui.js`**, otherwise the Arabic mode will show English.
- `GlobalModelAttributes` (`@ControllerAdvice`) supplies shared model attributes to templates.

## 8. Testing

`FixnowApplicationTests` (MockMvc, full context, in-memory H2, DB cleaned before each test) covers:
- context load
- assistant is customer-only and reports a missing key
- password hashing and login
- public registration rejects ADMIN
- register page forms
- technician profile creation
- protected pages redirect and API returns JSON auth errors
- dashboards render for each role
- create and view request
- auto-match by skill
- technician accept and complete
- admin assignment only to available technicians
- skill-match enforcement
- availability toggle blocked by active jobs
- review rules (completed only, one per request)
- chat session sender, DTO shape and non-participant blocking

`GeminiAssistantServiceTest` covers the assistant service. Run `./mvnw test` after any change to lifecycle, security or matching rules, and add a MockMvc test for new behaviour in the same style.

## 9. Conventions and gotchas for contributors

- Use **constructor injection**, and keep the Controller → Service → Repo layering. New business rules go in services, not controllers.
- Status values are Strings, so always use the `ServiceRequest.PENDING/ASSIGNED/ACCEPTED/COMPLETED` constants, never literals.
- Services mostly throw plain `RuntimeException` with user-facing text. Some controllers surface `ex.getMessage()` (e.g. admin assign, technician availability), others show a generic message. Prefer `BadRequestException` / `ResourceNotFoundException` for new code.
- Technician availability is tied to job state. Any change to assign, accept or complete must keep `available` consistent, or technicians get stuck "busy".
- Current user id is read from the session (`AUTH_USER_ID`, handled as `Long` or `Number`). Each controller has its own small `currentUserId` helper, so new code could extract a shared one.
- The repo has a `.git` history (branch `main`). A previous commit message mentions a removed context file, so this file is its replacement.
- Code comments are occasionally in Egyptian Arabic written in Latin letters (Franco-Arab). This is normal in this repo.
- `target/`, `.idea/`, `.vscode/` and `data/` are not source. Do not edit them.

## 10. Known gaps and roadmap

- Chat has no UI and no real-time transport (README plans WebSockets).
- No payments (Stripe planned) and no cloud or PostgreSQL profile.
- Request `status` should become an enum.
- Technician matching ignores rating, location and workload.
- Technician-to-user FK is not enforced at the DB level.
- Timeline has no persisted history.
- CSRF protection and tightening of dev-only settings are needed before any real deployment.
- Related coursework (outside this repo): UML and requirements documents, and a Flutter version of FixNow for the mobile apps course.
