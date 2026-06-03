package io.github.repopulse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a RepoPulse run, optionally loaded from a {@code .repopulse.yml}
 * file at the repository root. To keep the tool dependency-free, this parses a
 * deliberately small subset of YAML: top-level {@code key: value} scalars and
 * simple {@code - item} lists. Comments ({@code #}) and blank lines are ignored.
 *
 * <p>Example:</p>
 * <pre>
 * output: REPORT.md
 * format: markdown      # markdown | json
 * failUnder: 70         # fail the run if onboarding score is below this
 * exclude:
 *   - generated/
 *   - legacy/
 * </pre>
 */
public final class RepoPulseConfig {

    public static final String DEFAULT_OUTPUT = "REPORT.md";

    private String output = DEFAULT_OUTPUT;
    private String format = "markdown";
    private int failUnder = -1; // -1 disables the gate
    private final List<String> exclude = new ArrayList<>();

    /** Loads {@code <root>/.repopulse.yml} if present, else returns defaults. */
    public static RepoPulseConfig load(Path root) {
        Path file = root.resolve(".repopulse.yml");
        if (!Files.isRegularFile(file)) {
            return new RepoPulseConfig();
        }
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            return parse(content);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Parses the supported YAML subset from raw text. */
    public static RepoPulseConfig parse(String content) {
        RepoPulseConfig cfg = new RepoPulseConfig();
        String currentListKey = null;
        for (String raw : content.split("\n", -1)) {
            String line = stripComment(raw);
            if (line.trim().isEmpty()) {
                continue;
            }

            boolean indented = line.startsWith(" ") || line.startsWith("\t");
            String trimmed = line.trim();

            if (indented && trimmed.startsWith("-")) {
                String item = trimmed.substring(1).trim();
                if (!item.isEmpty() && "exclude".equals(currentListKey)) {
                    cfg.exclude.add(unquote(item));
                }
                continue;
            }

            int colon = trimmed.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = trimmed.substring(0, colon).trim();
            String value = trimmed.substring(colon + 1).trim();

            if (value.isEmpty()) {
                // Start of a list block (e.g. "exclude:").
                currentListKey = key;
                continue;
            }
            currentListKey = null;

            switch (key) {
                case "output":
                    cfg.output = unquote(value);
                    break;
                case "format":
                    cfg.format = unquote(value).toLowerCase();
                    break;
                case "failUnder":
                case "fail_under":
                    cfg.failUnder = parseIntSafe(value, -1);
                    break;
                case "exclude":
                    // Inline comma list: exclude: a, b, c
                    for (String part : unquote(value).split(",")) {
                        String p = part.trim();
                        if (!p.isEmpty()) cfg.exclude.add(p);
                    }
                    break;
                default:
                    // Unknown keys are ignored for forward-compatibility.
                    break;
            }
        }
        return cfg;
    }

    private static String stripComment(String line) {
        int hash = line.indexOf('#');
        return hash >= 0 ? line.substring(0, hash) : line;
    }

    private static String unquote(String s) {
        if (s.length() >= 2
                && ((s.startsWith("\"") && s.endsWith("\""))
                || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public String output() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String format() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format == null ? "markdown" : format.toLowerCase();
    }

    public boolean isJson() {
        return "json".equals(format);
    }

    public int failUnder() {
        return failUnder;
    }

    public void setFailUnder(int failUnder) {
        this.failUnder = failUnder;
    }

    public boolean hasFailGate() {
        return failUnder >= 0;
    }

    public List<String> exclude() {
        return exclude;
    }

    /** True if the given repo-relative path matches any configured exclude fragment. */
    public boolean isExcluded(String relativePath) {
        String normalized = relativePath.replace('\\', '/');
        for (String fragment : exclude) {
            if (fragment.isEmpty()) {
                continue;
            }
            if (matchesGlob(normalized, fragment.replace('\\', '/'))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lightweight glob match supporting {@code *} (any run of non-slash chars)
     * and plain substring fragments (e.g. {@code "generated/"}). A fragment with
     * no wildcard matches if it appears anywhere in the path.
     */
    private static boolean matchesGlob(String path, String fragment) {
        if (fragment.indexOf('*') < 0) {
            return path.contains(fragment);
        }
        String regex = globToRegex(fragment);
        return path.matches(regex) || path.matches(".*/" + regex + "(/.*)?");
    }

    private static String globToRegex(String glob) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*':
                    sb.append("[^/]*");
                    break;
                case '.': case '(': case ')': case '+': case '|':
                case '^': case '$': case '@': case '%': case '\\':
                case '{': case '}': case '[': case ']': case '?':
                    sb.append('\\').append(c);
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
