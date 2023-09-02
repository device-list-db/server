package com.holo.db;

import java.io.IOException;
import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.Talker;
import com.holo.network.ClientHandler;
import com.holo.util.FatalErrors;
import com.holo.util.LoggerLevels;

public class Statements {
    private static final Logger logger = System.getLogger(Statements.class.getName());

    private static int personId(DBConnection con, String name) {
        try {
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT id FROM people WHERE name=?");
            ps.setString(1, name);
            ResultSet rs = con.returnResult(ps).orElseThrow();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {}
        return -1;
    }

    /**
     * Allow the server to craft the required response to the client
     * @param what What the request is
     * @return A string to send back
     */
    public static String craftResponse(String what, DBConnection con, Talker talker, ClientHandler ch) {
        String[] array = what.split(" ");
        switch(array[0]) {
            case "COUNT-ADMIN": {
            logger.log(LoggerLevels.WARNING, "COUNT-ADMIN recieved - Deprecated system call. Look into why it was used.");
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM users WHERE is_admin = true");
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    rs.next();
                    return "ADMIN-TOTAL " + Integer.toString(rs.getInt(1)); // Get the total amount of admin users
                } catch (SQLException e) {
                    return "ADMIN-TOTAL 0"; // Return the default if no database was found
                }
            }
            case "LOGIN": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT password, banned FROM users WHERE username = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    rs.next();
                    if (rs.getBoolean(2)) {
                        logger.log(LoggerLevels.WARNING, ch.getIp() + " has tried to log into a banned account.");
                        throw new SQLException("Banned user");
                    }
                    return "LOGIN-PASS " + rs.getString(1);
                } catch (SQLException e) {
                    return "LOGIN-PASS NULL";
                }
            }
            case "LOG": {
                logger.log(LoggerLevels.INFO, array[2] + " has logged in.");
                return "LOG-YES";
            }
            case "ADMIN-LOGIN": {
            logger.log(LoggerLevels.WARNING, "ADMIN-LOGIN recieved - Deprecated system call. Look into why it was used.");
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT password FROM users WHERE username = ? AND is_admin = ?");
                    ps.setString(1, array[1]);
                    ps.setBoolean(2, true);
                    ResultSet rs = con.returnResult(ps).orElseThrow();
                    rs.next();
                    return "ADMIN-PASS " + rs.getString(1);
                } catch (SQLException e) {
                    return "ADMIN-PASS NULL";
                }
            }
            case "REGISTER": {
                try {
                    String name = "";
                    PreparedStatement prePS = con.getConnection().get().prepareStatement("INSERT INTO `people`(name) VALUES (?)");
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `users` VALUES (?, ?, ?, ?, ?)");
                    ps.setString(1, array[1]); // Username
                    ps.setString(2, array[2]); // Password

                    if (array.length > 4) // Name contains a space
                        name = array[3] + " " + array[4];
                    else // Name does not contain a space
                        name = array[3];

                    prePS.setString(1, name);
                    con.runQuery(prePS); // Register them in the People table

                    ps.setInt(3, personId(con, name));
                    ps.setBoolean(4, false); // isAdmin
                    ps.setBoolean(5, false); // isBanned
                    con.runQuery(ps);
                    return "REGISTRATION-PASS"; // Succeeded
                } catch(SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.ERROR, FatalErrors.DATABASE_UNSYNC);
                    return "REGISTRATION-FAIL"; // DB error
                }
            }
            case "DEVICE-REGISTER": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `devices` VALUES (?, ?, ?, ?)");
                    ps.setString(1, array[1]);
                    ps.setString(2, array[2]);
                    ps.setString(3, array[3]);
                    ps.setString(4, array[4]);
                    con.runQuery(ps);
                    return "D-REGISTRATION-PASS";
                } catch (SQLException e) {
                    return "D-REGISTRATION-FAIL";
                }
            }
            case "DEBT-REGISTER": {
                try{
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `debt`(`debtor_id`, `debtee_id`, `amount`, `memo`) VALUES (?, ?, ?, ?)");
                    ps.setInt(1, personId(con, array[1]));
                    ps.setInt(2, personId(con, array[2]));
                    ps.setDouble(3, Double.parseDouble(array[3]));
                    ps.setString(4, array[4]);
                    con.runQuery(ps);
                    return "D-REGISTRATION-PASS";
                } catch (SQLException e) {
                    return "D-REGISTRATION-FAIL";
                }
            }
            case "DEBT-UPDATE": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE `debt` SET `paid`=? WHERE `debt_id`=?");
                    ps.setDouble(1, Double.parseDouble(array[1]));
                    ps.setInt(2, Integer.parseInt(array[2]));
                    con.runQuery(ps);
                    return "D-UPDATE-SUCCESS";
                } catch (SQLException e) {
                    return "D-UPDATE-FAIL";
                }
            }
            case "GET-DEVICES": {
                try {
                    PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Devices WHERE owner = ?");
                    ps1.setString(1, array[1]);
                    ResultSet rs1 = ps1.executeQuery();
                    if (rs1.next())
                        talker.send("DEVICE-TOTAL " + rs1.getInt(1));
                    else 
                        talker.send("DEVICE-TOTAL 0");
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM Devices WHERE owner = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        talker.send("DEVICE-SERIAL " + rs.getString(1));
                        talker.send("DEVICE-MAC " + rs.getString(2));
                        talker.send("DEVICE-NAME " + rs.getString(3));
                        talker.send("DEVICE-OWNER " + rs.getString(4));
                    }
                    return "DEVICE-FINISH";
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Unable to get device information - killing client as a safeguard");
                    return "KILL";
                }
            }
            case "ADMIN-RESPONSE": {
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT is_admin FROM Users WHERE username = ?");
                    ps.setString(1, array[1]);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        if (rs.getBoolean(1)) {
                            ch.setAdmin(true);
                            return "YES";
                        }
                    }
                    ch.setAdmin(false);
                    return "NO";
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Admin table may not exist");
                    ch.setAdmin(false);
                    return "NO";
                }
            }
            case "REGISTER-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                try {
                    PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `devices` VALUES (?, ?, ?, ?)");
                    ps.setString(1, deviceSerial);
                    ps.setString(2, macAddress);
                    ps.setString(3, deviceName);
                    ps.setString(4, owner);
                    con.runQuery(ps);
                    return "DEVICE-YES";
                } catch (SQLException e) {
                    e.printStackTrace();
                    logger.log(LoggerLevels.WARNING, "Recieved incorrect response - Device is not registred.");
                    return "DEVICE-NO";
                }
            }
            case "UPDATE-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                if (owner.equals(ch.getUsername().orElse("")) || ch.getAdmin())
                    try {
                        PreparedStatement ps = con.getConnection().get().prepareStatement("UPDATE devices SET mac_address=?, device_name=?, owner=? WHERE serial_number=?");
                        ps.setString(1, macAddress);
                        ps.setString(2, deviceName);
                        ps.setString(3, owner);
                        ps.setString(4, deviceSerial);
                        con.runQuery(ps);
                        return "DEVICE-UPDATE-YES";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logger.log(LoggerLevels.WARNING, "No device with that serial number exists");
                        return "DEVICE-UPDATE-NO";
                    }
                else {
                    logger.log(LoggerLevels.WARNING, ch.getUsername() + " tried to modify a device they had no permission to edit.");
                    return "DEVICE-UPDATE-NO";
                }
            }
            case "DELETE-DEVICE": {
                String owner = array[1];
                String deviceSerial = array[2];
                String macAddress = array[3];
                String deviceName = array[4];
                if (owner.equals(ch.getUsername().orElse("")) || ch.getAdmin())
                    try {
                        PreparedStatement ps = con.getConnection().get().prepareStatement("DELETE FROM devices WHERE mac_address=? AND device_name=? AND owner=? AND serial_number=?");
                        ps.setString(1, macAddress);
                        ps.setString(2, deviceName);
                        ps.setString(3, owner);
                        ps.setString(4, deviceSerial);
                        con.runQuery(ps);
                        return "DEVICE-DELETE-YES";
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logger.log(LoggerLevels.WARNING, "No device with that serial number exists");
                        return "DEVICE-DELETE-NO";
                    }
                else {
                    logger.log(LoggerLevels.WARNING, ch.getUsername() + " tried to delete a device they had no permission to delete.");
                    return "DEVICE-DELETE-NO";
                }
            }
        }
        return "KILL"; // Default stance - kill client
    }
}
