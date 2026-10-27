package com.qa.framework.businessflows;

/**
 * Flows check their own preconditions and fail with a clear message, so a broken setup step is not
 * mistaken for a failure of the behaviour under test. Business assertions stay in the Then steps.
 */
final class FlowPreconditions {

    private FlowPreconditions() {
    }

    static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("Precondition failed: " + message);
        }
    }
}
