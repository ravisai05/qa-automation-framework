package com.qa.framework.data;

/** A demo user from testdata/users.json. Passwords are not test data: they come from environment variables. */
public record UserAccount(String username, String description) {
}
