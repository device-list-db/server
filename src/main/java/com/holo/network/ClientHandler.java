package com.holo.network;

import java.io.IOException;
import java.lang.System.Logger;
import java.net.Socket;
import java.util.Optional;

import com.holo.ServerMain;
import com.holo.util.Talker;
import com.holo.db.DBConnection;
import com.holo.db.Statements;
import com.holo.util.LoggerLevels;

/**
 * How to handle messages from the client
 * @since 0.1.0
 * @version 0.3.0
 */
public class ClientHandler implements Runnable {
    private Talker talker;
    private DBConnection dbConn;
    private String ipAddress;
    // User info connected to this socket
    private String username;
    private boolean isAdmin;
    private int infoChanged;
    private final Logger logger;

    public ClientHandler(Socket s, DBConnection conn) throws IOException {
        talker = new Talker(s);
        ipAddress = s.getLocalAddress().getHostAddress();
        logger = System.getLogger(ClientHandler.class.getName());
        dbConn = conn;
        infoChanged = 2;
        logger.log(LoggerLevels.DEBUG, ipAddress + ": Connected to the service");
        new Thread(this).start();
    }

    private void messageHandler(String message) throws IOException {
        String[] messageArray = message.split(" ");
        switch(messageArray[0]) {
            case "REGISTER":
                sendMessage(message, "Registering a new account with the database");
                break;
            case "REGISTER-ACCOUNT":
                sendMessage(message, "PRIVILEGE COMMAND: REGISTER-ACCOUNT used");
                break;
            case "LOGIN":
                sendMessage(message, "Attempting to log into the service. Account: " + messageArray[1]);
                break;
            case "LOG":
                sendMessage(message, messageArray[1]);
                break;
            case "GET-DEVICES":
                sendMessage(message, "Getting their devices");
                break;
            case "GET-DEVICES-ALL":
                sendMessage(message, "Getting all devices");
                break;
            case "ADMIN-RESPONSE":
                sendMessage(message, "Checking if user " + messageArray[1] + " can preform admin actions while logged in.");
                username = messageArray[1];
                break;
            case "REGISTER-DEVICE":
                sendMessage(message, messageArray[1] + " is registering a new device");
                break;
            case "UPDATE-DEVICE":
                sendMessage(message, messageArray[1] + " is updating an existing device");
                break;
            case "DELETE-DEVICE":
                sendMessage(message, messageArray[1] = " is deleting a device.");
                break;
			case "GET-PEOPLE":
				sendMessage(message, "PRIVILEGE COMMAND: GET-PEOPLE ran");
				break;
			case "ADD-PERSON":
				sendMessage(message, "PRIVILEGE COMMAND: ADD-PERSON ran");
				break;
			case "GET-AUTHOR-ID":
				sendMessage(message, "Getting an author ID");
				break;
			case "ADD-AUTHOR":
				sendMessage(message, "Creating an author in the system.");
				break;
			case "REGISTER-BOOK":
				sendMessage(message, "Registering a book.");
				break;
			case "GET-BOOKS":
				sendMessage(message, "Getting the list of books.");
				break;
			case "GET-PERSON":
				sendMessage(message, "Getting a single person");
				break;
			case "RENT-BOOK":
				sendMessage(message, "Renting a book");
				break;
			case "UNRENT-BOOK":
				sendMessage(message, "Returning a book");
				break;
            default: // Illegal protocol message - kill the client
                sendMessage("KILL", "Sent unrecgonized command '" + messageArray[0] + "'- Killing client");
                ServerMain.clientDisconnect(this);
                break;
        }
    }

    public void setUsername(String username) {
        if (infoChanged > 0) this.username = username;
        infoChanged--;
    }

    public Optional<String> getUsername() { return Optional.ofNullable(username); }

    public void setAdmin(boolean isAdmin) {
        if (infoChanged > 0) this.isAdmin = isAdmin;
        infoChanged--;
    }

    public boolean getAdmin() { return isAdmin; }

    public String getIp() { return ipAddress; }

    public void sendMessage(String message, String log) throws IOException {
        logger.log(LoggerLevels.DEBUG, ipAddress + ": " + log);
        talker.send(Statements.craftResponse(message, dbConn, talker, this));
    }

    @Override
    public void run() {
            try {
                while (infoChanged >= 0)
                    messageHandler(talker.recieve().substring(0));
                logger.log(LoggerLevels.ERROR, ipAddress + ": Attempted to manipulate server account variables");
                ServerMain.clientDishoneredDisconnect(this);
            } catch (IOException e) { // Socket was closed
                logger.log(LoggerLevels.ERROR, ipAddress + ": Socket closed");
                ServerMain.clientDisconnect(this);
            }
    }
    
}
