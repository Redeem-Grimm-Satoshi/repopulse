package io.github.repopulse;

import io.github.repopulse.model.RepoMetrics;
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
 *   java -jar repopulse.jar [repoPath] [-o outputFile]
 * </pre>
 *
 * Defaults: scans the current directory and writes {@code REPORT.md}.
 */
public final class RepoPulse {

    private static final String VERSION = "1.0.0";

    public static void main(String[] args) throws IOException {
        Path root = Paths.get(".");
        Path output = Paths.get("REPORT.md");

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("-h") || a.equals("--help")) {
                printHelp();
                return;
            } else if (a.equals("-v") || a.equals("--version")) {
                System.out.println("RepoPulse " + VERSION);
                return;
            } else if ((a.equals("-o") || a.equals("--output")) && i + 1 < args.length) {
                output = Paths.get(args[++i]);
            } else if (!a.startsWith("-")) {
                root = Paths.get(a);
            }
        }

        if (!Files.isDirectory(root)) {
            System.err.println("error: not a directory: " + root);
            System.exit(2);
        }

        RepoMetrics metrics = new RepoScanner().scan(root);
        String report = new MarkdownReport().render(metrics);
        Files.write(output, report.getBytes(StandardCharsets.UTF_8));

        System.out.printf("RepoPulse %s: scanned %d Java files (%d LOC) in '%s'.%n",
                VERSION, metrics.javaFileCount(), metrics.codeLines(), metrics.rootName());
        System.out.printf("Onboarding readiness: %d/100. Report written to %s.%n",
                metrics.onboardingScore(), output);
    }

    private static void printHelp() {
        System.out.println("RepoPulse " + VERSION + " - Java repo health & boilerplate scanner");
        System.out.println();
        System.out.println("Usage: java -jar repopulse.jar [repoPath] [-o outputFile]");
        System.out.println();
        System.out.println("  repoPath          directory to scan (default: .)");
        System.out.println("  -o, --output      report file to write (default: REPORT.md)");
        System.out.println("  -v, --version     print version and exit");
        System.out.println("  -h, --help        print this help and exit");
    }

    private RepoPulse() {
    }
}
