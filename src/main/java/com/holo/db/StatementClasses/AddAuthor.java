package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class AddAuthor implements Statement {
    private DBConnection con;
    private String[] array;

    public AddAuthor(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        try {
			PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO authors(name) VALUES (?)");
			ps.setString(1, array[1]);
			con.runQuery(ps);
			return "ADD-AUTHOR-SUCCESS";
		} catch (SQLException e) {
			return "ADD-AUTHOR-FAILURE";
		}
    }
}
