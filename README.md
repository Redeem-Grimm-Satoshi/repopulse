# RepoPulse

A zero-dependency Java CLI that scans a repository and produces a **Repo Health Report** — focused on the things Java developers actually complain about: boilerplate, missing onboarding signals, and creeping maintenance debt.

It runs on plain JDK (no third-party runtime dependencies), so it's fast, portable, and trivial to drop into CI.

## Why

The JetBrains *State of Developer Ecosystem 2025* and Stack Overflow surveys consistently put **repetitive, low-value work** at the top of developer frustrations, with getters/setters, `equals`/`hashCode`, and DTO mapping cited as the most tedious Java boilerplate. RepoPulse surfaces exactly that boilerplate, plus the onboarding gaps that slow new contributors down.

## What it reports

- **Onboarding readiness score** — build tool, README, tests, CI, `.gitignore`, LICENSE.
- **Boilerplate hotspots** — files heaviest in getters/setters; flags plain data carriers that could become Java `record`s (16+) or use Lombok.
- **Correctness checks** — classes overriding `equals()` without `hashCode()` (or vice versa).
- **Metrics** — files, lines of code, type count, TODO/FIXME markers, largest files.

The report is written to `REPORT.md` and is regenerated daily by a GitHub Action (see `.github/workflows/daily-pulse.yml`).

## Build & run

```bash
mvn -B package
java -jar target/repopulse.jar .            # scan current repo, write REPORT.md
java -jar target/repopulse.jar ../some-repo -o HEALTH.md
java -jar target/repopulse.jar --help
```

## Usage

```
java -jar repopulse.jar [repoPath] [options]

  repoPath              directory to scan (default: .)
  -o, --output <file>   report file to write (default: REPORT.md)
      --format <fmt>    markdown (default) or json
      --fail-under <N>  exit 1 if onboarding score is below N (CI gate)
  -v, --version         print version and exit
  -h, --help            print help and exit
```

### Configuration (`.repopulse.yml`)

Drop a `.repopulse.yml` at the repo root to set defaults. Command-line flags
override it.

```yaml
output: REPORT.md
format: markdown        # markdown | json
failUnder: 70           # fail the run if onboarding score is below this
exclude:
  - generated/          # path fragments or globs (e.g. *Generated.java)
  - legacy/
```

### CI gating example

```bash
# Fail the pipeline if repo health regresses below 70/100
java -jar target/repopulse.jar . --fail-under 70

# Emit machine-readable JSON for a dashboard
java -jar target/repopulse.jar . --format json -o health.json
```

## Project layout

```
repopulse/
├── pom.xml
├── src/
│   ├── main/java/io/github/repopulse/
│   │   ├── RepoPulse.java            # CLI entry point
│   │   ├── RepoScanner.java          # walks the tree, derives findings
│   │   ├── JavaFileAnalyzer.java     # per-file heuristics
│   │   ├── model/                    # Finding, FileMetrics, RepoMetrics
│   │   └── report/MarkdownReport.java
│   └── test/java/...                 # JUnit 5 tests
└── .github/workflows/
    ├── ci.yml                        # build + test on every push/PR
    └── daily-pulse.yml               # scheduled daily report + commit
```

## How the daily report works

`daily-pulse.yml` runs on a cron schedule, builds the tool, runs it against this repo, and commits the refreshed `REPORT.md` if anything changed. Because the report reflects the real state of the code, the daily commit is meaningful rather than an empty placeholder.

## Roadmap

- Per-module breakdown for multi-module Maven/Gradle builds.
- JSON output for machine consumption.
- Configurable thresholds via `.repopulse.toml`.
- Optional auto-conversion of record candidates.

## License

MIT — see [LICENSE](LICENSE).
