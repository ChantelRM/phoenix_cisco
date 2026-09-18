const CACHE_NAME = "kilowhat-v1";

const APP_SHELL = [
  "/index.html",
  "/login.html",
  "/register.html",
  "/dashboard.html",
  "/purchases.html",
  "/outages.html",
  "/css/theme.css",
  "/js/api.js",
  "/js/popup.js"
];

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(APP_SHELL)));
});

self.addEventListener("fetch", (event) => {
  if (APP_SHELL.some((path) => event.request.url.endsWith(path))) {
    event.respondWith(caches.match(event.request).then((cached) => cached || fetch(event.request)));
  }
});
