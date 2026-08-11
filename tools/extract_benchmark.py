#!/usr/bin/env python3
"""Convert a Relay benchmark JSON file into a readable markdown report."""

import argparse
import json
import sys
from pathlib import Path


def ms(value_ns: int) -> str:
    return f"{value_ns / 1_000_000:.1f}"


def main() -> int:
    parser = argparse.ArgumentParser(description="Summarize a Relay benchmark JSON file.")
    parser.add_argument("benchmark_json", type=Path, help="Path to relay_benchmark_*.json")
    parser.add_argument("-o", "--output", type=Path, default=None, help="Optional output markdown file")
    args = parser.parse_args()

    data = json.loads(args.benchmark_json.read_text())

    lines: list[str] = []
    lines.append("# Relay benchmark report")
    lines.append("")
    lines.append(f"- **Commit:** `{data.get('commit', 'unknown')}`")
    lines.append(f"- **Build type:** `{data.get('buildType', 'unknown')}`")
    lines.append(f"- **Pipeline:** `{data.get('pipelineImplementation', 'unknown')}`")
    lines.append(f"- **Corpus:** `{data.get('corpusId', 'unknown')}`")
    lines.append(f"- **Warm run:** {data.get('isWarm', False)}")
    lines.append(f"- **Completed/cancelled turns:** {data.get('sampleCount', 0)}")
    lines.append(f"- **Failures:** {data.get('failures', 0)}")
    lines.append("")

    profile = data.get("deviceProfile", {})
    lines.append("## Device profile")
    lines.append("")
    lines.append(f"- **Model:** {profile.get('model', 'unknown')}")
    lines.append(f"- **SoC:** {profile.get('soc', 'unknown')}")
    lines.append(f"- **Android version:** {profile.get('androidVersion', 'unknown')}")
    free_mem = profile.get('freeMemoryBytes')
    if free_mem:
        lines.append(f"- **Free memory at export:** {free_mem / (1024 * 1024):.1f} MB")
    lines.append("")

    models = data.get("modelVersions", {})
    lines.append("## Model versions")
    lines.append("")
    if models:
        for name, version in models.items():
            lines.append(f"- **{name}:** {version}")
    else:
        lines.append("_No model versions recorded._")
    lines.append("")

    summaries = data.get("summaries", [])
    lines.append("## Stage latencies")
    lines.append("")
    lines.append("| Stage | p50 (ms) | p95 (ms) | Max (ms) | Samples |")
    lines.append("| --- | ---: | ---: | ---: | ---: |")
    for summary in summaries:
        lines.append(
            f"| {summary['stage']} | {summary['p50Ms']} | {summary['p95Ms']} | {summary['maxMs']} | {summary['sampleCount']} |"
        )
    lines.append("")

    events = data.get("events", [])
    lines.append("## Raw events")
    lines.append("")
    lines.append("| Timestamp (ns) | Stage |")
    lines.append("| ---: | --- |")
    for event in events:
        lines.append(f"| {event['timestampNs']} | {event['stage']} |")
    lines.append("")

    report = "\n".join(lines)
    if args.output:
        args.output.write_text(report)
        print(f"Report written to {args.output}")
    else:
        print(report)

    return 0


if __name__ == "__main__":
    sys.exit(main())
