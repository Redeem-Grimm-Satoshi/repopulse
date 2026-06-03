package io.github.repopulse.model;

/**
 * A single actionable observation about the repository, e.g. a class that could
 * become a record or a file with heavy getter/setter boilerplate.
 */
public final class Finding {

    public enum Severity { INFO, SUGGESTION, WARNING }

    private final Severity severity;
    private final String category;
    private final String message;
    private final String location;

    public Finding(Severity severity, String category, String message, String location) {
        this.severity = severity;
        this.category = category;
        this.message = message;
        this.location = location;
    }

    public Severity severity() {
        return severity;
    }

    public String category() {
        return category;
    }

    public String message() {
        return message;
    }

    public String location() {
        return location;
    }
}
