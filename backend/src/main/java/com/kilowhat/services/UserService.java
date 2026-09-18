package com.kilowhat.services;

import com.kilowhat.db.Database;
import com.kilowhat.models.ApplianceCategory;
import com.kilowhat.models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    public User register(String username, String areaId, int householdSize,
                          List<ApplianceCategory> categories) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                String insertUser = "INSERT INTO users (username, area_id, household_size) VALUES (?, ?, ?) RETURNING id";
                try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                    ps.setString(1, username);
                    ps.setString(2, areaId);
                    ps.setInt(3, householdSize);
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    userId = rs.getInt("id");
                }

                String insertCategory = "INSERT INTO appliance_category (user_id, category, count) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertCategory)) {
                    for (ApplianceCategory c : categories) {
                        ps.setInt(1, userId);
                        ps.setString(2, c.category);
                        ps.setInt(3, c.count);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();

                User user = new User();
                user.id = userId;
                user.username = username;
                user.areaId = areaId;
                user.householdSize = householdSize;
                return user;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public User login(String username) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, username, area_id, household_size FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;
                return mapUser(rs);
            }
        }
    }

    public User getById(int userId) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, username, area_id, household_size FROM users WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return null;
                return mapUser(rs);
            }
        }
    }

    public List<ApplianceCategory> getCategories(int userId) throws SQLException {
        List<ApplianceCategory> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, category, count FROM appliance_category WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    ApplianceCategory c = new ApplianceCategory();
                    c.id = rs.getInt("id");
                    c.category = rs.getString("category");
                    c.count = rs.getInt("count");
                    list.add(c);
                }
            }
        }
        return list;
    }

    public void updateCategory(int userId, int categoryId, int newCount) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE appliance_category SET count = ? WHERE id = ? AND user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, newCount);
                ps.setInt(2, categoryId);
                ps.setInt(3, userId);
                ps.executeUpdate();
            }
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.id = rs.getInt("id");
        u.username = rs.getString("username");
        u.areaId = rs.getString("area_id");
        u.householdSize = rs.getInt("household_size");
        return u;
    }
}
