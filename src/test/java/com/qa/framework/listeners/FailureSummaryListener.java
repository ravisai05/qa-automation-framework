package com.qa.framework.listeners;

import com.qa.framework.retry.BrowserCrashSignatures;
import com.qa.framework.utils.ScenarioNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Writes target/failure-summary.csv (scenario, tags, first line of the error) for quick triage in CI.
 * Only FINAL failures are listed: an attempt that TestNG retries is reported as skipped, not failed.
 * The file is always written (header only when everything passed) so CI can always upload it.
 */
public class FailureSummaryListener implements ITestListener, ISuiteListener {

    static final Path OUTPUT = Path.of("target", "failure-summary.csv");

    private static final Logger LOG = LogManager.getLogger(FailureSummaryListener.class);

    /** Instance state (one listener per suite run), concurrent because scenarios fail on parallel threads. */
    private final Queue<String> rows = new ConcurrentLinkedQueue<>();

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable error = result.getThrowable();
        rows.add(String.join(",",
                csv(ScenarioNames.of(result)),
                csv(String.join(" ", ScenarioNames.tagsOf(result))),
                csv(error == null ? "" : BrowserCrashSignatures.firstLine(error))));
    }

    @Override
    public void onFinish(ISuite suite) {
        List<String> lines = new ArrayList<>();
        lines.add("scenario,tags,error");
        lines.addAll(rows);
        try {
            Files.createDirectories(OUTPUT.getParent());
            Files.write(OUTPUT, lines, StandardCharsets.UTF_8);
            LOG.info("Failure summary: {} failed scenario(s) -> {}", rows.size(), OUTPUT);
        } catch (IOException e) {
            LOG.error("Could not write {}: {}", OUTPUT, e.getMessage());
        }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
