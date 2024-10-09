package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class DebtRegister implements Statement {
    private DBConnection con;
    private String[] array;

    public DebtRegister(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
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
        try{
            PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO `debt`(`debtor_id`, `debtee_id`, `amount`, `memo`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, personId(con, array[1]));
            ps.setInt(2, personId(con, array[2]));
            ps.setDouble(3, Double.parseDouble(array[3]));
            ps.setString(4, array[4]);
            con.runQuery(ps);
            return "D-REGISTRATION-PASS";
        } catch (SQLException e) {
            return "D-REGISTRATION-FAIL";
        }
    }
}
