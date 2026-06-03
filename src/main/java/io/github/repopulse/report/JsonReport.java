package io.github.repopulse.report;

import io.github.repopulse.model.FileMetrics;
import io.github.repopulse.model.Finding;
import io.github.repopulse.model.RepoMetrics;

import java.util.List;

/**
 * Renders {@link RepoMetrics} as JSON for machine consumption (CI, dashboards).
 * Hand-rolled to keep RepoPulse dependency-free.
 */
public final class JsonReport {

    public String render(RepoMetrics m) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"repo\": ").append(str(m.rootName())).append(",\n");
        sb.append("  \"onboardingScore\": ").append(m.onboardingScore()).append(",\n");
        sb.append("  \"buildTool\": ").append(str(m.buildTool())).append(",\n");
        sb.append("  \"summary\": {\n");
        sb.append("    \"javaFiles\": ").append(m.javaFileCount()).append(",\n");
        sb.append("    \"codeLines\": ").append(m.codeLines()).append(",\n");
        sb.append("    \"totalLines\": ").append(m.totalLines()).append(",\n");
        sb.append("    \"types\": ").append(m.typeCount()).append(",\n");
        sb.append("    \"getterSetterMethods\": ").append(m.getterSetterCount()).append(",\n");
        sb.append("    \"recordCandidates\": ").append(m.recordCandidateCount()).append(",\n");
        sb.append("    \"todoMarkers\": ").append(m.todoCount()).append("\n");
        sb.append("  },\n");
        sb.append("  \"onboarding\": {\n");
        sb.append("    \"hasReadme\": ").append(m.hasReadme()).append(",\n");
        sb.append("    \"hasTests\": ").append(m.hasTests()).append(",\n");
        sb.append("    \"hasCi\": ").append(m.hasCi()).append(",\n");
        sb.append("    \"hasGitignore\": ").append(m.hasGitignore()).append(",\n");
        sb.append("    \"hasLicense\": ").append(m.hasLicense()).append("\n");
        sb.append("  },\n");

        sb.append("  \"findings\": [");
        List<Finding> findings = m.findings();
        for (int i = 0; i < findings.size(); i++) {
            Finding f = findings.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {")
                    .append("\"severity\": ").append(str(f.severity().name())).append(", ")
                    .append("\"category\": ").append(str(f.category())).append(", ")
                    .append("\"message\": ").append(str(f.message())).append(", ")
                    .append("\"location\": ").append(str(f.location()))
                    .append("}");
        }
        sb.append(findings.isEmpty() ? "],\n" : "\n  ],\n");

        sb.append("  \"files\": [");
        List<FileMetrics> files = m.files();
        for (int i = 0; i < files.size(); i++) {
            FileMetrics f = files.get(i);
            sb.append(i == 0 ? "\n" : ",\n");
            sb.append("    {")
                    .append("\"path\": ").append(str(f.path())).append(", ")
                    .append("\"codeLines\": ").append(f.codeLines()).append(", ")
                    .append("\"getterSetters\": ").append(f.getterSetterCount()).append(", ")
                    .append("\"recordCandidate\": ").append(f.recordCandidate())
                    .append("}");
        }
        sb.append(files.isEmpty() ? "]\n" : "\n  ]\n");

        sb.append("}\n");
        return sb.toString();
    }

    /** JSON-encodes a string with the escapes required by the spec. */
    private static String str(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append('"').toString();
    }
}
