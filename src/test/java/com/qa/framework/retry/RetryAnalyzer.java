package com.qa.framework.retry;

import com.qa.framework.config.ExecutionSettings;
import com.qa.framework.utils.ScenarioNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Two independent retry budgets per scenario:
 * <ul>
 *     <li><b>Browser crash</b> (signature found anywhere in the cause chain): always retried, up to
 *     {@value #MAX_CRASH_RETRIES} times. The failure says nothing about the application, so a re-run with a
 *     fresh browser is the honest answer.</li>
 *     <li><b>Any other failure</b>: retried only when {@code -DisRetry=true}, up to {@code -DretryCount}
 *     (default 1). Off by default so flaky tests stay visible instead of being silently re-run.</li>
 * </ul>
 * TestNG keeps one analyzer instance per data-provider row (i.e. per scenario), so the counters are
 * per-scenario instance fields - never static.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    static final int MAX_CRASH_RETRIES = 3;

    private static final Logger LOG = LogManager.getLogger(RetryAnalyzer.class);

    private int crashRetries;
    private int normalRetries;

    @Override
    public boolean retry(ITestResult result) {
        Throwable failure = result.getThrowable();
        String scenario = ScenarioNames.of(result);

        if (failure != null && BrowserCrashSignatures.isBrowserCrash(failure)) {
            if (crashRetries < MAX_CRASH_RETRIES) {
                crashRetries++;
                LOG.warn("Browser crash in '{}' ({}). Crash retry {}/{}.", scenario,
                        BrowserCrashSignatures.firstLine(failure), crashRetries, MAX_CRASH_RETRIES);
                return true;
            }
            LOG.error("Browser crash in '{}' - crash retry budget ({}) exhausted.", scenario, MAX_CRASH_RETRIES);
            return false;
        }

        int allowed = ExecutionSettings.isRetryEnabled() ? ExecutionSettings.retryCount() : 0;
        if (normalRetries < allowed) {
            normalRetries++;
            LOG.warn("'{}' failed. Retry {}/{} (isRetry=true).", scenario, normalRetries, allowed);
            return true;
        }
        return false;
    }
}
