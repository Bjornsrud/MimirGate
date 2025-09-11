package com.mimirgate.model;

public class LoginResult {

    private final User user;
    private final int terminalWidth;
    private final LoginStatus status;

    public LoginResult(User user, int terminalWidth, LoginStatus status) {
        this.user = user;
        this.terminalWidth = terminalWidth;
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public int getTerminalWidth() {
        return terminalWidth;
    }

    public LoginStatus getStatus() {
        return status;
    }

    public enum LoginStatus {
        SUCCESS,
        RETRY,
        DISCONNECT
    }
}
