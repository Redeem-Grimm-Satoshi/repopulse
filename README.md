# RepoPulse

A zero-dependency Java CLI that scans a repository and produces a **Repo Health Report** — focused on the things Java developers actually complain about: boilerplate, missing onboarding signals, and creeping maintenance debt.

It runs on plain JDK (no third-party runtime dependencies), so it's fast, portable, and trivial to drop into CI.

## Why

The JetBrains *State of Developer Ecosystem 2025* and Stack Overflow surveys consistently put **repetitive, low-value work** at the top of developer frustrations, with getters/setters, `equals`/`hashCode`, and DTO mapping cited as the most tedious Java boilerplate. RepoPulse surfaces exactly that boilerplate, plus the onboarding gaps that slow new contributors down.

## How it helps Java developers

RepoPulse targets the day-to-day friction Java developers know well:

- **Catch boilerplate before it spreads.** It flags getter/setter-heavy data classes and highlights the ones that could collapse into a Java `record` (16+) or a Lombok-annotated type — turning "this file is 90% ceremony" into a concrete, actionable list.
- **Onboard to a repo faster.** A single readiness score and checklist (build tool, README, tests, CI, `.gitignore`, license) tells a new contributor — or you, six months later — whether a project is set up to be productive in minutes or hours.
- **Guard correctness.** It surfaces classic Java footguns such as a class overriding `equals()` but not `hashCode()`, which silently breaks hash-based collections like `HashMap` and `HashSet`.
- **Enforce standards in CI.** `--fail-under` fails a pull request when repo health regresses, and `--format json` feeds dashboards — so conventions are checked automatically instead of in review comments.
- **Add zero weight to your build.** It runs on a plain JDK with no third-party runtime dependencies, so it pulls nothing into your dependency tree and starts instantly.

Whether you're maintaining a large legacy service or starting a greenfield project, RepoPulse gives an at-a-glance, trackable read on code health that's specific to Java conventions.

## What it reports

- **Onboarding readiness score** — build tool, README, tests, CI, `.gitignore`, LICENSE.
- **Boilerplate hotspots** — files heaviest in getters/setters; flags plain data carriers that could become Java `record`s (16+) or use Lombok.
- **Correctness checks** — classes overriding `equals()` without `hashCode()` (or vice versa).
- **Metrics** — files, lines of code, type count, TODO/FIXME markers, largest files.

The report is written to `REPORT.md` and is regenerated daily by a GitHub Action (see `.github/workflows/daily-pulse.yml`).

## Getting started — scan your own repository

RepoPulse scans **any** Java project, not just this one. There's nothing to
install on your project; you build the jar once and point it at your repo.

**Prerequisites:** JDK 17+ (Maven is optional — it's only used to build the jar).

1. Clone this repository and build the runnable jar:

   ```bash
   git clone https://github.com/Redeem-Grimm-Satoshi/repopulse.git
   cd repopulse
   mvn -B package
   ```

   This produces `target/repopulse.jar` — a single, self-contained file you can copy anywhere.

2. Run it against **your own** project, passing that project's path:

   ```bash
   java -jar target/repopulse.jar /path/to/your/java/project
   ```

   On Windows, use the full path, e.g. `java -jar target/repopulse.jar C:\dev\my-service`.

3. Open the generated `REPORT.md` inside your project's folder — that's your health report.

Common variations:

```bash
# Write the report somewhere specific
java -jar target/repopulse.jar /path/to/project -o health-report.md

# Machine-readable JSON for dashboards or scripts
java -jar target/repopulse.jar /path/to/project --format json -o health.json

# Fail with a non-zero exit code if health is below 70/100 (handy in CI)
java -jar target/repopulse.jar /path/to/project --fail-under 70

# See all options
java -jar target/repopulse.jar --help
```

No Maven? You can build with the JDK alone: compile `src/main/java` with `javac`
and run the `io.github.repopulse.RepoPulse` class directly.

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

See [ROADMAP.md](ROADMAP.md) for the full 6-month plan. Highlights still ahead:
AST-based analysis (JavaParser), dependency-freshness checks, multi-module
support, an HTML report with trends, and a GitHub Marketplace action.

## License

MIT — see [LICENSE](LICENSE).
