package com.qa.framework.businessflows;

import com.qa.framework.config.ConfigReader;
import com.qa.framework.context.TestContext;
import com.qa.framework.data.TestDataReader;
import com.qa.framework.data.UserAccount;

/** Login as a named demo user. Usernames come from testdata/users.json, the password from a secret. */
public class LoginFlow {

    static final String PASSWORD_SECRET = "UI_USER_PASSWORD";

    private final TestContext context;

    public LoginFlow(TestContext context) {
        this.context = context;
    }

    public void openLoginPage() {
        context.pages().loginPage().open();
    }

    /** @param alias key in users.json, e.g. "standard", "locked", "problem" */
    public void loginAs(String alias) {
        UserAccount user = TestDataReader.user(alias);
        context.pages().loginPage().loginAs(user.username(), ConfigReader.secret(PASSWORD_SECRET));
    }

    public void loginWith(String username, String password) {
        context.pages().loginPage().loginAs(username, password);
    }

    /** Precondition flow used by Background steps: open, log in, and confirm the products page loaded. */
    public void loginAndOpenProducts(String alias) {
        openLoginPage();
        loginAs(alias);
        FlowPreconditions.require(context.pages().inventoryPage().isLoaded(),
                "Products page did not load after logging in as '" + alias + "'");
    }
}
