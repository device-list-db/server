package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.network.ClientHandler;
import com.holo.util.LoggerLevels;

public class AdminResponse implements Statement{
    private DBConnection con;
    private ClientHandler ch;
    private String[] array;
    private Logger logger;

    public AdminResponse(DBConnection con, ClientHandler ch, String[] array, Logger logger) {
        this.con = con;
        this.ch = ch;
        this.array = array;
        this.logger = logger;
    }

    public String run() {
        try {
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT is_admin FROM Users WHERE username = ?");
            ps.setString(1, array[1]);
            ResultSet rs = con.returnResult(ps).orElseThrow();
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
}
