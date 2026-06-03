package io.github.repopulse.report;

import io.github.repopulse.model.Finding;
import io.github.repopulse.model.RepoMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonReportTest {

    @Test
    void rendersCoreFields() {
        RepoMetrics m = new RepoMetrics();
        m.setRootName("demo");
        m.setBuildTool("Maven");
        m.setHasReadme(true);

        String json = new JsonReport().render(m);

        assertTrue(json.contains("\"repo\": \"demo\""), json);
        assertTrue(json.contains("\"buildTool\": \"Maven\""), json);
        assertTrue(json.contains("\"onboardingScore\":"), json);
        assertTrue(json.contains("\"hasReadme\": true"), json);
        // Balanced braces is a cheap structural sanity check.
        assertTrue(countChar(json, '{') == countChar(json, '}'), "unbalanced braces");
    }

    @Test
    void escapesQuotesInStrings() {
        RepoMetrics m = new RepoMetrics();
        m.setRootName("a\"b");
        m.addFinding(new Finding(Finding.Severity.INFO, "Cat", "msg \"quoted\"", "loc"));

        String json = new JsonReport().render(m);

        assertTrue(json.contains("a\\\"b"), json);
        assertTrue(json.contains("msg \\\"quoted\\\""), json);
    }

    private static int countChar(String s, char c) {
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) n++;
        }
        return n;
    }
}
