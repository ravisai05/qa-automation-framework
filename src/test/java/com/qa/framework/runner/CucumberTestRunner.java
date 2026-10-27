package com.qa.framework.runner;

import com.qa.framework.config.ExecutionSettings;
import com.qa.framework.retry.RetryAnalyzer;
import io.cucumber.testng.CucumberOptions;
import io.cucumber.testng.FeatureWrapper;
import io.cucumber.testng.PickleWrapper;
import io.cucumber.testng.TestNGCucumberRunner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlTest;

import java.util.Optional;

/**
 * Custom TestNG runner for every UI and API scenario.
 * <p>
 * Instead of extending AbstractTestNGCucumberTests it drives {@link TestNGCucumberRunner} directly so it can:
 * <ol>
 *     <li>filter scenarios by -DfeatureToRun (OR of tags, '+' or '@' prefix, case-insensitive, de-duplicated);
 *     when absent, Cucumber's own -Dcucumber.filter.tags (a full tag expression) applies instead;</li>
 *     <li>run the remaining scenarios in parallel - pool size from -DthreadCount via ThreadCountListener;</li>
 *     <li>attach {@link RetryAnalyzer} to every scenario.</li>
 * </ol>
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.qa.framework.stepdefinitions", "com.qa.framework.hooks"},
        plugin = {
                "pretty",
                "json:target/cucumber-reports/cucumber.json",
                "html:target/cucumber-reports/cucumber.html",
                "rerun:target/rerun.txt",
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"
        },
        monochrome = true
)
public class CucumberTestRunner {

    private static final Logger LOG = LogManager.getLogger(CucumberTestRunner.class);

    private TestNGCucumberRunner testNGCucumberRunner;

    @BeforeClass(alwaysRun = true)
    public void setUpClass(ITestContext context) {
        XmlTest xmlTest = context.getCurrentXmlTest();
        testNGCucumberRunner = new TestNGCucumberRunner(getClass(), xmlTest::getParameter);
    }

    @Test(groups = "cucumber", description = "Runs Cucumber scenarios",
            dataProvider = "scenarios", retryAnalyzer = RetryAnalyzer.class)
    public void runScenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) {
        // featureWrapper is unused on purpose: it only makes the feature name visible in TestNG reports.
        testNGCucumberRunner.runScenario(pickleWrapper.getPickle());
    }

    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        Object[][] all = testNGCucumberRunner.provideScenarios();
        Optional<String> featureToRun = ExecutionSettings.featureToRun();
        if (featureToRun.isEmpty()) {
            LOG.info("featureToRun not set - running {} scenario(s) selected by cucumber.filter.tags (if any)", all.length);
            return all;
        }
        ScenarioFilter filter = ScenarioFilter.parse(featureToRun.get());
        Object[][] selected = filter.apply(all);
        LOG.info("featureToRun={} selected {} of {} scenario(s)", filter.wantedTags(), selected.length, all.length);
        if (selected.length == 0) {
            throw new IllegalArgumentException("-DfeatureToRun=" + featureToRun.get()
                    + " matched no scenario. Check the tag names in src/test/resources/features.");
        }
        return selected;
    }

    @AfterClass(alwaysRun = true)
    public void tearDownClass() {
        if (testNGCucumberRunner != null) {
            // Flushes the Cucumber plugins: cucumber.json, rerun.txt, Extent report.
            testNGCucumberRunner.finish();
        }
    }
}
