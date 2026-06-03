package io.github.repopulse;

import io.github.repopulse.model.FileMetrics;
import io.github.repopulse.model.Finding;
import io.github.repopulse.model.RepoMetrics;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Walks a repository tree, runs {@link JavaFileAnalyzer} on every {@code .java}
 * file, detects onboarding signals, and turns the aggregate metrics into a set
 * of actionable {@link Finding}s.
 */
public final class RepoScanner {

    private final JavaFileAnalyzer analyzer = new JavaFileAnalyzer();

    public RepoMetrics scan(Path root) {
        RepoMetrics metrics = new RepoMetrics();
        Path abs = root.toAbsolutePath().normalize();
        metrics.setRootName(abs.getFileName() == null ? "repo" : abs.getFileName().toString());

        detectOnboardingSignals(root, metrics);

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !isIgnored(root.relativize(p)))
                    .forEach(p -> {
                        try {
                            String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                            String rel = root.relativize(p).toString().replace('\\', '/');
                            metrics.add(analyzer.analyze(rel, content));
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        deriveFindings(metrics);
        return metrics;
    }

    private boolean isIgnored(Path relative) {
        String s = relative.toString().replace('\\', '/');
        return s.startsWith("target/") || s.startsWith("build/")
                || s.contains("/target/") || s.contains("/build/")
                || s.contains("/.git/") || s.startsWith(".git/");
    }

    private void detectOnboardingSignals(Path root, RepoMetrics m) {
        boolean maven = Files.exists(root.resolve("pom.xml"));
        boolean gradle = Files.exists(root.resolve("build.gradle"))
                || Files.exists(root.resolve("build.gradle.kts"));
        if (maven && gradle) {
            m.setBuildTool("Maven + Gradle");
        } else if (maven) {
            m.setBuildTool("Maven");
        } else if (gradle) {
            m.setBuildTool("Gradle");
        } else {
            m.setBuildTool("unknown");
        }

        m.setHasReadme(exists(root, "README.md") || exists(root, "README") || exists(root, "README.txt"));
        m.setHasGitignore(exists(root, ".gitignore"));
        m.setHasLicense(exists(root, "LICENSE") || exists(root, "LICENSE.md") || exists(root, "LICENSE.txt"));
        m.setHasCi(Files.isDirectory(root.resolve(".github").resolve("workflows")));
        m.setHasTests(Files.isDirectory(root.resolve("src").resolve("test")));
    }

    private boolean exists(Path root, String name) {
        return Files.exists(root.resolve(name));
    }

    private void deriveFindings(RepoMetrics m) {
        if (m.recordCandidateCount() > 0) {
            m.addFinding(new Finding(Finding.Severity.SUGGESTION, "Boilerplate",
                    m.recordCandidateCount() + " class(es) look like plain data carriers. "
                            + "Consider Java records (16+) or Lombok to remove getter/setter boilerplate.",
                    "project-wide"));
        }
        if (m.getterSetterCount() > 0 && m.codeLines() > 0) {
            double ratio = (double) m.getterSetterCount() / m.codeLines() * 100.0;
            if (ratio > 8.0) {
                m.addFinding(new Finding(Finding.Severity.INFO, "Boilerplate",
                        String.format("Getters/setters account for roughly %.0f%% of code lines.", ratio),
                        "project-wide"));
            }
        }
        if (!m.hasReadme()) {
            m.addFinding(new Finding(Finding.Severity.WARNING, "Onboarding",
                    "No README found. New contributors have no starting point.", "repo root"));
        }
        if (!m.hasTests()) {
            m.addFinding(new Finding(Finding.Severity.WARNING, "Quality",
                    "No src/test directory found. Add automated tests.", "repo root"));
        }
        if (!m.hasCi()) {
            m.addFinding(new Finding(Finding.Severity.SUGGESTION, "Onboarding",
                    "No CI workflows under .github/workflows.", "repo root"));
        }
        if ("unknown".equals(m.buildTool())) {
            m.addFinding(new Finding(Finding.Severity.WARNING, "Onboarding",
                    "No recognised build tool (pom.xml / build.gradle). Builds are not reproducible.",
                    "repo root"));
        }
        if (m.todoCount() > 0) {
            m.addFinding(new Finding(Finding.Severity.INFO, "Maintenance",
                    m.todoCount() + " TODO/FIXME marker(s) across the codebase.", "project-wide"));
        }
        // Per-file: equals without hashCode (a classic correctness bug).
        for (FileMetrics f : m.files()) {
            if (f.hasEquals() != f.hasHashCode()) {
                m.addFinding(new Finding(Finding.Severity.WARNING, "Correctness",
                        "Overrides equals() or hashCode() but not both.", f.path()));
            }
        }
    }
}
