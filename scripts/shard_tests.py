#!/usr/bin/env python3
"""Build a GitHub Actions matrix that shards test-case tags across N parallel jobs.

Modes
  regression  every @regression scenario in the .feature files
  custom      the tags given with --tags (TC ids are used as-is; any other tag, e.g. @smoke,
              is expanded to the TC ids of the scenarios carrying it)
  failed      only the scenarios that failed in a previous run, read from --results:
              a Cucumber rerun.txt, a TestNG testng-results.xml, or a directory searched
              recursively for those files (e.g. downloaded CI artifacts).
              Exits with code 2 and a clear message when nothing can be found.

Output
  JSON on stdout: {"include": [{"shard": 1, "featureToRun": "+TC-UI-001,+TC-API-004"}, ...]}
  With --github-output (or $GITHUB_OUTPUT set) it also appends:  matrix=<json>  count=<n>
  Each featureToRun value plugs straight into:  mvn test -DfeatureToRun="<value>"

Examples
  python3 scripts/shard_tests.py --mode regression --shards 4
  python3 scripts/shard_tests.py --mode custom --tags "@smoke,+TC-API-009" --shards 2
  python3 scripts/shard_tests.py --mode failed --results target/rerun.txt --shards 2
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

TC_TAG = re.compile(r"^@TC-[A-Z]+-\d+$", re.IGNORECASE)
SCENARIO_LINE = re.compile(r"^\s*(Scenario|Scenario Outline|Scenario Template|Example):\s*(.*)$")
RERUN_ENTRY = re.compile(r"^(?:file:|classpath:)?(?P<path>.+?\.feature)(?P<lines>(?::\d+)+)$")


@dataclass
class ScenarioInfo:
    path: Path
    name: str
    tags: set[str]
    lines: set[int] = field(default_factory=set)  # scenario line + example row lines (what rerun.txt lists)

    @property
    def tc_ids(self) -> list[str]:
        return sorted(t[1:].upper() for t in self.tags if TC_TAG.match(t))


def fail(message: str) -> None:
    print(f"ERROR: {message}", file=sys.stderr)
    sys.exit(2)


def parse_features(features_dir: Path) -> list[ScenarioInfo]:
    """Small line-based Gherkin reader: enough for tags, names and example-row lines."""
    if not features_dir.is_dir():
        fail(f"features directory not found: {features_dir}")
    scenarios: list[ScenarioInfo] = []
    for path in sorted(features_dir.rglob("*.feature")):
        feature_tags: set[str] = set()
        pending: set[str] = set()
        current: ScenarioInfo | None = None
        in_examples = header_seen = False
        for number, raw in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
            line = raw.strip()
            if not line or line.startswith("#"):
                continue
            if line.startswith("@"):
                pending |= {t for t in line.split() if t.startswith("@")}
                continue
            if line.startswith("Feature:"):
                feature_tags, pending = pending, set()
                continue
            match = SCENARIO_LINE.match(line)
            if match:
                current = ScenarioInfo(path, match.group(2).strip(), feature_tags | pending, {number})
                scenarios.append(current)
                pending, in_examples, header_seen = set(), False, False
                continue
            if line.startswith(("Examples:", "Scenarios:")):
                pending, in_examples, header_seen = set(), True, False
                continue
            if line.startswith(("Background:", "Rule:")):
                current, pending, in_examples = None, set(), False
                continue
            if in_examples and current is not None and line.startswith("|"):
                if header_seen:
                    current.lines.add(number)
                header_seen = True
    return scenarios


def regression_tags(scenarios: list[ScenarioInfo]) -> list[str]:
    return [tc for s in scenarios if any(t.lower() == "@regression" for t in s.tags) for tc in s.tc_ids]


def custom_tags(scenarios: list[ScenarioInfo], raw: str) -> list[str]:
    result: list[str] = []
    for token in (t.strip() for t in raw.split(",")):
        if not token:
            continue
        tag = "@" + token.lstrip("+@")
        if TC_TAG.match(tag):
            result.append(tag[1:].upper())
            continue
        matches = [tc for s in scenarios if tag.lower() in {t.lower() for t in s.tags} for tc in s.tc_ids]
        if not matches:
            fail(f"tag '{token}' matches no scenario with a @TC-* id")
        result.extend(matches)
    if not result:
        fail("--tags is empty")
    return result


def failed_tags(scenarios: list[ScenarioInfo], results: Path) -> list[str]:
    if not results.exists():
        fail(f"results not found: {results}. In 'failed' mode pass the rerun.txt / testng-results.xml "
             "of a previous run (or the folder its CI artifacts were downloaded to).")
    files = [results] if results.is_file() else (
        sorted(results.rglob("rerun.txt")) or sorted(results.rglob("testng-results.xml")))
    if not files:
        fail(f"no rerun.txt or testng-results.xml found under {results}")
    tags: list[str] = []
    for file in files:
        tags.extend(from_testng_xml(scenarios, file) if file.suffix == ".xml" else from_rerun(scenarios, file))
    return tags


def feature_key(path: str) -> str:
    """Path below the 'features/' folder, so file:, classpath: and absolute URIs all compare equal."""
    posix = path.replace("\\", "/")
    return posix.split("features/", 1)[-1]


def from_rerun(scenarios: list[ScenarioInfo], rerun: Path) -> list[str]:
    tags: list[str] = []
    for token in rerun.read_text(encoding="utf-8").split():
        entry = RERUN_ENTRY.match(token)
        if not entry:
            print(f"WARNING: unrecognised rerun entry '{token}'", file=sys.stderr)
            continue
        key = feature_key(entry.group("path"))
        lines = {int(n) for n in entry.group("lines").strip(":").split(":")}
        found = [s for s in scenarios if feature_key(s.path.as_posix()) == key and s.lines & lines]
        if not found:
            print(f"WARNING: rerun entry {token} matches no scenario (feature files changed?)", file=sys.stderr)
        tags.extend(tc for s in found for tc in s.tc_ids)
    return tags


def from_testng_xml(scenarios: list[ScenarioInfo], xml_file: Path) -> list[str]:
    """Final failures only: retried attempts are recorded as SKIP by TestNG."""
    try:
        root = ET.parse(xml_file).getroot()
    except ET.ParseError as error:
        fail(f"cannot parse {xml_file}: {error}")
    names = set()
    for method in root.iter("test-method"):
        if method.get("status") == "FAIL" and method.get("name") == "runScenario":
            value = method.find("./params/param[@index='0']/value")
            if value is not None and value.text:
                names.add(value.text.strip().strip('"'))
    tags: list[str] = []
    for name in sorted(names):
        found = [s for s in scenarios if s.name == name or re.fullmatch(re.sub(r"<[^>]+>", ".+", re.escape(s.name)), name)]
        if not found:
            print(f"WARNING: failed scenario '{name}' not found in the feature files", file=sys.stderr)
        tags.extend(tc for s in found for tc in s.tc_ids)
    return tags


def natural_key(tag: str) -> tuple:
    return tuple(int(p) if p.isdigit() else p for p in re.split(r"(\d+)", tag))


def build_matrix(tags: list[str], shards: int) -> dict:
    unique = sorted(dict.fromkeys(tags), key=natural_key)  # de-duplicate, keep a stable order
    count = min(shards, len(unique))
    buckets: list[list[str]] = [[] for _ in range(count)]
    for index, tag in enumerate(unique):  # round-robin mixes UI and API ids so shards take similar time
        buckets[index % count].append(tag)
    return {"include": [{"shard": i + 1, "featureToRun": ",".join("+" + t for t in bucket)}
                        for i, bucket in enumerate(buckets)]}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--mode", choices=["regression", "custom", "failed"], default="regression")
    parser.add_argument("--shards", type=int, default=4)
    parser.add_argument("--tags", default="", help="custom mode: comma-separated tags, '+' or '@' prefix")
    parser.add_argument("--results", type=Path, default=Path("target/rerun.txt"), help="failed mode input")
    parser.add_argument("--features-dir", type=Path, default=Path("src/test/resources/features"))
    parser.add_argument("--github-output", default=os.environ.get("GITHUB_OUTPUT", ""),
                        help="file to append matrix=/count= to (default: $GITHUB_OUTPUT; empty = stdout only)")
    args = parser.parse_args()
    if args.shards < 1:
        fail("--shards must be >= 1")

    scenarios = parse_features(args.features_dir)
    if args.mode == "regression":
        tags = regression_tags(scenarios)
    elif args.mode == "custom":
        tags = custom_tags(scenarios, args.tags)
    else:
        tags = failed_tags(scenarios, args.results)

    if not tags and args.mode != "failed":
        fail(f"mode '{args.mode}' selected no @TC-* tags")
    matrix = build_matrix(tags, args.shards)
    if not tags:
        print("No failed scenarios found - nothing to re-run.", file=sys.stderr)

    payload = json.dumps(matrix, separators=(",", ":"))
    print(json.dumps(matrix, indent=2))
    if args.github_output:
        with open(args.github_output, "a", encoding="utf-8") as out:
            out.write(f"matrix={payload}\ncount={len(matrix['include'])}\n")


if __name__ == "__main__":
    main()
