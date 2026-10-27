#!/usr/bin/env python3
"""Quality gate for CI. Fails (exit 1) when:
  - target/cucumber-reports/cucumber.json is missing, empty, "[]" or contains no scenarios
    (i.e. nothing actually ran - a silent green build is the worst kind of failure), or
  - TestNG recorded failures (testng-results.xml). Retried attempts count as SKIP there,
    so a scenario that passed on retry does not fail the build, and one that never passed does.

Run after `mvn test -DtestFailureIgnore=true` so reports are always generated first.
"""
from __future__ import annotations

import json
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

CUCUMBER_JSON = Path("target/cucumber-reports/cucumber.json")
TESTNG_XML = Path("target/surefire-reports/testng-results.xml")


def main() -> int:
    problems: list[str] = []

    if not CUCUMBER_JSON.is_file():
        problems.append(f"{CUCUMBER_JSON} is missing - did the test run start at all?")
    else:
        text = CUCUMBER_JSON.read_text(encoding="utf-8").strip()
        if not text or text == "[]":
            problems.append(f"{CUCUMBER_JSON} is empty ('{text}') - no scenario was executed")
        else:
            try:
                features = json.loads(text)
                scenarios = sum(1 for f in features for e in f.get("elements", []) if e.get("type") == "scenario")
                print(f"cucumber.json: {len(features)} feature(s), {scenarios} scenario execution(s)")
                if scenarios == 0:
                    problems.append(f"{CUCUMBER_JSON} contains no scenarios")
            except (json.JSONDecodeError, AttributeError, TypeError) as error:
                problems.append(f"{CUCUMBER_JSON} is not valid Cucumber JSON: {error}")

    if not TESTNG_XML.is_file():
        problems.append(f"{TESTNG_XML} is missing")
    else:
        root = ET.parse(TESTNG_XML).getroot()
        total, failed, skipped = (int(root.get(k, "0")) for k in ("total", "failed", "skipped"))
        print(f"TestNG: total={total} failed={failed} skipped(incl. retried attempts)={skipped}")
        if failed:
            problems.append(f"{failed} scenario(s) failed - see target/failure-summary.csv")

    for problem in problems:
        print(f"GATE FAILED: {problem}", file=sys.stderr)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
