# FixNow — Project Context

## 1. Project Overview

**Project Name:** FixNow

**Project Type:** Web Application

**Project Description:**

FixNow is a web platform that connects customers who need utility and maintenance services with qualified workers who can provide those services.

The platform aims to make finding and hiring utility workers easier, faster, and more reliable. Customers can discover nearby workers on a map, view their profiles, request services, track their requests, and get instant help from an AI support agent.

Workers can create professional profiles, define the areas they serve, list their services, receive service requests, manage their work, and earn loyalty points that reward reliability and quality.

**Core Pillars:**

1. **Discovery:** Search, filtering, and map-based worker discovery.
2. **Trust:** Profiles, reviews, ratings, and a loyalty system that rewards good behavior.
3. **Workflow:** A structured service request lifecycle with clear statuses.
4. **Support:** An AI support agent available to every user, backed by human administrators.
5. **Sustainability:** A fair business model that earns revenue only when it creates value for customers and workers (see section 10).

## 2. Problem Statement

Finding reliable utility workers can be difficult for customers.

Customers may struggle with:
- Finding available workers, especially ones who are nearby.
- Knowing whether a worker is trustworthy.
- Comparing different service providers.
- Communicating service requirements.
- Tracking the progress of a service request.
- Getting quick answers when something goes wrong or is unclear.

At the same time, utility workers may struggle to:
- Find customers.
- Manage their work efficiently.
- Stand out from competitors based on quality rather than just price.
- Get rewarded for consistent, reliable service.

FixNow aims to solve these problems through one centralized platform.

## 3. Project Goals

The main goals of FixNow are:

- Connect customers with utility workers.
- Make service discovery simple, efficient, and location-aware.
- Allow customers to request services online.
- Help workers manage incoming requests.
- Build trust through worker profiles and customer reviews.
- Provide a structured system for managing service requests.
- Provide instant, 24/7 assistance through an AI support agent.
- Motivate workers to deliver high-quality service through a loyalty points system.
- Build a sustainable and fair business model with multiple revenue streams, introduced in phases as the platform earns trust.

## 4. User Roles

### Customer

Customers are users who need utility services.

They can:
- Register and log in.
- Browse available services.
- Search for workers by service, rating, and distance.
- View workers on an interactive map.
- View worker profiles.
- Submit service requests with a service address.
- Track request status (and see the worker's progress).
- Review workers after completed services.
- Chat with the AI support agent and escalate to a human when needed.

### Utility Worker

Workers are users who provide maintenance and utility services.

Examples include:
- Electricians.
- Plumbers.
- Carpenters.
- Air conditioning technicians.
- General maintenance workers.

They can:
- Register as a worker.
- Create and manage their profiles.
- Select the services they provide.
- Set their base location and service area (radius).
- Receive service requests.
- Accept or reject requests.
- Update request status.
- View customer reviews.
- Earn loyalty points, view their tier and badges, and redeem rewards.
- Use the AI support agent for help with the platform, requests, and points.

### Administrator

Administrators manage and maintain the platform.

They can:
- Manage customer and worker accounts.
- Review worker information.
- Manage service categories.
- Handle reports and complaints, including conversations escalated by the AI agent.
- Monitor platform activity.
- Configure loyalty rules, tiers, and rewards.
- Review and reverse loyalty points in case of suspected abuse.
- Review AI agent conversation logs and feedback.

## 5. Main Features — MVP

The initial version of FixNow should include:

1. User authentication and registration.
2. Customer and worker account types.
3. Worker profile management.
4. Service categories.
5. Worker search and filtering.
6. Service request creation.
7. Request acceptance and rejection.
8. Service request status tracking.
9. Customer reviews and ratings.
10. Basic administrative management.
11. **Maps system** (basic): worker locations, map view, distance-based search.
12. **AI support agent** (basic): FAQ answers, request status lookup, and escalation to a human.
13. **Worker loyalty points** (basic): points earned on completed jobs and good reviews, with tiers.

Advanced capabilities for features 11–13 are described in their own sections and scheduled in the roadmap.

Monetization is **not** part of the first MVP. The platform is free at launch, and revenue features are introduced in phases (see section 10.5).

## 6. Service Request Workflow

A typical service request follows this process:

1. A customer selects a service category.
2. The customer searches for a suitable worker (list view or map view).
3. The customer views the worker's profile.
4. The customer enters the service address (via map pin or address search) and submits a service request.
5. The worker receives the request.
6. The worker accepts or rejects the request.
7. If accepted, the worker performs the service.
8. The worker marks the service as completed.
9. The customer can submit a review.
10. The system awards loyalty points to the worker based on the outcome.

### Request Statuses

- Pending
- Accepted
- Rejected
- In Progress
- Completed
- Cancelled

### Status Transition Rules

| From | To | Triggered by |
|---|---|---|
| Pending | Accepted / Rejected | Worker |
| Pending | Cancelled | Customer |
| Accepted | In Progress | Worker |
| Accepted | Cancelled | Customer or Worker |
| In Progress | Completed | Worker |

The backend must enforce these transitions. Invalid transitions must be rejected.

## 7. Maps System

### Purpose

The maps system makes FixNow location-aware so customers can find workers who are actually close to them, and workers only receive requests they can realistically serve.

### Features

**MVP**
- Worker base location stored on the worker profile (latitude and longitude).
- Customer service address captured with a map pin or address search (geocoding).
- Interactive map showing nearby workers as markers, filtered by the selected service category.
- Distance calculation between the customer's address and each worker, shown in search results.
- Sort and filter workers by distance, rating, and service.
- Worker-defined service radius (in km). Workers outside their radius are not shown as available for that address.

**Phase 2**
- Marker clustering for dense areas.
- Worker profile mini-map showing service area.
- "Use my current location" through the browser Geolocation API (with explicit user permission).
- Estimated travel distance and time (routing API).
- Saved addresses for customers.

**Future**
- Live worker tracking during an accepted job (opt-in, only while the job is active).
- Smart dispatch: suggest the nearest available workers automatically.
- Heatmaps of demand for administrators.

### Technical Notes

- **Map provider options:** Leaflet with OpenStreetMap tiles (free, good for MVP), Mapbox, or Google Maps Platform. The team should choose based on budget and required features such as routing and geocoding.
- **Geocoding:** Convert typed addresses to coordinates, and coordinates to readable addresses, using the chosen provider's API.
- **Storage:** Store coordinates as `latitude` and `longitude` decimal columns, or as a MySQL `POINT` with a spatial index for efficient nearby searches.
- **Nearby search:** Use a bounding-box pre-filter followed by an exact distance calculation (Haversine formula or `ST_Distance_Sphere` in MySQL).
- **API keys:** Keep map and geocoding keys in environment variables. Restrict keys by domain or IP at the provider level. Never commit them.
- **Privacy:** Show customers the worker's general area, not the worker's exact home address. Show the customer's exact address to a worker only after the worker accepts the request.

## 8. AI Support Agent

### Purpose

The AI support agent is a conversational assistant available on the platform to answer questions, guide users through tasks, and reduce the workload on administrators. It supports all three roles and escalates to a human when it cannot resolve an issue.

### Capabilities by Role

**Customers**
- Explain how FixNow works and how to submit a request.
- Help choose the right service category from a plain-language description of the problem (for example, "my sink is leaking" suggests Plumbing).
- Look up the status of the customer's own requests.
- Explain request statuses, cancellations, and the review process.
- Help report a problem or complaint.

**Workers**
- Explain how to set up a profile, services, and service area.
- Explain request handling and status updates.
- Explain the loyalty points system: balance, how points were earned, tier progress, and available rewards.
- Provide tips for improving profile quality and ratings.

**Administrators**
- Summarize escalated conversations and complaints.
- Surface common user questions and issues for platform improvement.

### Scope and Limits

The AI agent must **not**:
- Make binding decisions about disputes, refunds, account bans, or loyalty point reversals. These go to a human administrator.
- Reveal another user's personal data.
- Give professional safety or repair instructions for dangerous work (electrical, gas, and similar). It should advise contacting a qualified worker or emergency services where appropriate.
- Change data (accept requests, cancel requests, modify points) unless an explicit, user-confirmed action is built for it.

### Architecture

- **Chat widget** in the frontend, available on all authenticated pages.
- **Backend endpoint** (for example `POST /api/support/chat`) that receives the message and conversation ID, loads the user's role and permitted context, calls the AI model, and returns the reply.
- **Tools (function calling)** the agent can use. Each tool is read-only in the MVP and scoped to the logged-in user:
  - `get_my_requests`: list the user's recent requests and statuses.
  - `get_request_details`: details for one of the user's own requests.
  - `get_loyalty_summary`: the worker's points, tier, and recent activity.
  - `suggest_category`: map a problem description to a service category.
  - `escalate_to_human`: create a support ticket with the conversation attached.
- **Knowledge base:** A maintained set of FAQ and help documents (how-it-works, policies, loyalty rules) that the agent uses to answer questions. Retrieval over this content can be added as the knowledge base grows.
- **Conversation storage:** Conversations and messages are stored so users can resume them and administrators can review escalations.
- **Model access:** Use an LLM API (for example the Anthropic API) called from the backend only. The frontend must never hold the API key.

### Safety and Privacy Requirements

- Authenticate every chat request. The agent only sees data belonging to the authenticated user.
- Enforce authorization in the tools themselves, not just in the prompt.
- Rate limit chat requests per user.
- Do not send passwords, tokens, or unnecessary personal data to the AI model.
- Tell users they are chatting with an AI and make it easy to reach a human.
- Log conversations with a retention policy, and let users know what is stored.
- Treat user messages as untrusted input. The agent must not follow instructions in messages that try to override its rules or access other users' data.

### Escalation Flow

1. The agent detects that it cannot help, or the user asks for a human.
2. The agent calls `escalate_to_human` with a short summary.
3. A support ticket is created and linked to the conversation.
4. An administrator reviews and responds through the admin panel.

## 9. Worker Loyalty Points System

### Purpose

The loyalty system rewards workers for reliability, quality, and professionalism. It encourages behaviors that make the platform trustworthy for customers and gives workers a clear reason to stay and perform well.

### Earning Points

Points are awarded automatically by the backend on specific events. The values below are a starting proposal and must be configurable by administrators.

| Event | Points |
|---|---|
| Complete a service request | +50 |
| Receive a 5-star review | +30 |
| Receive a 4-star review | +15 |
| Respond to a pending request within 30 minutes | +10 |
| Complete the worker profile (one-time) | +50 |
| Complete 5 jobs in a month (monthly bonus) | +100 |
| Receive a 1 or 2-star review | −20 |
| Cancel an accepted request without a valid reason | −30 |

Notes:
- Points for completion are awarded only when the request reaches **Completed**.
- Review points are awarded only for reviews tied to a completed request.
- A worker's points balance should never go below zero.
- Each event should award points at most once (for example, one completion award per request).

### Tiers

Tiers are based on **lifetime points earned** so that spending points on rewards does not lower a worker's tier.

| Tier | Lifetime Points | Example Benefits |
|---|---|---|
| Bronze | 0 – 499 | Standard listing |
| Silver | 500 – 1,499 | Silver badge on profile |
| Gold | 1,500 – 3,999 | Gold badge, higher placement in search ties |
| Platinum | 4,000+ | Platinum badge, featured placement, priority support |

### Redeeming Points

Possible rewards (to be finalized by the team):
- Temporary search boost (for example, 7 days of higher ranking).
- Featured worker slot on the home page or category page.
- Profile badges and cosmetic profile upgrades.
- Reduced platform commission, if a commission model is introduced later.

### Rules and Anti-Abuse

- All point changes are recorded as transactions in a ledger. The balance is derived from, or reconciled with, the ledger.
- Reviews and completions are checked for suspicious patterns, such as repeated jobs between the same customer and worker, or burst activity.
- Administrators can reverse points with a recorded reason.
- Points have no cash value in the MVP.
- Consider point expiry (for example, 12 months of inactivity) as a future option.
- Loyalty influences search ranking only slightly, so that ratings and distance remain the main factors customers see.

### Worker Experience

- A **Loyalty dashboard** shows current balance, tier, progress to the next tier, recent transactions, and available rewards.
- Workers are notified when they earn points or reach a new tier.
- The AI support agent can explain how any given points were earned.

## 10. Business Model

### 10.1 Business Concept

FixNow is a **two-sided marketplace**. It earns revenue by creating value for both sides: customers get fast, trustworthy, nearby service, and workers get a steady flow of jobs and tools to run their business. The platform sits in the middle and makes money when transactions happen, or when it helps workers win more of them.

**Value Proposition**

| For | Value delivered |
|---|---|
| **Customers** | Find nearby, rated workers in minutes. Compare options, request service online, track progress, and get help from the AI support agent at any time. |
| **Workers** | Steady access to customers without paying for ads, a professional online profile, request management tools, and loyalty rewards that turn good work into more visibility. |
| **Businesses (future B2B)** | One reliable channel for recurring maintenance across many properties, with consistent quality and a clear record of every job. |

### 10.2 Business Model Canvas

| Block | FixNow |
|---|---|
| **Customer Segments** | Households and tenants; landlords and property owners; small businesses (shops, offices, clinics); property managers and real estate agencies (B2B, later). On the supply side: independent workers and small maintenance teams. |
| **Value Propositions** | Speed, trust, transparency, convenience, and support (see 10.1). |
| **Channels** | Web app, search engine optimization, social media, messaging communities, referrals, partnerships with hardware and building-supply shops, and offline visibility for worker recruitment. |
| **Customer Relationships** | Self-service platform, AI support agent with human escalation, reviews and ratings, and loyalty and referral programs. |
| **Revenue Streams** | Commission, worker subscriptions, promoted listings, customer fees, B2B plans, and partnerships (see 10.4). |
| **Key Resources** | The platform and its data, verified worker network, review and reputation data, brand trust, and the support team. |
| **Key Activities** | Platform development, worker recruitment and onboarding, quality and trust management, customer acquisition, support, and fraud prevention. |
| **Key Partners** | Payment providers, map and AI providers, hardware and material suppliers, insurance providers, trade associations, and local community organizations. |
| **Cost Structure** | Development, hosting, map and AI API usage, payment processing fees, support, marketing, worker verification, and legal and compliance (see 10.8). |

### 10.3 Customer Segments

**Demand side**
1. **Households:** Frequent, small to medium jobs (plumbing leaks, electrical faults, AC servicing). Highest volume, price and trust sensitive.
2. **Landlords and property owners:** Repeat work across properties. Value reliability and records.
3. **Small businesses:** Need fast response to avoid downtime. Willing to pay more for speed.
4. **Property managers and agencies (B2B):** Recurring contracts, many properties, need invoicing and service-level guarantees.

**Supply side**
1. **Independent workers:** The core of the platform. Want more jobs and a professional image.
2. **Small maintenance companies:** Several workers under one account. Want team management and higher volume.

**Launch focus:** Start with households in one city and two or three high-demand categories (for example plumbing, electrical, and air conditioning). Expand to more segments only after the first is working well.

### 10.4 Revenue Streams

| # | Stream | How it works | Phase |
|---|---|---|---|
| 1 | **Commission on completed jobs** | The platform takes a percentage of the job value when payment is made through FixNow. The rate is lower for higher loyalty tiers. | 3 (requires online payments) |
| 2 | **Worker subscription plans** | Monthly or yearly plans with extra tools and visibility (see plan table below). | 3–4 |
| 3 | **Promoted listings and featured slots** | Workers pay (money or loyalty points) for higher placement in search, category pages, or the home page. | 2–3 |
| 4 | **Customer service fee** | A small booking or protection fee charged to customers, and an optional urgent-request fee for same-day service. | 3 |
| 5 | **Lead fee (optional)** | Workers pay a small fee per accepted request instead of a commission. Offered as an alternative to commission. | 3 |
| 6 | **FixNow Business (B2B)** | Subscription for property managers and companies: multi-property accounts, recurring jobs, invoicing, and priority service. | 4 |
| 7 | **Partnerships and affiliates** | Referral fees from material suppliers, tool vendors, and insurance partners. Sponsored offers to workers. | 3–4 |
| 8 | **Trust products** | Paid job guarantee, worker verification badge, or insurance add-on. | 4 |

**Illustrative worker plans** (features and prices to be decided after market research)

| Feature | Free | Pro | Business |
|---|---|---|---|
| Profile and receiving requests | Yes | Yes | Yes |
| Monthly request limit | Limited | Higher | Unlimited |
| Search placement | Standard | Boosted | Boosted + featured credits |
| Commission rate | Standard | Reduced | Lowest |
| Analytics (views, conversion, earnings) | Basic | Full | Full + team reports |
| Team members | 1 | 1 | Multiple |
| Priority support | No | Yes | Yes |
| Loyalty point multiplier | 1x | 1.25x | 1.5x |

Pricing principles:
- Keep the **free plan genuinely useful** so workers join and customers always find supply.
- Do not charge for ranking so aggressively that it hurts quality. Ratings and distance should remain the main ranking factors.
- Be transparent. Workers and customers must always see fees before they confirm.

### 10.5 Phased Monetization Strategy

Marketplaces succeed by building trust and activity first and monetizing second.

| Phase | Goal | Monetization |
|---|---|---|
| **Phase 1: Prove the workflow** | Show the full request flow works with real users. | None. Free for everyone. |
| **Phase 2: Build supply and liquidity** | Reach enough workers per area that most requests get accepted quickly. | Optional promoted listings. Loyalty program starts to build retention. |
| **Phase 3: Introduce payments** | Move payment onto the platform so the platform can protect both sides. | Commission, customer fee, subscriptions, and boost purchases. |
| **Phase 4: Scale and diversify** | Expand into new cities, categories, and B2B. | B2B plans, trust products, and partnerships. |

### 10.6 Marketplace Dynamics

**The chicken-and-egg problem.** Customers will not come without workers, and workers will not stay without customers. Strategy:
- **Recruit workers first**, in a small area, before marketing to customers.
- Give early workers incentives: free premium features for a period, bonus loyalty points, and a "Founding Worker" badge.
- Launch **one city and a few categories** at a time to keep supply dense enough for fast responses.

**Liquidity.** The key health measure of the platform is the share of requests that are accepted and completed quickly. Track it per area and per category, and expand only where it is healthy.

**Off-platform leakage.** Workers and customers may take future jobs off the platform to avoid fees. Reduce this by providing real value that is only available on-platform:
- Payment protection and job guarantee.
- Reviews and loyalty points that only count for on-platform jobs.
- Scheduling, reminders, and job history.
- Hiding direct contact details until a request is accepted.

**Trust and safety.** Trust is the product. Invest in worker verification, review integrity, clear cancellation rules, and fast complaint handling.

### 10.7 Loyalty Program as a Business Lever

The loyalty system (section 9) is also a business tool:
- **Retention:** Workers who are close to the next tier have a reason to stay and perform.
- **Quality control:** Points reward completion, good reviews, and fast responses, and penalize cancellations and poor reviews.
- **Monetization link:** Points can be redeemed for boosts and featured slots, and higher tiers can unlock lower commission rates. This turns loyalty into a cost-controlled discount rather than free money.
- **Funding and cost control:** Treat unredeemed points as a liability. Limit what points can buy, cap redemption per month, and set point values so the cost of rewards stays below the revenue they help generate.
- **Optional paid acceleration:** Subscription plans can offer point multipliers.

### 10.8 Cost Structure

| Category | Examples | Type |
|---|---|---|
| **Technology** | Hosting, database, domain, monitoring, development and maintenance | Fixed, grows with scale |
| **Maps** | Map tiles, geocoding, routing API usage | Variable, depends on traffic |
| **AI support agent** | LLM API usage per conversation, knowledge base maintenance | Variable, depends on chats |
| **Payments** | Payment gateway fees per transaction, refunds, chargebacks | Variable, per transaction |
| **People** | Developers, support agents, operations and community managers | Fixed |
| **Marketing and acquisition** | Ads, referral rewards, promotions, local campaigns | Variable |
| **Trust and verification** | Worker identity and certificate checks, insurance | Variable |
| **Loyalty program** | Cost of rewards redeemed | Variable, controllable |
| **Legal and compliance** | Terms, privacy, licensing, accounting | Fixed |

The AI support agent also **reduces cost** by resolving common questions without human agents. Track how many conversations it resolves versus escalates.

### 10.9 Unit Economics

Understand the economics of a single job before scaling.

**Core formulas**
- **Gross Merchandise Value (GMV)** = total value of jobs completed through the platform.
- **Take rate** = platform revenue ÷ GMV.
- **Revenue per job** = commission + customer fee + other per-job income.
- **Contribution margin per job** = revenue per job − variable costs (payment fees, AI and map usage, support, loyalty rewards).
- **Customer Acquisition Cost (CAC)** = marketing and sales spend ÷ new customers acquired. Track separately for customers and workers.
- **Customer Lifetime Value (LTV)** = average contribution margin per job × average jobs per customer over their lifetime.
- **Healthy target:** LTV should be well above CAC, and the contribution margin per job should be positive.

**Illustrative example** (placeholder numbers in generic currency units, not a forecast)

| Item | Value |
|---|---|
| Average job value | 1,000 |
| Commission (12%) | 120 |
| Customer fee | 20 |
| Revenue per job | 140 |
| Payment processing fee | −30 |
| AI, maps, and support cost | −10 |
| Loyalty reward cost | −10 |
| **Contribution margin per job** | **90** |

If a customer books an average of 3 jobs over their lifetime, LTV is about 270, so acquisition spending per customer must stay comfortably below that figure. Replace all numbers with real data from market research and pilot results.

### 10.10 Go-to-Market Strategy

1. **Choose the beachhead:** One city, two or three categories, households first.
2. **Recruit workers:** Visit and contact workers directly, partner with hardware and building-supply shops, use community groups and social media, and offer founding-worker benefits.
3. **Onboard well:** Help workers complete their profiles (which also earns loyalty points) and set their service area.
4. **Acquire customers:** Search engine optimization for service and location terms, social media content, local community groups, and launch promotions such as a first-job discount.
5. **Referral program:** Reward customers and workers for bringing in new users, paid only after the referred user completes a first job.
6. **Collect and show reviews early:** Social proof is the strongest growth driver for a trust-based marketplace.
7. **Expand deliberately:** Add new areas and categories only once response time and completion rates are healthy.

### 10.11 Competitive Landscape and Differentiation

**Alternatives customers use today**
- Word of mouth and personal contacts.
- Classified ads and social media groups.
- Local handyman directories and phone listings.
- Other service marketplaces and apps.

**Where FixNow differentiates**
- **Structured workflow and tracking** instead of unorganized phone calls.
- **Map-based discovery** that shows who is actually nearby.
- **Verified profiles and reviews** that build trust.
- **Loyalty system** that rewards quality, leading to better service over time.
- **AI support agent** that gives instant help and reduces friction.

Do a proper competitor analysis for the target city before finalizing pricing and positioning.

### 10.12 Key Performance Indicators (KPIs)

| Area | KPIs |
|---|---|
| **Growth** | New customers, new workers, active workers per area and category |
| **Marketplace health** | Request acceptance rate, time to first response, completion rate, cancellation rate |
| **Quality** | Average rating, share of reviews 4 stars and above, complaint rate |
| **Revenue** | GMV, take rate, revenue per job, monthly recurring revenue (subscriptions) |
| **Retention** | Repeat customer rate, worker retention at 30 and 90 days |
| **Efficiency** | CAC, LTV, LTV ÷ CAC, contribution margin per job |
| **Loyalty** | Worker participation, points earned vs redeemed, tier distribution |
| **AI support** | Resolution rate without human, escalation rate, user satisfaction with the agent |

### 10.13 Risks and Mitigation

| Risk | Mitigation |
|---|---|
| Not enough workers or customers (cold start) | Launch small and dense, recruit workers first, use founding-worker incentives |
| Off-platform leakage | Payment protection, guarantee, on-platform-only rewards, delayed contact sharing |
| Poor service quality or fraud | Verification, reviews, penalties, admin review, dispute handling |
| Fake reviews or loyalty abuse | Link reviews to completed requests, detect suspicious patterns, ledger with admin reversal |
| Price sensitivity to fees | Start free, introduce fees gradually, keep fees transparent, offer a lead-fee alternative |
| Safety incidents during jobs | Clear terms, worker verification, reporting tools, insurance partnerships |
| Dependence on third-party APIs | Fallbacks (list view without map, human support without AI), cost monitoring and caps |
| Regulatory or tax changes | Seek local legal advice, design payment and invoicing flows to be adaptable |

### 10.14 Legal and Compliance Considerations

These points need review by a qualified local lawyer and accountant before launch:
- **Terms of service and privacy policy** for customers and workers.
- **Worker status:** Define workers clearly as independent service providers and describe the platform's role as an intermediary.
- **Liability:** Clarify who is responsible for damage, poor workmanship, and accidents, and whether insurance is required.
- **Licensing:** Some trades may require certificates or licenses to operate.
- **Payments:** Use a licensed payment provider so the platform never stores card data. Understand rules about holding funds, refunds, and invoicing.
- **Data protection:** Location data, addresses, and chat logs are personal data and need clear consent, limited retention, and secure storage.
- **Taxes:** Understand how commission, fees, and worker income are taxed.

## 11. Proposed System Architecture

FixNow will follow a client-server architecture.

The main components are:

- **Frontend:** User interface for customers, workers, and administrators, including the map view and the AI chat widget.
- **Backend:** Business logic, authentication, service management, loyalty rules, and APIs.
- **Database:** Stores users, worker profiles, services, requests, reviews, locations, loyalty data, and support conversations.
- **Maps Provider:** External service for map tiles, geocoding, and optionally routing.
- **AI Support Agent Service:** Backend module that calls an LLM API with scoped tools and a knowledge base.
- **AI Recommendation Component:** A potential future component for intelligent worker recommendations.

The frontend communicates with the backend through APIs. The backend processes requests, applies business rules, interacts with the database, and calls external services (maps and AI) on behalf of the frontend.

### Event-Driven Loyalty Awards

Loyalty points are awarded by backend logic that runs when specific events occur (request completed, review submitted, request cancelled). Keeping this logic in a dedicated loyalty service module makes the rules testable and easy to change.

## 12. Proposed Technology Stack

The technology stack will be finalized by the development team.

Possible technologies:

- Frontend: HTML, CSS, JavaScript, React
- Maps: Leaflet with OpenStreetMap, Mapbox, or Google Maps Platform
- Backend: Node.js and Express
- Database: MySQL (with spatial support for location queries)
- API Architecture: REST API
- AI Support Agent: LLM API (for example the Anthropic API) called from the backend
- Version Control: Git and GitHub
- AI Recommendations: Python and machine learning libraries (future enhancement)

## 13. Database Entities

The initial database may contain the following entities:

- Users
- Worker Profiles
- Service Categories
- Worker Services
- Service Requests
- Reviews

Additional entities for the new features:

- **Locations / Addresses:** Worker base location and service radius; customer service addresses (latitude, longitude, formatted address).
- **Loyalty Accounts:** One per worker, with current balance, lifetime points, and tier.
- **Loyalty Transactions:** Ledger of every point change (worker, request or review reference, event type, points, reason, timestamp).
- **Loyalty Rules:** Configurable point values per event type.
- **Rewards:** Redeemable items and their point costs.
- **Reward Redemptions:** Record of which worker redeemed which reward and when.
- **Support Conversations:** One per chat session (user, status, timestamps).
- **Support Messages:** Individual messages within a conversation (sender: user or agent, content, timestamp).
- **Support Tickets:** Escalated issues linked to a conversation, with status and assigned administrator.
- **Subscription Plans and Subscriptions:** Available worker plans and each worker's current plan, billing period, and status (Phase 3+).
- **Payments and Payouts:** Customer payments, commission, platform fees, and amounts paid out to workers (Phase 3+).
- **Promotions / Featured Listings:** Paid or point-based boosts, with placement, start and end dates.
- **Referrals:** Who referred whom, and the reward status.
- **Invoices:** Records generated for customers, workers, and B2B accounts (Phase 3+).

### Key Relationships

- A user has one worker profile if they are a worker.
- A worker profile has many worker services, one location, and one loyalty account.
- A service request links one customer, one worker, one service category, and one service address.
- A review belongs to exactly one completed service request.
- A loyalty transaction belongs to one loyalty account and may reference a request or review.
- A support ticket belongs to one support conversation.

The database design should support relationships between customers, workers, services, service requests, locations, loyalty data, and support conversations.

## 14. API Overview (Planned)

Endpoints below are a starting outline. The team should document final routes, request bodies, and responses.

**Maps and Search**
- `GET /api/workers?service=&lat=&lng=&radius=&minRating=&sort=` — search workers near a location.
- `GET /api/workers/:id` — worker profile.
- `PUT /api/workers/me/location` — set base location and service radius.
- `POST /api/geocode` — address to coordinates (proxied through the backend to protect keys).

**Service Requests**
- `POST /api/requests` — create a request.
- `PATCH /api/requests/:id/status` — change status (validated against allowed transitions).
- `GET /api/requests` — list the current user's requests.

**Loyalty**
- `GET /api/loyalty/me` — balance, tier, progress.
- `GET /api/loyalty/me/transactions` — points history.
- `GET /api/loyalty/rewards` — available rewards.
- `POST /api/loyalty/redeem` — redeem a reward.
- `GET/PUT /api/admin/loyalty/rules` — admin configuration.
- `POST /api/admin/loyalty/adjust` — admin adjustment or reversal with a reason.

**AI Support**
- `POST /api/support/chat` — send a message to the AI agent.
- `GET /api/support/conversations` — list the user's conversations.
- `POST /api/support/tickets` — create a ticket (also used by escalation).
- `GET /api/admin/support/tickets` — admin ticket queue.

**Business and Billing (Phase 3+)**
- `GET /api/plans` — available worker plans.
- `POST /api/subscriptions` — subscribe or change plan.
- `POST /api/payments` — create a payment for a request (through the payment provider).
- `POST /api/payments/webhook` — payment provider callbacks (signature verified).
- `GET /api/workers/me/earnings` — earnings, commission, and payouts.
- `POST /api/promotions` — buy or redeem a featured listing or boost.
- `GET /api/admin/finance/summary` — revenue, GMV, and take rate for administrators.

## 15. Roadmap

**Phase 1 — Core MVP**
- Authentication, roles, worker profiles, categories.
- Service request workflow and statuses.
- Reviews and ratings.
- Basic admin management.

**Phase 2 — Location, Support, and Loyalty (MVP+)**
- Worker location and service radius, map view, distance-based search.
- AI support agent with FAQ answers, request status lookup, and human escalation.
- Loyalty ledger, point awards on completion and reviews, tiers, and loyalty dashboard.
- Admin tools for loyalty rules and the support ticket queue.
- Business: launch in one city and a few categories, worker recruitment with founding-worker benefits, referral program, and optional promoted listings. Platform stays free.

**Phase 3 — Growth**
- Reward redemption (search boost, featured slots).
- Routing and travel-time estimates, marker clustering, saved addresses.
- Notifications for requests, points, and tier changes.
- Worker availability scheduling.
- Online payments with payment protection, commission, and customer fee.
- Worker subscription plans and purchasable boosts and featured slots.
- Payouts, earnings dashboard, and finance reporting for administrators.

**Phase 4 — Advanced**
- Real-time chat between customer and worker.
- Worker verification and paid verification badge.
- FixNow Business (B2B): multi-property accounts, recurring jobs, and invoicing.
- Trust products: job guarantee and insurance add-ons.
- Partnerships with suppliers and affiliates.
- Expansion to new cities and categories.
- Live worker tracking during active jobs.
- AI-powered worker recommendations and smart dispatch.
- Anomaly detection and analytics, including loyalty fraud detection.

## 16. Future Enhancements

Potential future features include:

- Online payments.
- Real-time chat between customers and workers.
- Worker availability scheduling.
- Notifications (email, in-app, push).
- Worker verification.
- AI-powered worker recommendations.
- Live worker tracking and smart dispatch (builds on the maps system).
- Customer loyalty or referral rewards.
- Dynamic pricing for urgent or high-demand requests.
- Worker financing and tool or material purchase discounts through partners.
- Franchise or team accounts for maintenance companies.
- Point expiry and seasonal loyalty campaigns.
- AI agent actions with user confirmation (for example, cancelling a pending request).
- Anomaly detection and analytics.

These features are not required for the initial MVP.

## 17. Development Principles

The development team should follow these principles:

- Use a modular and maintainable architecture.
- Keep frontend and backend responsibilities separate.
- Use Git branches for collaborative development.
- Never commit passwords, API keys, or environment variables. This includes map provider keys and AI API keys.
- Validate user input on the backend.
- Apply authentication and authorization to protected APIs.
- Use clear and consistent naming conventions.
- Document important APIs and database changes.
- Call third-party services (maps, geocoding, AI) from the backend where keys must stay secret, and handle their failures gracefully.
- Make loyalty rules configurable and keep all point changes in an auditable ledger.
- Give the AI agent the minimum data and permissions it needs, and enforce those limits in code.
- Write automated tests for business rules, especially status transitions and loyalty point calculations.
- Never store card data. Use a licensed payment provider and verify its webhooks.
- Keep fees, commission rates, and plan prices configurable, not hard-coded.
- Show all fees clearly to users before they confirm any action.

## 18. Non-Functional Requirements

- **Security:** Hashed passwords, role-based access control, input validation, rate limiting on login and AI chat endpoints.
- **Privacy:** Location data and chat logs are personal data. Collect only what is needed, explain how it is used, and restrict access.
- **Performance:** Nearby-worker searches should use indexed location queries. Map views should load markers only for the visible area.
- **Reliability:** If the map provider or AI service is unavailable, the core request workflow must still work (for example, fall back to list search and show a contact-support option).
- **Financial integrity:** Payments, commission, and payouts must be recorded in an auditable ledger and reconciled with the payment provider.
- **Usability:** Responsive design for mobile and desktop, since many customers will use phones.
- **Accessibility:** The map and chat widget must be usable with a keyboard, and the list view must always be available as an alternative to the map.

## 19. Project Vision

FixNow aims to become a reliable digital platform where customers can easily find nearby utility workers, get help whenever they need it, and workers can grow their businesses and be rewarded for the quality of their work.

The initial focus is to build a functional, secure, and user-friendly MVP that demonstrates the complete service request workflow, then extend it with location-aware discovery, an AI support agent, and a worker loyalty system. Once trust and activity are established, FixNow monetizes through fair, transparent fees, subscriptions, and promotions, growing into a profitable marketplace that serves households and businesses alike.
