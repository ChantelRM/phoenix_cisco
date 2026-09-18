# Blackout Brain

**Track:** Smart & Sustainable Futures — Energy Optimization & Waste Management
**Event:** She Builds Tomorrow Hackathon (WeThinkCode_ x Cisco)

## The Problem

South African households — regardless of income — waste a meaningful share of their limited or prepaid electricity because they have no visibility into which appliances or habits are draining their credit. This leads to unexpected outages and avoidable cost, and existing tools only show national loadshedding schedules, not personalized household consumption behavior.

## What We're Building

An app that:
1. Tracks a household's remaining electricity credit and estimated usage rate
2. Warns users before they run out, so they can top up proactively instead of arriving home to no power
3. Surfaces personalized, location-specific tips for reducing electricity waste (climate and appliance-aware)

## Onboarding Flow

User provides: email, area/location, household size, list of appliances owned.

## Data Model

```
User
  - email
  - area
  - household_size

Appliance
  - name
  - wattage (from static reference table)
  - hours_used_per_day

Purchase
  - amount_rand
  - units_kwh
  - date
```
## Repo layout
```
kilowhat/
├── frontend/     static site, no build step
├── backend/      Javalin app (Maven), fully implemented
├── docs/         API contract
└── docker-compose.yml
```
## Core Logic

```
daily_usage_kwh = sum(appliance.wattage * appliance.hours_used_per_day) / 1000
days_remaining = remaining_units_kwh / daily_usage_kwh

IF days_remaining <= threshold:
    trigger low_credit_alert

tip = lookup_tip(user.area_climate_zone, current_season, top_usage_appliance)
```
## Running locally
1. `cd backend && mvn compile exec:java` (set `ESP_API_KEY` env var for
   the outages feature to work - free key at sepush.co.za). No separate
   database server or password needed - the backend runs on embedded
   SQLite (`backend/kilowhat.db`, created and migrated automatically on
   first run). The old `docker compose up -d` / Postgres step is no
   longer required.
2. Open `frontend/splash.html` (loading splash → routes to login or
   dashboard automatically) — or jump straight to `frontend/index.html`
   (login) / `frontend/register.html` — in a browser, or serve the
   folder with any static server

## Team & Roles

| Person | Role | Responsibilities |
|---|---|---|
| Paris Nyoni| Frontend | Onboarding screens (email, area, household size, appliance picker) |
| Kgodiso Lebese | Frontend | Dashboard (remaining credit, days-left estimate) + tip/alert pop-ups |
| Chantel Reabetswe Muthaphuli | Backend — Data & Logic Engine | Data model/database setup (User, Appliance, Purchase); core calculation logic (daily usage rate, days-remaining estimate, low-credit trigger) |
| Yanele Bhengu | Backend — API & Content | REST endpoints (onboarding, purchases, dashboard, tip-matching); appliance wattage reference table and area/climate tip library |
| Vanessa Botsime | Full-stack / Integration | Wires frontend to backend per `integration/api-contract.md`; tests the end-to-end flow; handles deployment/demo setup; drafts and coordinates the 3-slide pitch |

See `integration/` for the API contract, the React fetch layer (`api.js`), an example wired component (`Dashboard.jsx`), and a backend CORS setup (`CorsConfig.java`).

## Deliverables Checklist

- [ ] Data model finalized
- [ ] Logic flow diagram (calculation + tip-matching)
- [ ] Wireframe (onboarding, dashboard, alert screens)
- [ ] Appliance wattage reference table
- [ ] Area/climate tip library
- [ ] 3-slide pitch (The Gap / The AI Solution / The Tomorrow Factor)
- [ ] (Stretch) Working demo

## Tech Stack

- **Frontend:** React
- **Backend:** Java (Javalin)
- **Database:** _TBD — pick something quick to set up, e.g. SQLite or an in-memory store, given the time limit_

## Pitch Deck

3-slide pitch: [Blackout Brain — Pitch](https://claude.ai/artifact/Dc2ZatXV6PVRacg7pkoNNH)