package com.holo.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Various system statements needed for security
 * @since 0.1.0
 * @version 0.1.0
 */
public class SystemStatements {
    public static void BanIP(DBConnection con, String ip) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `ipban` VALUES(?)");
        ps.setString(1, ip);
        con.runQuery(ps);
    }

	public static void UnbanIP(DBConnection con, String ip) throws SQLException {
		PreparedStatement ps = con.getConnection().get().prepareStatement("DELETE FROM `ipban` WHERE `ip_address`=?");
		ps.setString(1, ip);
		con.runQuery(ps);
	}

    public static void BanUser(DBConnection con, String user) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE `users` SET `banned`=? WHERE `username`=?");
        ps.setBoolean(1, true);
        ps.setString(2, user);
        con.runQuery(ps);
    }

	public static void UnbanUser(DBConnection con, String user) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE `users` SET `banned`=? WHERE `username`=?");
        ps.setBoolean(1, false);
        ps.setString(2, user);
        con.runQuery(ps);
    }

    public static boolean IsIPBanned(DBConnection con, String ip) throws SQLException {
        PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM `ipban` WHERE `ip_address`=?");
        ps.setString(1, ip);
        ResultSet rs = con.returnResult(ps).get();
        return rs.next();
    }
}
