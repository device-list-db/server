package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class RegisterBook implements Statement {
    private DBConnection con;
    private String[] array;

    public RegisterBook(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        try {
			PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO books VALUES (?, ?, ?, ?)");
			ps.setString(1, array[1]);
			ps.setInt(2, Integer.parseInt(array[3]));
			ps.setString(3, array[4]);
			ps.setString(4, array[2]);
			con.runQuery(ps);
			return "ADD-BOOK-SUCCESS";
		} catch (SQLException e) {
			return "ADD-BOOK-FAILURE";
		}
    }
}
