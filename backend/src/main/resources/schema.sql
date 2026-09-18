CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    area_id TEXT,
    household_size INTEGER NOT NULL
);

-- Instead of naming each appliance, registration just records how many
-- appliances the user has per category. This is what the energy
-- estimate is built from.
CREATE TABLE IF NOT EXISTS appliance_category (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category TEXT NOT NULL,   -- KITCHEN | ENTERTAINMENT | HEATING_COOLING | LIGHTING | OTHER
    count INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS daily_usage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    usage_date TEXT NOT NULL,   -- yyyy-MM-dd
    kwh REAL NOT NULL,
    UNIQUE (user_id, usage_date)
);

CREATE TABLE IF NOT EXISTS purchase (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount_rand REAL NOT NULL,
    units_kwh REAL NOT NULL,
    purchase_date TEXT NOT NULL   -- yyyy-MM-dd
);

-- Pop-up messages. The backend writes rows here whenever something
-- worth surfacing happens (usage spike, purchase recorded, etc);
-- the frontend polls /api/notifications and pops them up, then marks read.
CREATE TABLE IF NOT EXISTS notification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS manual_outage (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    started_at TEXT NOT NULL,
    restored_at TEXT
);

-- cache so we don't burn the ESP free-tier rate limit on every page load
CREATE TABLE IF NOT EXISTS outage_cache (
    area_id TEXT PRIMARY KEY,
    schedule_json TEXT,
    fetched_at TEXT
);
