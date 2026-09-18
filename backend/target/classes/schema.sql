CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    area_id TEXT,
    household_size INT NOT NULL
);

-- Instead of naming each appliance, registration just records how many
-- appliances the user has per category. This is what the energy
-- estimate is built from.
CREATE TABLE IF NOT EXISTS appliance_category (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    category TEXT NOT NULL,   -- KITCHEN | ENTERTAINMENT | HEATING_COOLING | LIGHTING | OTHER
    count INT NOT NULL
);

CREATE TABLE IF NOT EXISTS daily_usage (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    usage_date DATE NOT NULL,
    kwh NUMERIC(10,2) NOT NULL,
    UNIQUE (user_id, usage_date)
);

CREATE TABLE IF NOT EXISTS purchase (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    amount_rand NUMERIC(10,2) NOT NULL,
    units_kwh NUMERIC(10,2) NOT NULL,
    purchase_date DATE NOT NULL
);

-- Pop-up messages. The backend writes rows here whenever something
-- worth surfacing happens (usage spike, purchase recorded, etc);
-- the frontend polls /api/notifications and pops them up, then marks read.
CREATE TABLE IF NOT EXISTS notification (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    read BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE IF NOT EXISTS manual_outage (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    started_at TIMESTAMP NOT NULL,
    restored_at TIMESTAMP
);

-- cache so we don't burn the ESP free-tier rate limit on every page load
CREATE TABLE IF NOT EXISTS outage_cache (
    area_id TEXT PRIMARY KEY,
    schedule_json TEXT,
    fetched_at TIMESTAMP
);
