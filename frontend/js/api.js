const API_BASE = "http://localhost:7000/api";

function getUserId() {
  return localStorage.getItem("kw-user-id");
}

function setUserId(id) {
  localStorage.setItem("kw-user-id", id);
}

function authHeaders() {
  const id = getUserId();
  return id ? { "X-User-Id": id } : {};
}

async function apiGet(path) {
  const res = await fetch(`${API_BASE}${path}`, { headers: authHeaders() });
  if (!res.ok) throw new Error(`GET ${path} failed: ${res.status}`);
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error(`POST ${path} failed: ${res.status}`);
  return res.status === 204 ? null : res.json();
}

async function apiPut(path, body) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json", ...authHeaders() },
    body: JSON.stringify(body)
  });
  if (!res.ok) throw new Error(`PUT ${path} failed: ${res.status}`);
  return res.status === 204 ? null : res.json();
}

const Api = {
  register: (data) => apiPost("/auth/register", data),
  login: async (username) => {
    const user = await apiPost("/auth/login", { username });
    setUserId(user.id);
    return user;
  },
  me: () => apiGet("/users/me"),

  getCategories: () => apiGet("/appliance-categories"),
  updateCategory: (id, count) => apiPut(`/appliance-categories/${id}`, { count }),

  recordDailyUsage: (date, kwh) => apiPost("/usage/daily", { date, kwh }),
  getUsageHistory: () => apiGet("/usage/daily"),
  getUsageSummary: () => apiGet("/usage/summary"),

  recordPurchase: (amountRand, unitsKwh, date) => apiPost("/purchases", { amountRand, unitsKwh, date }),
  getPurchases: () => apiGet("/purchases"),

  getNotifications: () => apiGet("/notifications"),
  markNotificationRead: (id) => apiPut(`/notifications/${id}/read`),

  getBackupEstimate: () => apiGet("/backup/estimate"),

  getScheduledOutages: (areaId) => apiGet(`/outages/scheduled?area=${areaId}`),
  getManualOutages: () => apiGet("/outages/manual"),
  reportOutage: () => apiPost("/outages/manual", {}),
  restoreOutage: (id) => apiPut(`/outages/manual/${id}/restore`)
};
