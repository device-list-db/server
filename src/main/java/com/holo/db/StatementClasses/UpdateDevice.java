package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.network.ClientHandler;
import com.holo.util.LoggerLevels;

public class UpdateDevice implements Statement {
    private DBConnection con;
    private String[] array;
    private ClientHandler ch;
    private Logger logger;

    public UpdateDevice(DBConnection con, String[] array, ClientHandler ch, Logger logger) {
        this.con = con;
        this.array = array;
        this.ch = ch;
        this.logger = logger;
    }

    public String run() {
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
}
