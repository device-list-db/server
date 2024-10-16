package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.System.Logger;

import com.holo.db.DBConnection;
import com.holo.network.ClientHandler;
import com.holo.util.LoggerLevels;

public class Login implements Statement {
    private DBConnection con;
    private ClientHandler ch;
    private String[] array;
    private Logger logger;

    public Login(DBConnection con, ClientHandler ch, String[] array, Logger logger) {
        this.con = con;
        this.ch = ch;
        this.array = array;
        this.logger = logger;
    }

    public String run() {
        try {
            PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT * FROM ipban WHERE ip_address = ?");
            ps1.setString(1, ch.getIp());
            ResultSet rs1 = con.returnResult(ps1).orElseThrow();
            if (rs1.next()) {
                logger.log(LoggerLevels.WARNING, ch.getIp() + " has tried to log in, when their IP is banned.");
                throw new SQLException("Banned IP");
            }
            PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT password, banned FROM users WHERE username = ?");
            ps.setString(1, array[1]);
            ResultSet rs = con.returnResult(ps).orElseThrow();
            rs.next();
            if (rs.getBoolean(2)) {
                logger.log(LoggerLevels.WARNING, ch.getIp() + " has tried to log into a banned account.");
                throw new SQLException("Banned user");
            }
            logger.log(LoggerLevels.INFO, array[1] + " has logged in.");
            return "LOGIN-PASS " + rs.getString(1);
            } catch (SQLException e) {
                return "LOGIN-PASS NULL";
            }
        }
}

