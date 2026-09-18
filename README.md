# KiloWhat

(formerly "Blackout Brain") A small energy-management app: register with a
username, area, household size, and how many appliances you have per
category → track daily usage and electricity purchases → get pop-up
notifications on usage spikes and confirmed purchases → see an estimated
backup runtime and outage schedule.

## Stack
- Frontend: plain HTML/CSS/JS, PWA-capable, mobile-responsive
- Backend: Javalin (plain, fully implemented - see docs/api.md)
- Database: PostgreSQL
- External data: EskomSePush (ESP) API for scheduled outages

## Repo layout
```
kilowhat/
├── frontend/     static site, no build step
├── backend/      Javalin app (Maven), fully implemented
├── docs/         API contract
└── docker-compose.yml
```

## Running locally
1. `docker compose up -d` (starts Postgres and loads schema.sql)
2. `cd backend && mvn compile exec:java` (set `ESP_API_KEY` env var for
   the outages feature to work - free key at sepush.co.za)
3. Open `frontend/index.html` (login) or `frontend/register.html` in a
   browser, or serve the folder with any static server

## Backend status
Every endpoint in docs/api.md is implemented with real SQL against
Postgres - nothing is a stub. The one thing worth double-checking before
a demo is `EspClient.searchAreas()`'s exact URL against ESP's current
docs, since free-tier API paths occasionally move.

## Frontend status
Kept intentionally thin this round since the team is wiring the frontend
to the API themselves - `login.html`, `register.html`, and `js/api.js`
(with the `X-User-Id` header + every endpoint above already stubbed as
functions) are there as a starting reference, not a finished UI.
