package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.FatalErrors;
import com.holo.util.LoggerLevels;

public class RegisterAccount implements Statement {
    private DBConnection con;
    private String[] array;
    private Logger logger;

    public RegisterAccount(DBConnection con, String[] array, Logger logger) {
        this.con = con;
        this.array = array;
        this.logger = logger;
    }

    public String run() {
        try {
            String name;
            if (array.length > 4) // Name contains a space
                name = array[3] + "_" + array[4];
            else // Name does not contain a space
                name = array[3];
            PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT `id` FROM people WHERE name=?");
            ps1.setString(1, name);
            ResultSet rs = con.returnResult(ps1).orElseThrow();
            rs.next();
            int id = rs.getInt(1);
            if (id == 0) {
                // Something went wrong - kill the client as a precaution
                return "KILL";
            }
            PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `users` VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, array[1]); // Username
            ps.setString(2, array[2]); // Password
            ps.setInt(3, id);
            ps.setBoolean(4, false); // isAdmin
            ps.setBoolean(5, false); // isBanned
            con.runQuery(ps);
            return "REGISTRATION-PASS"; // Succeeded
        } catch (SQLException e) {
            e.printStackTrace();
            logger.log(LoggerLevels.ERROR, FatalErrors.DATABASE_UNSYNC);
            return "REGISTRATION-FAIL"; // DB error
        }
    }
}
