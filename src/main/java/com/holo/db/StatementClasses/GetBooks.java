package com.holo.db.StatementClasses;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.Talker;

public class GetBooks implements Statement {
    private DBConnection con;
    private Talker talker;

    public GetBooks(DBConnection con, Talker talker) {
        this.con = con;
        this.talker = talker;
    }

    public String run() {
        try {
			PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM Books");
			ResultSet rs1 = con.returnResult(ps1).orElseThrow();
			if (rs1.next()) {
				talker.send("BOOK-TOTAL " + rs1.getInt(1));
			} else {
				talker.send("BOOK-TOTAL 0");
			}
			PreparedStatement ps2 = con.getConnection().get().prepareStatement("SELECT authors.name AS 'Author Name', books.name AS 'Book Name', books.isbn, books.book_id FROM authors, books WHERE books.author_id = authors.author_id;");
			ResultSet rs2 = con.returnResult(ps2).orElseThrow();
			while (rs2.next()) {
				talker.send("BOOK-TITLE " + rs2.getString(2));
				talker.send("BOOK-AUTHOR " + rs2.getString(1));
				talker.send("BOOK-ISBN " + rs2.getString(3));
				talker.send("BOOK-ID " + rs2.getString(4));
				PreparedStatement ps3 = con.getConnection().get().prepareStatement("SELECT people.id, people.name, bookrentals.due_date FROM people, bookrentals, books WHERE books.book_id = bookrentals.book_id AND bookrentals.rentor = people.id AND books.book_id = ? AND bookrentals.returned = 0;");
				ps3.setString(1, rs2.getString(4));
				ResultSet rs3 = con.returnResult(ps3).orElseThrow();
				if (rs3.next()) {
					talker.send("BOOK-MORE");
					talker.send("USER-NAME " + rs3.getString(2));
					talker.send("USER-ID " + rs3.getString(1));
					talker.send("BOOK-DUE " + rs3.getTimestamp(3).toLocalDateTime().toString());
				} else {
					talker.send("BOOK-NEXT");
				}
			}
			return "BOOK-FINISH";
		} catch (IOException | SQLException e) {
			return "BOOK-FINISH";
		}
    }
}
