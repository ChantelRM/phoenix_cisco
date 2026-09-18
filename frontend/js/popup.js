// Polls /api/notifications and shows each unread one as a pop-up, then
// marks it read. Call startNotificationPolling() on any page that should
// show pop-ups (dashboard, purchases, etc).
function showPopup(message) {
  const el = document.createElement("div");
  el.style.cssText = `
    position: fixed; top: 16px; left: 50%; transform: translateX(-50%);
    background: var(--burgundy); color: var(--cream);
    padding: 12px 16px; border-radius: var(--radius);
    font-size: 13px; max-width: 320px; z-index: 1000;
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  `;
  el.textContent = message;
  document.body.appendChild(el);
  setTimeout(() => el.remove(), 5000);
}

async function checkNotifications() {
  try {
    const notifications = await Api.getNotifications();
    for (const n of notifications) {
      showPopup(n.message);
      await Api.markNotificationRead(n.id);
    }
  } catch (err) {
    // silently skip - not being able to fetch notifications shouldn't
    // block the rest of the page
  }
}

function startNotificationPolling(intervalMs = 15000) {
  checkNotifications();
  setInterval(checkNotifications, intervalMs);
}
