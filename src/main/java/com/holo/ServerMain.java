package com.holo;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.holo.db.DBConnection;
import com.holo.db.SystemStatements;
import com.holo.network.ClientHandler;
import com.holo.util.ConsoleCommands;
import com.holo.util.FatalErrors;
import com.holo.util.LoggerLevels;

import io.github.cdimascio.dotenv.Dotenv;


/**
 * Main class for the server
 * @since 0.1.0
 * @version 0.1.0
 */
public class ServerMain {
    private static final Logger logger = System.getLogger(ServerMain.class.getName());
    private static ArrayList<ClientHandler> clientList;
    private static DBConnection dbCon;
    private static ServerSocket ss;
    /**
     * Main function for the server
     */
    public static void main( String[] args ) {
        logger.log(LoggerLevels.INFO, "Home Device Server application" );

        clientList = new ArrayList<>();

        connectDatabase(Dotenv.configure().load());
        serverLoop(); // Server loop starts here
        logger.log(LoggerLevels.INFO, "Shutting down...");
        closeDatabaseConnection();
        logger.log(LoggerLevels.INFO, "Stopped");
    }

    private static void connectDatabase(Dotenv dotenv) {
        Properties prop = new Properties();
        prop.put("db.user", dotenv.get("DBUSER"));
        prop.put("db.pass", dotenv.get("DBPASS"));

        try {
            dbCon = new DBConnection(dotenv.get("DBIP"), Integer.parseInt(dotenv.get("DBPORT")), dotenv.get("DBNAME"), prop);
            ss = new ServerSocket(Integer.parseInt(dotenv.get("SERVPORT")));
        } catch (ClassNotFoundException e1) {
            logger.log(LoggerLevels.ERROR, FatalErrors.DATABASE_DRIVER.getErrorMessage());
        } catch (SQLException e1) {
            logger.log(LoggerLevels.ERROR, FatalErrors.OFFLINE_DATABASE.getErrorMessage());
        } catch (IOException e) {
            logger.log(LoggerLevels.ERROR, FatalErrors.PORT_TAKEN.getErrorMessage());
        }
    }

    private static void serverLoop() {
        String cmd;
        logger.log(LoggerLevels.INFO, "Insert console commands");
        do {
            cmd = System.console().readLine();
            System.out.println(ConsoleCommands.intrepretCommand(cmd, dbCon));
        } while (!cmd.equalsIgnoreCase("start"));
        if (ss != null) {
            logger.log(LoggerLevels.INFO, "Accepting new connections");
            do {
                try {
                    Socket s = ss.accept();
                    if (!SystemStatements.IsIPBanned(dbCon, s.getLocalAddress().getHostAddress())) clientList.add(new ClientHandler(s, dbCon));
                    s.getLocalAddress().getHostAddress();
                    clientList.add(new ClientHandler(s, dbCon));
                } catch (IOException e) {
                    logger.log(LoggerLevels.WARNING, "Connection reset by client");
                } catch (SQLException e) {}
            } while (ss != null);
            logger.log(LoggerLevels.INFO, "New connections no longer accepted");
        }
    }

    public static void clientDisconnect(ClientHandler ch) {
        clientList.remove(ch);
    }

    public static void clientDishoneredDisconnect(ClientHandler ch) {
        clientList.remove(ch);
        try {
            SystemStatements.BanIP(dbCon, ch.getIp());
            SystemStatements.BanUser(dbCon, ch.getUsername().orElse("NULL"));
        } catch (SQLException e) {}
    }

    /**
     * Shut down the server ungracefully (fatal error)
     * @param errorCode The code of the error that occured
     * @deprecated Use the FatalErrors enum version
     */
    public static void serverShutdown(int errorCode) {
        if (errorCode != 5)
            closeDatabaseConnection();
        System.exit(errorCode);
    }

    /**
     * Shutdown the server due to a fatal error
     * @param error The error that caused the shutdown
     */
    public static void serverShutdown(FatalErrors error) {
        for (int i = 0; i < clientList.size(); i++) {
            try {
                clientList.get(i).sendMessage("KILL", "Killing client due to shutdown");
            } catch(IOException e) {}
        }
        if (error != FatalErrors.DATABASE_DRIVER)
            closeDatabaseConnection();
        System.exit(error.getErrorCode());
    }

    private static void closeDatabaseConnection() {
        try {
            dbCon.shutdown();
            logger.log(LoggerLevels.INFO, "Database connection shut down.");
        } catch (SQLException e) {
            logger.log(LoggerLevels.ERROR, "Database shutdown failed for an unknown reason.");
        }
    }
}
