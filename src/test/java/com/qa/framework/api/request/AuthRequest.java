package com.qa.framework.api.request;

public record AuthRequest(String username, String password) {

    /** Never print the password (records include every field in toString by default). */
    @Override
    public String toString() {
        return "AuthRequest[username=" + username + ", password=****]";
    }
}
