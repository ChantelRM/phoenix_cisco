package com.kilowhat.services;

import com.kilowhat.db.Database;
import com.kilowhat.models.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationService {

    public void create(int userId, String message) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO notification (user_id, message) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, message);
                ps.executeUpdate();
            }
        }
    }

    // Only unread ones - this is what the frontend polls to decide what
    // pop-up to show. Read notifications are kept in the table as history
    // rather than deleted.
    public List<Notification> getUnread(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, message, created_at, read FROM notification WHERE user_id = ? AND read = false ORDER BY created_at DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Notification n = new Notification();
                    n.id = rs.getInt("id");
                    n.message = rs.getString("message");
                    n.createdAt = rs.getTimestamp("created_at").toString();
                    n.read = rs.getBoolean("read");
                    list.add(n);
                }
            }
        }
        return list;
    }

    public void markRead(int userId, int notificationId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE notification SET read = true WHERE id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, notificationId);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        }
    }
}
