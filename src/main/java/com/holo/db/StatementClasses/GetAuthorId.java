package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;

public class GetAuthorId implements Statement {
    private DBConnection con;
    private String[] array;

    public GetAuthorId(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        try {
			PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT * FROM authors WHERE name=?");
			ps.setString(1, array[1]);
			ResultSet rs = con.returnResult(ps).orElseThrow();
			if (rs.next()) {
				return String.valueOf(rs.getInt(1));
			} else {
				return "-1";
			}
		} catch (SQLException e) {
			return "-1";
		}
    }
}
