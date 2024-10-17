package com.holo.db.StatementClasses;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import com.holo.db.DBConnection;

public class RentBook implements Statement {
    private DBConnection con;
    private String[] array;

    public RentBook(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        LocalDate ldt = LocalDate.now();
        LocalDate dueTime = ldt.plusWeeks(2);
		try {
			PreparedStatement ps1 = con.getConnection().get().prepareStatement("INSERT INTO bookrentals(book_id, rentor, rented_date, due_date) VALUES (?, ?, ?, ?)");
			ps1.setString(1, array[1]);
			ps1.setInt(2, Integer.parseInt(array[2]));
			ps1.setString(3, ldt.toString());
			ps1.setString(4, dueTime.toString());
			con.runQuery(ps1);
		} catch (SQLException e) {
			return "NULL";
		}
		return "RENT-BOOK-SUCCESS";
    }
}
