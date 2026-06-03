package io.github.repopulse;

import io.github.repopulse.model.RepoMetrics;
import io.github.repopulse.report.JsonReport;
import io.github.repopulse.report.MarkdownReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-line entry point.
 *
 * <pre>
 *   java -jar repopulse.jar [repoPath] [-o file] [--format markdown|json] [--fail-under N]
 * </pre>
 *
 * Settings resolve in this order: built-in defaults, then {@code .repopulse.yml}
 * at the repo root, then command-line flags (which win). Defaults: scan the
 * current directory and write {@code REPORT.md} as Markdown.
 */
public final class RepoPulse {

    private static final String VERSION = "1.1.0";

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(".");
        String outputOverride = null;
        String formatOverride = null;
        Integer failUnderOverride = null;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("-h") || a.equals("--help")) {
                printHelp();
                return;
            } else if (a.equals("-v") || a.equals("--version")) {
                System.out.println("RepoPulse " + VERSION);
                return;
            } else if ((a.equals("-o") || a.equals("--output")) && i + 1 < args.length) {
                outputOverride = args[++i];
            } else if (a.equals("--format") && i + 1 < args.length) {
                formatOverride = args[++i].toLowerCase();
            } else if (a.equals("--fail-under") && i + 1 < args.length) {
                failUnderOverride = parseIntOrExit(args[++i]);
            } else if (!a.startsWith("-")) {
                root = Paths.get(a);
            } else {
                System.err.println("warning: unknown option '" + a + "'");
            }
        }

        if (!Files.isDirectory(root)) {
            System.err.println("error: not a directory: " + root);
            System.exit(2);
        }

        // Defaults <- .repopulse.yml <- CLI flags.
        RepoPulseConfig config = RepoPulseConfig.load(root);
        if (outputOverride != null) {
            config.setOutput(outputOverride);
        }
        if (formatOverride != null) {
            config.setFormat(formatOverride);
        }
        if (failUnderOverride != null) {
            config.setFailUnder(failUnderOverride);
        }

        RepoMetrics metrics = new RepoScanner().scan(root, config);

        String report = config.isJson()
                ? new JsonReport().render(metrics)
                : new MarkdownReport().render(metrics);
        Path output = Paths.get(config.output());
        Files.write(output, report.getBytes(StandardCharsets.UTF_8));

        System.out.printf("RepoPulse %s: scanned %d Java files (%d LOC) in '%s'.%n",
                VERSION, metrics.javaFileCount(), metrics.codeLines(), metrics.rootName());
        System.out.printf("Onboarding readiness: %d/100. %s report written to %s.%n",
                metrics.onboardingScore(),
                config.isJson() ? "JSON" : "Markdown", output);

        if (config.hasFailGate() && metrics.onboardingScore() < config.failUnder()) {
            System.err.printf("FAIL: onboarding score %d is below threshold %d.%n",
                    metrics.onboardingScore(), config.failUnder());
            System.exit(1);
        }
    }

    private static int parseIntOrExit(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            System.err.println("error: --fail-under expects an integer, got '" + s + "'");
            System.exit(2);
            return -1; // unreachable
        }
    }

    private static void printHelp() {
        System.out.println("RepoPulse " + VERSION + " - Java repo health & boilerplate scanner");
        System.out.println();
        System.out.println("Usage: java -jar repopulse.jar [repoPath] [options]");
        System.out.println();
        System.out.println("  repoPath              directory to scan (default: .)");
        System.out.println("  -o, --output <file>   report file to write (default: REPORT.md)");
        System.out.println("      --format <fmt>    markdown (default) or json");
        System.out.println("      --fail-under <N>  exit 1 if onboarding score is below N (CI gate)");
        System.out.println("  -v, --version         print version and exit");
        System.out.println("  -h, --help            print this help and exit");
        System.out.println();
        System.out.println("Config: a .repopulse.yml at the repo root can set output, format,");
        System.out.println("failUnder, and an exclude list. Command-line flags override the file.");
    }

    private RepoPulse() {
    }
}
