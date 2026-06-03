package io.github.repopulse.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated, repository-wide analysis result. Mutable during the scan, then
 * read by the report writer.
 */
public final class RepoMetrics {

    private String rootName = "";
    private int javaFileCount;
    private int totalLines;
    private int codeLines;
    private int typeCount;
    private int getterSetterCount;
    private int todoCount;
    private int recordCandidateCount;

    // Onboarding readiness signals.
    private String buildTool = "unknown";
    private boolean hasReadme;
    private boolean hasTests;
    private boolean hasCi;
    private boolean hasGitignore;
    private boolean hasLicense;

    private final List<FileMetrics> files = new ArrayList<>();
    private final List<Finding> findings = new ArrayList<>();

    public void add(FileMetrics fm) {
        files.add(fm);
        javaFileCount++;
        totalLines += fm.totalLines();
        codeLines += fm.codeLines();
        typeCount += fm.typeCount();
        getterSetterCount += fm.getterSetterCount();
        todoCount += fm.todoCount();
        if (fm.recordCandidate()) {
            recordCandidateCount++;
        }
    }

    public void addFinding(Finding f) {
        findings.add(f);
    }

    /**
     * Onboarding readiness as a 0-100 score across six common signals that a
     * new contributor needs in order to get productive quickly.
     */
    public int onboardingScore() {
        int signals = 0;
        if (hasReadme) signals++;
        if (hasTests) signals++;
        if (hasCi) signals++;
        if (hasGitignore) signals++;
        if (hasLicense) signals++;
        if (!"unknown".equals(buildTool)) signals++;
        return (int) Math.round(signals / 6.0 * 100.0);
    }

    public String rootName() { return rootName; }
    public void setRootName(String v) { this.rootName = v; }

    public int javaFileCount() { return javaFileCount; }
    public int totalLines() { return totalLines; }
    public int codeLines() { return codeLines; }
    public int typeCount() { return typeCount; }
    public int getterSetterCount() { return getterSetterCount; }
    public int todoCount() { return todoCount; }
    public int recordCandidateCount() { return recordCandidateCount; }

    public String buildTool() { return buildTool; }
    public void setBuildTool(String v) { this.buildTool = v; }

    public boolean hasReadme() { return hasReadme; }
    public void setHasReadme(boolean v) { this.hasReadme = v; }

    public boolean hasTests() { return hasTests; }
    public void setHasTests(boolean v) { this.hasTests = v; }

    public boolean hasCi() { return hasCi; }
    public void setHasCi(boolean v) { this.hasCi = v; }

    public boolean hasGitignore() { return hasGitignore; }
    public void setHasGitignore(boolean v) { this.hasGitignore = v; }

    public boolean hasLicense() { return hasLicense; }
    public void setHasLicense(boolean v) { this.hasLicense = v; }

    public List<FileMetrics> files() { return files; }
    public List<Finding> findings() { return findings; }
}
