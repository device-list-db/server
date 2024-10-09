package com.holo.db.StatementClasses;

import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;

public class GetPerson implements Statement {
    private DBConnection con;
    private String[] array;
    private Logger logger;

    public GetPerson(DBConnection con, String[] array, Logger logger) {
        this.con = con;
        this.array = array;
        this.logger = logger;
    }

    public String run() {
        try {
			PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT id FROM people WHERE name = ?");
			ps.setString(1, array[1]);
			ResultSet rs = con.returnResult(ps).orElseThrow();
			if (rs.next())
				return "PERSON-ID " + rs.getString(1);
			else
				return "PERSON-ID -1";
		} catch (SQLException e) {
			e.printStackTrace();
            logger.log(LoggerLevels.WARNING, "People table may be missing");
            return "PERSON-ID -1";
		}
    }
}
