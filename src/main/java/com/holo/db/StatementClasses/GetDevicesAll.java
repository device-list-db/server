package com.holo.db.StatementClasses;

import java.io.IOException;
import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;
import com.holo.util.Talker;

public class GetDevicesAll implements Statement{
    private DBConnection con;
    private Talker talker;
    private Logger logger;

    public GetDevicesAll(DBConnection con, Talker talker, Logger logger) {
        this.con = con;
        this.talker = talker;
        this.logger = logger;
    }

    public String run() {
        try {
            PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Devices");
            ResultSet rs1 = con.returnResult(ps1).get();
            if (rs1.next())
                talker.send("DEVICE-TOTAL " + rs1.getInt(1));
            else
                talker.send("DEVICE TOTAL 0");
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM Devices");
            ResultSet rs = con.returnResult(ps).orElseThrow();
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
}
