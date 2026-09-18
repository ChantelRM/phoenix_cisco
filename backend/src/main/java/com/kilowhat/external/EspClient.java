package com.kilowhat.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Talks to the EskomSePush (ESP) API. Never call this directly from a
// controller for every page load - go through OutageService's cache
// first, or you'll burn the free-tier quota (roughly 50 calls/day).
public class EspClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey = System.getenv("ESP_API_KEY");
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode getScheduleForArea(String areaId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://developer.sepush.co.za/business/2.0/area?id=" + areaId))
            .header("Token", apiKey)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    // Used on the onboarding screen so the user can type a suburb and get
    // back matching area IDs. Verify this exact path against ESP's current
    // docs before the demo - free-tier API surfaces shift occasionally.
    public JsonNode searchAreas(String query) throws Exception {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://developer.sepush.co.za/business/2.0/areas_search?text=" + encoded))
            .header("Token", apiKey)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }
}
