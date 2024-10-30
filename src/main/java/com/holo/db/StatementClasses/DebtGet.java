package com.holo.db.StatementClasses;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.util.Talker;
import com.holo.network.ClientHandler;

public class DebtGet implements Statement {
	private DBConnection con;
	private Talker talker;
	private ClientHandler ch;
	
	public DebtGet(DBConnection con, Talker talker, ClientHandler ch) {
		this.con = con;
		this.talker = talker;
		this.ch = ch;
	}
	
	public String run() {
		try {
			// Send number of debts total of the user
			PreparedStatement ps1 = con.getConnection().get().prepareStatement("SELECT COUNT(*) FROM debt WHERE debt.debtee_id IN (SELECT people.id FROM people, users WHERE users.username = ? AND people.id = users.name_id)");
			ps1.setString(1, ch.getUsername().get());
			ResultSet rs1 = con.returnResult(ps1).orElseThrow();
			int numTimes = 0;
			if (rs1.next()) {
				talker.send("DEBT-TOTAL " + rs1.getInt(1));
				numTimes = rs1.getInt(1);
			}
			else
				talker.send("DEBT-TOTAL 0");
			// Get debtor information loaded
			PreparedStatement ps2 = con.getConnection().get().prepareStatement("SELECT * FROM people WHERE people.id IN (SELECT debt.debtor_id FROM debt WHERE debt.debtee_id IN (SELECT people.id FROM people, users WHERE users.username = ? AND people.id = users.name_id))");
			ps2.setString(1, ch.getUsername().get());
			ResultSet rs2 = con.returnResult(ps2).orElseThrow();
			if (!rs2.next()) {
				// Throw exception
			}
			// Get amount owed information loaded
			PreparedStatement ps3 = con.getConnection().get().prepareStatement("SELECT debt.amount FROM debt WHERE debt.debtee_id IN (SELECT people.id FROM people, users WHERE users.username = ? AND people.id = users.name_id)");
			ps3.setString(1, ch.getUsername().get());
			ResultSet rs3 = con.returnResult(ps3).orElseThrow();
			if (!rs3.next()) {
				// Throw exception
			}
			// Get amount paid information loaded
			PreparedStatement ps4 = con.getConnection().get().prepareStatement("SELECT debt.paid FROM debt WHERE debt.debtee_id IN (SELECT people.id FROM people, users WHERE users.username = ? AND people.id = users.name_id)");
			ps4.setString(1, ch.getUsername().get());
			ResultSet rs4 = con.returnResult(ps4).orElseThrow();
			if (!rs4.next()) {
				// Throw exception
			}
			for (int i = 0; i < numTimes; i++) {
				talker.send("USER-ID " + rs2.getInt(1));
				talker.send("USER-NAME " + rs2.getString(2));
				talker.send("DEBT-TOTAL-AMOUNT " + rs3.getDouble(1));
				talker.send("DEBT-AMOUNT-PAID " + rs4.getDouble(1));
				rs2.next();
				rs3.next();
				rs4.next();
			}
			return "DEBT-SUCCESS";
		} catch (SQLException | IOException e) {
			e.printStackTrace();
			return "DEBT-FAILURE";
		}
	}
}