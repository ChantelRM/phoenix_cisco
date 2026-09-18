# What's left

Backend: fully implemented. The only thing to verify before a demo is
EspClient.searchAreas()'s exact URL against ESP's current docs.

Frontend: kept thin on purpose since you're wiring it yourselves.
- register.html / index.html / dashboard.html / purchases.html /
  outages.html are working references, not a finished UI
- appliance-category editing (PUT /api/appliance-categories/{id}) has
  no page yet - only registration writes categories right now
- js/db.js (offline IndexedDB) from the old repo wasn't carried over -
  add it back once the API wiring is done, if offline support is still
  in scope
- dark theme still intentionally left for later, per earlier discussion
