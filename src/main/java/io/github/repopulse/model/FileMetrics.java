package io.github.repopulse.model;

/**
 * Per-file analysis result for a single {@code .java} source file.
 */
public final class FileMetrics {

    private final String path;
    private final int totalLines;
    private final int codeLines;
    private final int typeCount;
    private final int getterSetterCount;
    private final int todoCount;
    private final boolean hasEquals;
    private final boolean hasHashCode;
    private final boolean recordCandidate;

    public FileMetrics(String path, int totalLines, int codeLines, int typeCount,
                       int getterSetterCount, int todoCount, boolean hasEquals,
                       boolean hasHashCode, boolean recordCandidate) {
        this.path = path;
        this.totalLines = totalLines;
        this.codeLines = codeLines;
        this.typeCount = typeCount;
        this.getterSetterCount = getterSetterCount;
        this.todoCount = todoCount;
        this.hasEquals = hasEquals;
        this.hasHashCode = hasHashCode;
        this.recordCandidate = recordCandidate;
    }

    public String path() {
        return path;
    }

    public int totalLines() {
        return totalLines;
    }

    public int codeLines() {
        return codeLines;
    }

    public int typeCount() {
        return typeCount;
    }

    public int getterSetterCount() {
        return getterSetterCount;
    }

    public int todoCount() {
        return todoCount;
    }

    public boolean hasEquals() {
        return hasEquals;
    }

    public boolean hasHashCode() {
        return hasHashCode;
    }

    public boolean recordCandidate() {
        return recordCandidate;
    }
}
