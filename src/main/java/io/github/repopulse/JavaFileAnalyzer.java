package io.github.repopulse;

import io.github.repopulse.model.FileMetrics;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Analyzes the textual content of a single {@code .java} file using lightweight
 * heuristics (no full parser). The goal is fast, dependency-free signal on the
 * boilerplate that Java developers most often complain about.
 */
public final class JavaFileAnalyzer {

    private static final Pattern TYPE_DECL =
            Pattern.compile("\\b(class|interface|enum|record)\\s+[A-Z][A-Za-z0-9_]*");
    private static final Pattern GETTER =
            Pattern.compile("\\bpublic\\s+[\\w<>\\[\\],.?\\s]+\\sget[A-Z]\\w*\\s*\\(\\s*\\)");
    private static final Pattern BOOL_GETTER =
            Pattern.compile("\\bpublic\\s+boolean\\s+is[A-Z]\\w*\\s*\\(\\s*\\)");
    private static final Pattern SETTER =
            Pattern.compile("\\bpublic\\s+void\\s+set[A-Z]\\w*\\s*\\(");
    private static final Pattern EQUALS =
            Pattern.compile("\\bpublic\\s+boolean\\s+equals\\s*\\(\\s*Object\\s");
    private static final Pattern HASHCODE =
            Pattern.compile("\\bpublic\\s+int\\s+hashCode\\s*\\(\\s*\\)");
    private static final Pattern TODO =
            Pattern.compile("(//|/\\*|\\*).*(TODO|FIXME|XXX)");
    private static final Pattern PRIVATE_FIELD =
            Pattern.compile("\\bprivate\\s+(final\\s+)?[\\w<>\\[\\],.?]+\\s+\\w+\\s*(=|;)");

    /**
     * @param path    repo-relative path, used purely for reporting
     * @param content full file text
     */
    public FileMetrics analyze(String path, String content) {
        String[] lines = content.split("\n", -1);
        int total = lines.length;
        int code = 0;
        boolean inBlockComment = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            if (inBlockComment) {
                if (line.contains("*/")) inBlockComment = false;
                continue;
            }
            if (line.startsWith("//")) continue;
            if (line.startsWith("/*")) {
                if (!line.contains("*/")) inBlockComment = true;
                continue;
            }
            if (line.startsWith("*")) continue;
            code++;
        }

        int types = countMatches(TYPE_DECL, content);
        int getters = countMatches(GETTER, content) + countMatches(BOOL_GETTER, content);
        int setters = countMatches(SETTER, content);
        int getterSetters = getters + setters;
        int todos = 0;
        for (String raw : lines) {
            if (TODO.matcher(raw).find()) todos++;
        }
        boolean hasEquals = EQUALS.matcher(content).find();
        boolean hasHashCode = HASHCODE.matcher(content).find();

        boolean recordCandidate = isRecordCandidate(content, getterSetters);

        return new FileMetrics(path, total, code, types, getterSetters,
                todos, hasEquals, hasHashCode, recordCandidate);
    }

    /**
     * A "record candidate" is a plain data carrier: it declares several private
     * fields and exposes them through getters/setters, but is not already a
     * record and has little other behaviour. Converting it to a {@code record}
     * (Java 16+) or annotating with Lombok removes the boilerplate.
     */
    private boolean isRecordCandidate(String content, int getterSetters) {
        if (content.contains(" record ") || content.startsWith("record ")) {
            return false;
        }
        int fields = countMatches(PRIVATE_FIELD, content);
        int publicMethods = countMatches(
                Pattern.compile("\\bpublic\\s+[\\w<>\\[\\],.?\\s]+\\s+\\w+\\s*\\("), content);
        // Heuristic: at least two fields, getters/setters dominate the public API.
        return fields >= 2 && getterSetters >= 2 && getterSetters >= publicMethods - 1;
    }

    private int countMatches(Pattern p, String content) {
        java.util.regex.Matcher m = p.matcher(content);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    /** Convenience for callers that already have the lines split. */
    public FileMetrics analyzeLines(String path, List<String> lines) {
        return analyze(path, String.join("\n", lines));
    }
}
