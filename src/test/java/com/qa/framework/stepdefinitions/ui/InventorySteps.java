package com.qa.framework.stepdefinitions.ui;

import com.qa.framework.context.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InventorySteps {

    private final TestContext context;

    public InventorySteps(TestContext context) {
        this.context = context;
    }

    @When("the user sorts products by {string}")
    public void theUserSortsProductsBy(String option) {
        context.pages().inventoryPage().sortBy(option);
    }

    @Then("the product prices are sorted in ascending order")
    public void pricesAreSortedAscending() {
        List<Double> actual = context.pages().inventoryPage().productPrices();
        Assert.assertEquals(actual, sorted(actual, Comparator.naturalOrder()), "Prices are not sorted low to high");
    }

    @Then("the product prices are sorted in descending order")
    public void pricesAreSortedDescending() {
        List<Double> actual = context.pages().inventoryPage().productPrices();
        Assert.assertEquals(actual, sorted(actual, Comparator.reverseOrder()), "Prices are not sorted high to low");
    }

    @Then("the product names are sorted in reverse alphabetical order")
    public void namesAreSortedReverseAlphabetically() {
        List<String> actual = context.pages().inventoryPage().productNames();
        Assert.assertEquals(actual, sorted(actual, Comparator.reverseOrder()), "Product names are not sorted Z to A");
    }

    private static <T> List<T> sorted(List<T> values, Comparator<? super T> order) {
        List<T> copy = new ArrayList<>(values);
        copy.sort(order);
        return copy;
    }
}
