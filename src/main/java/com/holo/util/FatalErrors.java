package com.holo.util;

/**
 * Various fatal errors of the server application
 * @since 0.1.0
 * @version 0.1.0
 */
public enum FatalErrors {
    PORT_TAKEN("Port 1997 is already in use by another application", 4),
    DATABASE_DRIVER("Database driver class was unable to be found.", 5),
    OFFLINE_DATABASE("Database is not online", 6),
    DATABASE_UNSYNC("Database may no longer be synced correctly", 7);

    private String errorMessage;
    private int errorCode;
    
    private FatalErrors(String msg, int code) {
        this.errorMessage = msg;
        this.errorCode = code;
    }

    public String getErrorMessage() { return this.errorMessage; }

    public int getErrorCode() { return this.errorCode; }

    @Override
    public String toString() {
        return "Code " + getErrorCode();
    }
}
