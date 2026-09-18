package com.kilowhat.services;

import com.kilowhat.db.Database;
import com.kilowhat.models.DailyUsage;
import com.kilowhat.models.Purchase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class UsageService {

    private final NotificationService notifications = new NotificationService();

    public DailyUsage recordDailyUsage(int userId, String date, double kwh) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = """
                INSERT INTO daily_usage (user_id, usage_date, kwh) VALUES (?, ?, ?)
                ON CONFLICT (user_id, usage_date) DO UPDATE SET kwh = excluded.kwh
                """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, date);
                ps.setDouble(3, kwh);
                ps.executeUpdate();
            }

            int id;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT id FROM daily_usage WHERE user_id = ? AND usage_date = ?")) {
                ps.setInt(1, userId);
                ps.setString(2, date);
                ResultSet rs = ps.executeQuery();
                rs.next();
                id = rs.getInt("id");
            }

            checkUsageSpike(conn, userId, kwh);

            DailyUsage d = new DailyUsage();
            d.id = id;
            d.date = date;
            d.kwh = kwh;
            return d;
        }
    }

    // Pop-up rule: if today's entry is notably higher than the trailing
    // 7-day average (excluding today), tell the user.
    private void checkUsageSpike(Connection conn, int userId, double todayKwh) throws SQLException {
        String sql = """
            SELECT AVG(kwh) AS avg_kwh FROM daily_usage
            WHERE user_id = ? AND usage_date >= date('now', '-7 days')
              AND usage_date < date('now')
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double avg = rs.getDouble("avg_kwh");
                if (avg > 0 && todayKwh > avg * 1.2) {
                    int percentOver = (int) Math.round(((todayKwh - avg) / avg) * 100);
                    notifications.create(conn, userId,
                        "Today's usage is " + percentOver + "% higher than your weekly average.");
                }
            }
        }
    }

    public List<DailyUsage> getHistory(int userId) throws SQLException {
        List<DailyUsage> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, usage_date, kwh FROM daily_usage WHERE user_id = ? ORDER BY usage_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    DailyUsage d = new DailyUsage();
                    d.id = rs.getInt("id");
                    d.date = rs.getString("usage_date");
                    d.kwh = rs.getDouble("kwh");
                    list.add(d);
                }
            }
        }
        return list;
    }

    public Purchase recordPurchase(int userId, double amountRand, double unitsKwh, String date) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO purchase (user_id, amount_rand, units_kwh, purchase_date) VALUES (?, ?, ?, ?)";
            int id;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setDouble(2, amountRand);
                ps.setDouble(3, unitsKwh);
                ps.setString(4, date);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
            }

            notifications.create(conn, userId,
                "Purchase recorded: R" + amountRand + " for " + unitsKwh + " kWh.");

            Purchase p = new Purchase();
            p.id = id;
            p.amountRand = amountRand;
            p.unitsKwh = unitsKwh;
            p.date = date;
            return p;
        }
    }

    public List<Purchase> getPurchases(int userId) throws SQLException {
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, amount_rand, units_kwh, purchase_date FROM purchase WHERE user_id = ? ORDER BY purchase_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Purchase p = new Purchase();
                    p.id = rs.getInt("id");
                    p.amountRand = rs.getDouble("amount_rand");
                    p.unitsKwh = rs.getDouble("units_kwh");
                    p.date = rs.getString("purchase_date");
                    list.add(p);
                }
            }
        }
        return list;
    }

    // purchasedKwh this calendar month, consumedKwh this calendar month
    // (sum of daily_usage), avgPerDay, and trendPercent vs last month's
    // avg/day consumption.
    public Map<String, Object> getSummary(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            double purchasedThisMonth = sumSince(conn,
                "SELECT COALESCE(SUM(units_kwh),0) AS total FROM purchase WHERE user_id = ? AND strftime('%Y-%m', purchase_date) = strftime('%Y-%m', 'now')",
                userId);

            double consumedThisMonth = sumSince(conn,
                "SELECT COALESCE(SUM(kwh),0) AS total FROM daily_usage WHERE user_id = ? AND strftime('%Y-%m', usage_date) = strftime('%Y-%m', 'now')",
                userId);

            double consumedLastMonth = sumSince(conn,
                "SELECT COALESCE(SUM(kwh),0) AS total FROM daily_usage WHERE user_id = ? AND strftime('%Y-%m', usage_date) = strftime('%Y-%m', date('now', '-1 month'))",
                userId);

            int dayOfMonth = java.time.LocalDate.now().getDayOfMonth();
            double avgPerDay = dayOfMonth > 0 ? consumedThisMonth / dayOfMonth : 0;

            int daysLastMonth = java.time.YearMonth.now().minusMonths(1).lengthOfMonth();
            double avgPerDayLastMonth = daysLastMonth > 0 ? consumedLastMonth / daysLastMonth : 0;

            double trendPercent = avgPerDayLastMonth > 0
                ? ((avgPerDay - avgPerDayLastMonth) / avgPerDayLastMonth) * 100
                : 0;

            Map<String, Object> summary = new HashMap<>();
            summary.put("purchasedKwh", round2(purchasedThisMonth));
            summary.put("consumedKwh", round2(consumedThisMonth));
            summary.put("avgPerDay", round2(avgPerDay));
            summary.put("trendPercent", round2(trendPercent));
            return summary;
        }
    }

    private double sumSince(Connection conn, String sql, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getDouble("total");
        }
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
