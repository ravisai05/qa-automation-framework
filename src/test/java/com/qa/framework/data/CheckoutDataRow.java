package com.qa.framework.data;

/** One row of the "checkout" sheet in testdata/checkout-data.xlsx. */
public record CheckoutDataRow(String dataKey, CheckoutCustomer customer, String expectedError) {
}
