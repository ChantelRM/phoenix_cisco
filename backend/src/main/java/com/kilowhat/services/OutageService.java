package com.kilowhat.services;

import com.kilowhat.db.Database;
import com.kilowhat.external.EspClient;
import com.kilowhat.models.ManualOutage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OutageService {

    private final EspClient espClient = new EspClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode getScheduleForArea(String areaId) throws Exception {
        try (Connection conn = Database.getConnection()) {
            String cached = readCache(conn, areaId);
            try {
                JsonNode fresh = espClient.getScheduleForArea(areaId);
                writeCache(conn, areaId, fresh.toString());
                return fresh;
            } catch (Exception e) {
                // ESP down or rate-limited - fall back to whatever's cached,
                // even if stale, rather than failing the whole request.
                if (cached != null) {
                    return mapper.readTree(cached);
                }
                throw e;
            }
        }
    }

    private String readCache(Connection conn, String areaId) throws SQLException {
        String sql = "SELECT schedule_json FROM outage_cache WHERE area_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, areaId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("schedule_json");
        }
        return null;
    }

    private void writeCache(Connection conn, String areaId, String json) throws SQLException {
        String sql = """
            INSERT INTO outage_cache (area_id, schedule_json, fetched_at)
            VALUES (?, ?, now())
            ON CONFLICT (area_id) DO UPDATE SET schedule_json = ?, fetched_at = now()
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, areaId);
            ps.setString(2, json);
            ps.setString(3, json);
            ps.executeUpdate();
        }
    }

    public ManualOutage report(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO manual_outage (user_id, started_at) VALUES (?, now()) RETURNING id, started_at";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                rs.next();
                ManualOutage o = new ManualOutage();
                o.id = rs.getInt("id");
                o.startedAt = rs.getTimestamp("started_at").toString();
                return o;
            }
        }
    }

    public void markRestored(int userId, int outageId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE manual_outage SET restored_at = now() WHERE id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, outageId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        }
    }

    public List<ManualOutage> getManualOutages(int userId) throws SQLException {
        List<ManualOutage> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, started_at, restored_at FROM manual_outage WHERE user_id = ? ORDER BY started_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ManualOutage o = new ManualOutage();
                    o.id = rs.getInt("id");
                    o.startedAt = rs.getTimestamp("started_at").toString();
                    Timestamp restored = rs.getTimestamp("restored_at");
                    o.restoredAt = restored != null ? restored.toString() : null;
                    list.add(o);
                }
            }
        }
        return list;
    }
}
