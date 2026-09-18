# API contract

Auth model: no passwords/sessions. Register once, then send the returned
`id` as an `X-User-Id` header on every other request.

## Auth
POST /api/auth/register
  { username, areaId, householdSize, applianceCategories: [{category, count}] }
  → 201 { id, username, areaId, householdSize }

POST /api/auth/login
  { username }
  → { id, username, areaId, householdSize }   (404 if no such username)

GET  /api/users/me                    (header: X-User-Id)

## Appliance categories
GET  /api/appliance-categories        (header: X-User-Id)
PUT  /api/appliance-categories/{id}   { count }

Categories: KITCHEN | ENTERTAINMENT | HEATING_COOLING | LIGHTING | OTHER

## Daily usage
POST /api/usage/daily                 { date, kwh }
GET  /api/usage/daily                 → history, most recent first
GET  /api/usage/summary               → { purchasedKwh, consumedKwh, avgPerDay, trendPercent }

## Purchases
POST /api/purchases                   { amountRand, unitsKwh, date }
GET  /api/purchases

## Notifications (pop-ups)
GET  /api/notifications                     → unread notifications
PUT  /api/notifications/{id}/read

Notifications are created automatically by the backend:
- after POST /api/usage/daily, if today's kWh is >20% above the trailing
  7-day average
- after every POST /api/purchases

## Backup / runtime estimate
GET  /api/backup/estimate             → { estimatedLoadWatts, runtimeMinutes }
  Built from the user's appliance categories + household size - see
  BackupCalculatorService for the per-category wattage assumptions.

## Outages
GET  /api/outages/scheduled?area={areaId}   cache-backed ESP call
GET  /api/outages/manual
POST /api/outages/manual
PUT  /api/outages/manual/{id}/restore
