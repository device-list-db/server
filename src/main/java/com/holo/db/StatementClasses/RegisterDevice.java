package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;

public class RegisterDevice implements Statement {
    private DBConnection con;
    private String[] array;
    private Logger logger;

    public RegisterDevice(DBConnection con, String[] array, Logger logger) {
        this.con = con;
        this.array = array;
        this.logger = logger;
    }

    public String run() {
        String owner = array[1];
        String deviceSerial = array[2];
        String macAddress = array[3];
        String deviceName = array[4];
        try {
            PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `devices` VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, deviceSerial);
            ps.setString(2, macAddress);
            ps.setString(3, deviceName);
            ps.setString(4, owner);
            ps.setBoolean(5, false);
            con.runQuery(ps);
            return "DEVICE-YES";
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(LoggerLevels.WARNING, "Recieved incorrect response - Device is not registred.");
            return "DEVICE-NO";
        }
    }
}
