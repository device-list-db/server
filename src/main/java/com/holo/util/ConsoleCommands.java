package com.holo.util;

import java.sql.SQLException;

import com.holo.db.DBConnection;
import com.holo.db.SystemStatements;

public class ConsoleCommands {
    public static String intrepretCommand(String cmd, DBConnection dbCon) {
        String[] cmdTmp = cmd.split(" ");
        if (cmdTmp[0].equalsIgnoreCase("help")) return runHelp();
        if (cmdTmp[0].equalsIgnoreCase("start")) return "";
        if (cmdTmp[0].equalsIgnoreCase("banip")) return runIpBan(cmdTmp, dbCon);
		if (cmdTmp[0].equalsIgnoreCase("unbanip")) return runIpUnban(cmdTmp, dbCon);
        if (cmdTmp[0].equalsIgnoreCase("banuser")) return runBanUser(cmdTmp, dbCon);
		if (cmdTmp[0].equalsIgnoreCase("unbanuser")) return runUnbanUser(cmdTmp, dbCon);
        return "Command not recognized";
    }

    private static String runHelp() {
        return "Help - Show this dialog\n"
        + "Start - Run the server\n"
        + "BanIP - Ban an ip address from connecting\n"
        + "BanUser - Ban a user from connecting";
    }

    private static String runIpBan(String[] cmdTmp, DBConnection dbCon) {
        if (cmdTmp.length != 2) {
            return "Command usage: banip <ip>";
        }
        try {
            SystemStatements.BanIP(dbCon, cmdTmp[1]);
            return "IP Banned.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occured.";
        }
    }

	private static String runIpUnban(String[] cmdTmp, DBConnection dbCon) {
		 if (cmdTmp.length != 2) {
            return "Command usage: unbanip <ip>";
        }
        try {
            SystemStatements.UnbanIP(dbCon, cmdTmp[1]);
            return "IP Unbanned.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "An error occured.";
        }
	}

    private static String runBanUser(String[] cmdTmp, DBConnection dbCon) {
		if (cmdTmp.length != 2) {
			return "Command usage: banuser <username>";
		}
		try {
			SystemStatements.BanUser(dbCon, cmdTmp[1]);
			return "User banned.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "An error occured";
		}
    }

	private static String runUnbanUser(String[] cmdTmp, DBConnection dbCon) {
		if (cmdTmp.length != 2) {
			return "Command usage: unbanuser <username>";
		}
		try {
			SystemStatements.UnbanUser(dbCon, cmdTmp[1]);
			return "User unbanned.";
		} catch (SQLException e) {
			e.printStackTrace();
			return "An error occured";
		}
    }
}
