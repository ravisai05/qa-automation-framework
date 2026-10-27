package com.qa.framework.api.client;

import com.qa.framework.context.TestContext;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

/**
 * Logs every call (method, URI, status, duration) and attaches the full exchange to the Cucumber scenario,
 * so API evidence appears in the Extent/Cucumber reports next to the step. Passwords and tokens are masked.
 */
public class ApiLoggingFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(ApiLoggingFilter.class);
    private static final Pattern SENSITIVE = Pattern.compile("(\"(?:password|token)\"\\s*:\\s*\")[^\"]*(\")");

    private final TestContext context;

    public ApiLoggingFilter(TestContext context) {
        this.context = context;
    }

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification response,
                           FilterContext filterContext) {
        long start = System.nanoTime();
        Response result = filterContext.next(request, response);
        long millis = (System.nanoTime() - start) / 1_000_000;

        LOG.info("{} {} -> {} ({} ms)", request.getMethod(), request.getURI(), result.statusCode(), millis);
        Object body = request.getBody();
        String exchange = request.getMethod() + " " + request.getURI() + "\n"
                + "Request body:\n" + mask(body == null ? "<none>" : body.toString()) + "\n\n"
                + "Status: " + result.statusCode() + " (" + millis + " ms)\n"
                + "Response body:\n" + mask(result.asString());
        context.scenario().attach(exchange, "text/plain", request.getMethod() + " " + request.getURI());
        return result;
    }

    static String mask(String text) {
        return SENSITIVE.matcher(text).replaceAll("$1****$2");
    }
}
