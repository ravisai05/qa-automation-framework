package com.qa.framework.retry;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Registered in testng.xml. Gives every @Test that has no retry analyzer of its own the framework's
 * {@link RetryAnalyzer}, so a new runner class (e.g. a team adding a dedicated API runner) cannot forget it.
 * CucumberTestRunner also declares the analyzer explicitly, so it keeps working if run without testng.xml
 * (e.g. right-click > Run As > TestNG Test on the class in Eclipse).
 */
public class RetryAnnotationTransformer implements IAnnotationTransformer {

    @Override
    @SuppressWarnings("rawtypes") // the TestNG interface itself declares raw Class/Constructor parameters
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        Class<?> current = annotation.getRetryAnalyzerClass();
        if (current == null || DisabledRetryAnalyzer.class.equals(current)) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
