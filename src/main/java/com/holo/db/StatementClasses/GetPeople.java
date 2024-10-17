package com.holo.db.StatementClasses;

import java.io.IOException;
import java.lang.System.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.LoggerLevels;
import com.holo.util.Talker;

public class GetPeople implements Statement {
    private DBConnection con;
    private Talker talker;
    private Logger logger;

    public GetPeople(DBConnection con, Talker talker, Logger logger) {
        this.con = con;
        this.talker = talker;
        this.logger = logger;
    }

    public String run() {
        try {
			PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM people");
			ResultSet rs1 = con.returnResult(ps1).orElseThrow();
			if (rs1.next())
				talker.send("USER-TOTAL " + rs1.getInt(1));
			else
				talker.send("USER-TOTAL 0");
			PreparedStatement ps = con.getConnection().get().prepareStatement("SELECT people.id, users.username, people.name FROM people LEFT JOIN users ON users.name_id = people.id;");
			ResultSet rs = con.returnResult(ps).orElseThrow();
			while (rs.next()) {
				talker.send("USER-ID " + rs.getInt(1));
				talker.send("USER-USERNAME " + rs.getString(2));
				String name = rs.getString(3);
				if (name.contains(" "))
					name.replace(' ', '_');
				talker.send("USER-NAME " + name);
			}
			return "USER-FINISH";
		} catch (IOException | SQLException e) {
			e.printStackTrace();
			logger.log(LoggerLevels.WARNING, "Unable to get user info - killing client as a safeguard.");
			return "KILL";
		}
    }
}
