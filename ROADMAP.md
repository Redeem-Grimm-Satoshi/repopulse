# RepoPulse Roadmap (6 months)

A phased plan to grow RepoPulse from a single-file heuristic scanner into a
genuinely useful Java repo-health tool. Each phase lists concrete, shippable
features. Commits should map to real units of work (a feature, a test, a fix) —
not an artificial daily quota.

Status legend: [ ] planned · [~] in progress · [x] done

---

## Month 1 — Configuration & CI gating

Make the tool usable in real pipelines instead of just printing a report.

- [x] `.repopulse.yml` config file: include/exclude globs, thresholds, output path.
- [x] `--format json` in addition to Markdown, for machine consumption.
- [x] Exit codes: fail the build when the onboarding score or a metric crosses a configured threshold (`--fail-under 70`).
- [ ] `--quiet` / `--verbose` logging levels.
- [x] Unit tests for config parsing and threshold logic; raise coverage above 80%.

**Milestone:** RepoPulse can gate a CI build and be configured per-repo.

## Month 2 — Real parsing, deeper analysis

Replace regex heuristics with a proper parser for accuracy.

- [ ] Integrate JavaParser; build an AST-based analyzer alongside the regex fallback.
- [ ] Cyclomatic complexity per method; flag methods above a threshold.
- [ ] Detect long methods, long parameter lists, and "god classes".
- [ ] Smarter record/Lombok candidate detection using the AST (fields + accessors + no behaviour).
- [ ] Detect manual DTO/entity mapping boilerplate.

**Milestone:** Findings are accurate enough to trust on a real codebase.

## Month 3 — Dependencies & multi-module

- [ ] Parse `pom.xml` dependencies; query Maven Central for newer versions (freshness report).
- [ ] Flag dependencies pinned to known-vulnerable versions (OSV index).
- [ ] Gradle (`build.gradle`, `build.gradle.kts`) build-file parsing.
- [ ] Multi-module aggregation: per-module breakdown plus a repo rollup.

**Milestone:** Works on multi-module enterprise projects, surfaces dependency risk.

## Month 4 — Reporting & trends

- [ ] HTML report with charts (score over time, boilerplate by package).
- [ ] Historical metrics store (`.repopulse/history.json`) and deltas vs. last run.
- [ ] Health badge (shields.io endpoint JSON) for READMEs.
- [ ] SARIF output so findings show up in the GitHub "Security / Code scanning" tab.

**Milestone:** Teams can track repo health trends, not just a snapshot.

## Month 5 — Integrations & developer experience

- [ ] Publish a composite GitHub Action to the Marketplace.
- [ ] PR comment bot: post the report diff as a comment on pull requests.
- [ ] Pre-commit hook mode (`repopulse --hook`).
- [ ] Editor task recipes (IntelliJ external tool, VS Code task).

**Milestone:** RepoPulse fits naturally into existing Java workflows.

## Month 6 — Packaging, performance, 1.0

- [ ] GraalVM native image for sub-100ms startup.
- [ ] Distribution via SDKMAN! and Homebrew.
- [ ] Full test suite, CI matrix (JDK 17/21), coverage gate.
- [ ] Documentation site and `CONTRIBUTING.md`.
- [ ] Tag and release **v1.0.0**.

**Milestone:** A polished, installable 1.0 anyone can adopt.

---

## How progress is tracked

Each checkbox is roughly one focused pull request or a small group of commits.
The existing Daily Pulse workflow keeps `REPORT.md` current; real progress shows
up as feature commits against the milestones above. Quality over volume.
