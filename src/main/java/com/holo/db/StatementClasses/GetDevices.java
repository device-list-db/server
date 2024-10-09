package com.holo.db.StatementClasses;

import java.io.IOException;
import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;
import com.holo.util.Talker;

public class GetDevices implements Statement {
    private DBConnection con;
    private Talker talker;
    private String[] array;
    private Logger logger;

    public GetDevices(DBConnection con, Talker talker, String[] array, Logger logger) {
        this.con = con;
        this.talker = talker;
        this.array = array;
        this.logger = logger;
    }
    
    public String run() {
        try {
            PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Devices WHERE owner = ?");
            ps1.setString(1, array[1]);
            ResultSet rs1 = con.returnResult(ps1).orElseThrow();
            if (rs1.next())
                talker.send("DEVICE-TOTAL " + rs1.getInt(1));
            else 
                talker.send("DEVICE-TOTAL 0");
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM Devices WHERE owner = ?");
            ps.setString(1, array[1]);
            ResultSet rs = con.returnResult(ps).get();
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
