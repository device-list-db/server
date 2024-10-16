package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class DebtUpdate implements Statement {
    private DBConnection con;
    private String[] array;

    public DebtUpdate(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
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
}
