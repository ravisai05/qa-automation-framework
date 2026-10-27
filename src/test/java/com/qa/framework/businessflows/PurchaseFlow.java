package com.qa.framework.businessflows;

import com.qa.framework.context.ContextKey;
import com.qa.framework.context.TestContext;
import com.qa.framework.data.CheckoutCustomer;
import com.qa.framework.managers.PageObjectManager;

/** Cart -> checkout -> overview -> finish, spanning five pages. */
public class PurchaseFlow {

    /** The demo shop applies 8% tax to the item total. */
    static final double TAX_RATE = 0.08;

    private final TestContext context;

    public PurchaseFlow(TestContext context) {
        this.context = context;
    }

    /** Adds the product, opens the cart and proceeds to checkout step one. */
    public void openCheckoutWith(String product) {
        PageObjectManager pages = context.pages();
        pages.inventoryPage().addToCart(product);
        pages.inventoryPage().openCart();
        FlowPreconditions.require(pages.cartPage().isLoaded(), "Cart page did not load");
        pages.cartPage().proceedToCheckout();
        FlowPreconditions.require(pages.checkoutInformationPage().isLoaded(), "Checkout information step did not load");
    }

    /**
     * Places a full order. The overview totals are stored in the ScenarioContext before finishing,
     * so Then steps can assert them after the page has moved on.
     */
    public void purchase(String product, CheckoutCustomer customer) {
        openCheckoutWith(product);
        PageObjectManager pages = context.pages();
        pages.checkoutInformationPage().submit(customer);
        FlowPreconditions.require(pages.checkoutOverviewPage().isLoaded(), "Checkout overview did not load");
        context.scenarioContext().set(ContextKey.ORDER_ITEM_TOTAL, pages.checkoutOverviewPage().itemTotal());
        context.scenarioContext().set(ContextKey.ORDER_TOTAL, pages.checkoutOverviewPage().total());
        pages.checkoutOverviewPage().finish();
    }

    public static double expectedTotalWithTax(double itemTotal) {
        return Math.round(itemTotal * (1 + TAX_RATE) * 100.0) / 100.0;
    }
}
