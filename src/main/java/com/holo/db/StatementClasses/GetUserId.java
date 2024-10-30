package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class GetUserId implements Statement {
    private DBConnection con;
    private String[] array;

    public GetUserId(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        try {
            PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT users.name_id FROM users WHERE username = ?");
            ps1.setString(1, array[1]);
            ResultSet rs1 = con.returnResult(ps1).get();
            if (rs1.next())
                return rs1.getInt(1) + "";
            else
                return "-1";
        } catch (SQLException e) {
            return "-1";
        }
    }
}
