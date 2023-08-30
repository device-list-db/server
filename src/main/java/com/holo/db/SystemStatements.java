package com.holo.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemStatements {
    public static void BanIP(DBConnection con, String ip) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `ipban` VALUES(?)");
        ps.setString(1, ip);
        ps.executeQuery();
    }

    public static void BanUser(DBConnection con, String user) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE `users` SET 'banned'=? WHERE 'username'=?");
        ps.setBoolean(1, true);
        ps.setString(2, user);
        ps.executeQuery();
    }

    public static boolean IsIPBanned(DBConnection con, String ip) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM `ipban` WHERE `ip_address`=?");
        ps.setString(1, ip);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }
}
