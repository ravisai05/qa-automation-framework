# Interview Notes: QA Automation Framework

Ravi Sai Saridevi, Senior QA Engineer. Talking points about this repository. Every answer below refers to code in this project.

---

## 30-second pitch

"I built a layered BDD framework in Java 17 that tests a web UI with Selenium 4 and a REST API with REST Assured,
from one Maven project. It runs on Cucumber 7 and TestNG, with a custom runner. You can choose scenarios by
test-case ID, like `-DfeatureToRun="+TC-UI-001,+TC-API-002"`. They run in parallel threads, and in CI the nightly
regression is split by those IDs across parallel jobs. Each scenario gets its own browser, pages, API client and
state through PicoContainer, so there is no shared state between threads. Browser crashes are retried
automatically, but real failures are not unless you ask for it. Every scenario has a test-case ID that maps to a
test-management tool."

---

## 2-minute walkthrough

1. **Entry point.** Surefire runs `testng.xml`. Three listeners run first:
   - `ThreadCountListener` sizes the parallel pool from `-DthreadCount`. It validates the value and falls back to 2.
   - `RetryAnnotationTransformer` makes sure every test has a retry analyzer.
   - `FailureSummaryListener` writes a CSV of the final failures.
2. **Runner.** `CucumberTestRunner` does not extend `AbstractTestNGCucumberTests`. It drives `TestNGCucumberRunner`
   directly. Its `@DataProvider(parallel = true)` gets every scenario and passes them through `ScenarioFilter`:
   - The tags in `featureToRun` are combined with OR.
   - Each tag can start with `+` or `@`, and case does not matter.
   - A scenario that matches two tags runs only once.
   - If `featureToRun` is not set, the standard `cucumber.filter.tags` applies instead.
3. **Per-scenario objects.** Cucumber creates a new PicoContainer for each scenario. `TestContext` holds three things:
   a `ScenarioContext` (a typed key/value map), a `DriverManager` (one browser) and a lazily created
   `PageObjectManager`. The hooks start a browser only for `@ui` scenarios. On failure they attach a screenshot.
   They also route the logs into one file per scenario.
4. **Layers.** The step definitions are thin. They call **business flows**, such as `LoginFlow`, `PurchaseFlow` and
   `BookingFlow`. The flows call **pages** or **API clients**. Pages only talk to the browser through
   `WebDriverWrapper`. Every action in the wrapper waits explicitly first and has crash recovery.
5. **API.** `ApiRequestFactory` builds a request spec for each scenario, with a logging filter that masks secrets
   and attaches each request and response to the report. `AuthTokenProvider` gets the token once per scenario.
   Requests and responses are Java records.
6. **Retries.** There are two separate budgets:
   - Browser crashes are matched against a short list of messages. The whole exception cause chain is checked. They
     are always retried, up to 3 times.
   - Other failures are retried only with `-DisRetry=true`.
7. **CI.**
   - Pull requests run the `@smoke` scenarios.
   - The nightly run calls `shard_tests.py`, which turns the `@TC` IDs into a job matrix.
   - There is also a "failed" mode that re-runs only the failures of an earlier run.
   - Every job fails if `cucumber.json` is missing or empty, or if any scenario failed.

---

## Likely questions and answers

### Architecture

**Why did you write your own runner instead of extending `AbstractTestNGCucumberTests`?**
I needed three things the base class does not give me. First, filtering by a list of test-case IDs with OR logic
and no duplicates. That is what CI sharding and "re-run only the failures" depend on. Second, setting the number of
parallel threads from a property. Third, attaching a retry analyzer explicitly. Under the hood I still use
Cucumber's `TestNGCucumberRunner`, so the reports and plugins work exactly as usual.

**How do you make parallel runs thread-safe?**
Each object's lifetime limits who can see it. PicoContainer creates the driver, pages, API spec, token and
scenario state again for every scenario, and a scenario never switches thread. There is no static mutable state.
The only static fields are constants, an unmodifiable configuration map and Jackson's `ObjectMapper`, which is
thread-safe. I also avoid changing RestAssured's global static settings, because every thread shares them.

**Why not use a ThreadLocal WebDriver?**
A ThreadLocal works, but you have to remember to call `remove()`. Otherwise a pooled thread can carry a dead driver
into the next test. With Pico, the driver belongs to an object that only lives for one scenario, so it cannot leak
into another one. The hooks still quit the browser explicitly, in a `finally` block.

**Why a business-flow layer?**
Some journeys span several pages, such as "cart, then checkout, then overview, then finish". If the steps
contained that logic, step definitions would call each other and logic would be duplicated. Flows keep the steps
to one or two lines. Flows also check their own preconditions and fail with "Precondition failed: …". That way a
broken setup step is never reported as a bug in the feature under test. The business assertions stay in the Then
steps.

**Why do pages receive the Cucumber `Scenario`?**
So a page can attach evidence at the moment it reads something. For example, it can attach the list of prices it
checked, or a screenshot of the order confirmation. The report then shows what the test actually saw, not only the
result.

### Synchronisation and stability

**How do you handle waits?**
The implicit wait is 0, and every wait is explicit, through `WebDriverWait` in `WaitUtils`. The wrapper always
waits before acting: for example, it waits until an element is clickable before clicking it. There is no
`Thread.sleep` anywhere. Mixing implicit and explicit waits makes timeouts add up unpredictably.

**Explain the retry design. Isn't retrying just hiding flakiness?**
That is exactly why there are two budgets.
- A browser crash ("tab crashed", "invalid session id", "chrome not reachable", "DevToolsActivePort", "session
  deleted because of page crash") says nothing about the product. So it is always retried, up to 3 times, with a
  fresh browser.
- Any other failure is retried only when someone opts in with `-DisRetry=true`.

The list of crash messages is deliberately short. If I added something broad like "timeout", real defects would
turn green on retry. I check the whole exception cause chain because Selenium errors often arrive wrapped. The
retry counters are per scenario, because TestNG keeps one analyzer instance per data-provider row.

**What does the page reload in `WebDriverWrapper` add on top of that?**
It handles the cheap case. If only the tab crashed and the session is still alive, one reload plus repeating the
action saves the whole scenario. If the session is dead, the reload fails too. The wrapper then rethrows the
original exception, so the retry analyzer still sees the crash message and re-runs the scenario with a new browser.

**How do retries show up in reports?**
TestNG records a retried attempt as SKIPPED and only the last attempt as passed or failed. That is why the CI gate
reads `testng-results.xml`. `cucumber.json` lists every attempt, which is good for investigation but wrong for
deciding pass or fail.

### Data and configuration

**How is configuration resolved?**
In this order:
1. `-D` system property
2. `config-<env>.properties` (chosen with `-Denv=qa|staging`)
3. `config.properties`

The values are loaded once into an unmodifiable map. A missing key fails immediately, and the error message says
which file to add it to.

**How do you handle secrets?**
`ConfigReader.secret()` only reads environment variables. Locally they come from a git-ignored `.env` file, which
is documented in `.env.example`. In Eclipse they go in the run configuration, and in CI they come from secrets.
Passwords never appear in feature files. The `AuthRequest` and `AuthResponse` records override `toString()`, and
the API logging filter masks `password` and `token` fields before logging or attaching anything.

**What test data sources do you use, and why several?**
- JSON for structured fixtures, such as user aliases and booking payloads.
- Excel (Apache POI) for tables that non-developers maintain. Each checkout validation row holds both the inputs
  and the expected error.
- JavaFaker for data that must not clash between runs, such as customers and bookings.

The generated values are attached to the report, so a failure can be reproduced. I would replace JavaFaker with
Datafaker next, because JavaFaker is no longer maintained.

### API testing

**Walk me through an API scenario.**
TC-API-005 updates a booking:
1. The Given step creates a booking from `bookings.json` through `BookingFlow` and checks that it returned 200.
2. The When step builds a changed copy with `BookingRequestBuilder.from(original)`. It gets the token lazily from
   `AuthTokenProvider` and sends a PUT with the `Cookie: token=…` header.
3. The response is stored in `ScenarioContext`.
4. The Then steps check the status code and the returned fields.

The whole exchange, with secrets masked, is attached to the report.

**Any API quirks?**
Yes, and the tests assert them as they really are:
- Creating a booking returns 200, not 201.
- Deleting returns 201.
- Bad credentials return 200 with `"reason": "Bad credentials"`.

On a real product I would raise these as contract defects. For a practice API they are its documented behaviour.

### Traceability and CI

**How do tags map to your test-management tool?**
Each scenario has one `@TC-…` ID. Every example of a Scenario Outline shares it. The ID is the key of the test
case in a Jira/Xray-style tool. A CI step would read `cucumber.json`, which includes each scenario's tags, map each
ID to an issue key and post the results to a test execution. I left that call out of this public project on
purpose, but the input it needs is standard Cucumber JSON.

**How does the sharding work?**
1. `shard_tests.py` reads the feature files line by line and builds a list of scenarios with their tags, names and
   line numbers.
2. It selects scenario IDs in one of three modes:
   - `regression`: every `@regression` scenario.
   - `custom`: the IDs you give it. Other tags, such as `@smoke`, are expanded into the IDs that carry them.
   - `failed`: the IDs from `rerun.txt`, matched by feature file and line number, including example rows. It can
     also use `testng-results.xml`, where it only takes final failures, so it accounts for retries.
3. It removes duplicates and deals the IDs round-robin into N shards, which mixes UI and API tests.
4. It writes a matrix in the format GitHub Actions expects.

In failed mode, a missing results file stops the run with exit code 2 and a clear message.

**How do you stop CI from passing when nothing ran?**
`verify_results.py` fails the job in two cases:
- `cucumber.json` is missing, empty, `[]`, or contains no scenarios.
- TestNG recorded any final failure.

Maven runs with `testFailureIgnore=true` so that every report is still generated, and the gate makes the final
decision. There is no `continue-on-error` anywhere, and reports are uploaded with `if: always()`.

**How fast can it go?**
Total parallelism is the number of shards times the number of threads. For example, 4 jobs × 3 threads runs 12
scenarios at once. The obvious next step is to weight the shards by how long each test took in the previous run.
That data is already in `cucumber.json`.

### Trade-offs and what I'd do next

- **Duration-aware sharding**, using previous timings instead of round-robin.
- **Contract tests** with JSON Schema validation (REST Assured supports it) alongside the field checks.
- **Remote Selenium Grid or containerised browsers.** `DriverFactory` is the only class that would change.
- **Upload results to the test-management tool** as a CI step after the gate, as described above.
- **Replace JavaFaker with Datafaker.**

### Quick facts to remember

| Item | Value |
|---|---|
| Test cases | 22 (13 UI, 9 API), 26 scenario executions with outlines expanded |
| Default threads | 2 (valid range 1–16) |
| Crash retries | 3, always |
| Normal retries | off by default; `-DisRetry=true -DretryCount=N` |
| Gate | `scripts/verify_results.py` |
| Main reports | `target/cucumber-reports/cucumber.json`, `target/extent-report/SparkReport.html`, `target/cucumber-html-report/` |
