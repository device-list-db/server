package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.FatalErrors;
import com.holo.util.LoggerLevels;

public class Register implements Statement {
    private DBConnection con;
    private String[] array;
    private Logger logger;

    public Register(DBConnection con, String[] array, Logger logger) {
        this.con = con;
        this.array = array;
        this.logger = logger;
    }

    private int personId(DBConnection con, String name) {
        try {
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT id FROM people WHERE name=?");
            ps.setString(1, name);
            ResultSet rs = con.returnResult(ps).orElseThrow();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {}
        return -1;
    }

    public String run() {
        try {
            String name = "";
            PreparedStatement prePS = con.getConnection().get().prepareStatement("INSERT INTO `people`(name) VALUES (?)");
            PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `users` VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, array[1]); // Username
            ps.setString(2, array[2]); // Password

            if (array.length > 4) // Name contains a space
                name = array[3] + "_" + array[4];
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
}
