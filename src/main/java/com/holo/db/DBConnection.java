package com.holo.db;

import java.sql.*;
import java.util.Properties;
import java.util.Optional;

/**
 * Server connection object, low level functions only
 */
public class DBConnection {
    private Connection conn;

    /**
     * Creates a simple database connection
     * @param ip The address of the MySQL server
     * @param port The port of the MySQL server
     * @param dbName The name of the database
     * @param login The Properties object containing the login info
     * @throws ClassNotFoundException If the DB Driver class was unable to be found
     * @throws SQLException If the database was inaccessible for some reason
     */
    public DBConnection(String ip, int port, String dbName, Properties login) throws ClassNotFoundException, SQLException {
        String connect = "jdbc:mysql://" + ip + ":" + port + "/" + dbName;
        Class.forName("com.mysql.cj.jdbc.Driver");
        conn = DriverManager.getConnection(connect, login.getProperty("db.user"), login.getProperty("db.pass"));
    }

    /**
     * Gets the connection object stored in this class
     * @return The connection object if it was successfully created, or null otherwise.
     */
    public Optional<Connection> getConnection() {
        return Optional.ofNullable(conn);
    }

    /**
     * Runs a select query on the database to retrieve information
     * @param query The query to obtain information
     * @return The resultset if one was found, null otherwise
     * @throws SQLException If the database was inaccessible for any reason
     */
    public Optional<ResultSet> returnResult(PreparedStatement query) throws SQLException {
        return Optional.ofNullable(query.executeQuery());
    }

    /**
     * Final step in a modify operation, executes the prepared statement
     * @throws SQLException
     */
    public void runQuery(PreparedStatement query) throws SQLException {
        query.execute();
    }

    public void shutdown() throws SQLException {
        if (conn != null) conn.close();
    }
}
