package com.qa.framework.listeners;

import com.qa.framework.config.ExecutionSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * Sizes the pool that runs @DataProvider(parallel = true) rows, i.e. how many scenarios run at once.
 * It runs before the suite starts, so the value is in place before the data provider is invoked.
 */
public class ThreadCountListener implements IAlterSuiteListener {

    private static final Logger LOG = LogManager.getLogger(ThreadCountListener.class);

    @Override
    public void alter(List<XmlSuite> suites) {
        int threads = ExecutionSettings.threadCount();
        suites.forEach(suite -> suite.setDataProviderThreadCount(threads));
        LOG.info("Scenario thread pool size: {}", threads);
    }
}
