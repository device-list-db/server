package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import com.holo.db.DBConnection;

public class UnrentBook implements Statement {
    private DBConnection con;
    private String[] array;

    public UnrentBook(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        LocalDate ld = LocalDate.now();
		try {
			PreparedStatement ps1 = con.getConnection().get().prepareStatement("UPDATE bookrentals SET returned=1, return_date=? WHERE book_id=? AND returned=0");
            ps1.setString(1, ld.toString());
			ps1.setString(2, array[1]);
			con.runQuery(ps1);
			return "UNRENT-BOOK-SUCCESS";
		} catch (SQLException e) {
			return "UNRENT-BOOK-FAILURE";
		}
    }
}
