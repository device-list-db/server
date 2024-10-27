package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;

public class AddPerson implements Statement {
    private DBConnection con;
    private String[] array;
    private final Logger logger = System.getLogger(AddPerson.class.getName());;

    public AddPerson(DBConnection con, String[] array) {
        this.con = con;
        this.array = array;
    }

    public String run() {
        try {
			PreparedStatement ps = con.getConnection().get().prepareStatement("INSERT INTO people(`name`) VALUES (?)");
			ps.setString(1, array[1]);
			con.runQuery(ps);
            return "ADD-PERSON-OK";
		} catch (SQLException e) {
			e.printStackTrace();
			logger.log(LoggerLevels.WARNING, "Unable to add a person to the database.");
            return "ADD-PERSON-NO";
		}
    }
}
