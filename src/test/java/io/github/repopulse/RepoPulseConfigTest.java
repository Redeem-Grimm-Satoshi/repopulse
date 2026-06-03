package io.github.repopulse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepoPulseConfigTest {

    @Test
    void parsesScalars() {
        String yaml = "output: HEALTH.md\n"
                + "format: json\n"
                + "failUnder: 70\n";
        RepoPulseConfig cfg = RepoPulseConfig.parse(yaml);
        assertEquals("HEALTH.md", cfg.output());
        assertEquals("json", cfg.format());
        assertTrue(cfg.isJson());
        assertEquals(70, cfg.failUnder());
        assertTrue(cfg.hasFailGate());
    }

    @Test
    void parsesExcludeList() {
        String yaml = "exclude:\n"
                + "  - generated/\n"
                + "  - legacy/\n";
        RepoPulseConfig cfg = RepoPulseConfig.parse(yaml);
        assertEquals(2, cfg.exclude().size());
        assertTrue(cfg.isExcluded("src/generated/Foo.java"));
        assertTrue(cfg.isExcluded("legacy/Bar.java"));
        assertFalse(cfg.isExcluded("src/main/java/App.java"));
    }

    @Test
    void ignoresCommentsAndBlankLines() {
        String yaml = "# a comment\n"
                + "\n"
                + "format: markdown   # inline comment\n";
        RepoPulseConfig cfg = RepoPulseConfig.parse(yaml);
        assertEquals("markdown", cfg.format());
        assertFalse(cfg.isJson());
    }

    @Test
    void defaultsWhenEmpty() {
        RepoPulseConfig cfg = RepoPulseConfig.parse("");
        assertEquals("REPORT.md", cfg.output());
        assertEquals("markdown", cfg.format());
        assertFalse(cfg.hasFailGate());
        assertTrue(cfg.exclude().isEmpty());
    }

    @Test
    void supportsGlobInExclude() {
        String yaml = "exclude:\n  - *Test.java\n";
        RepoPulseConfig cfg = RepoPulseConfig.parse(yaml);
        assertTrue(cfg.isExcluded("src/test/java/io/app/FooTest.java"));
        assertFalse(cfg.isExcluded("src/main/java/io/app/Foo.java"));
    }

    @Test
    void invalidFailUnderFallsBackToDisabled() {
        RepoPulseConfig cfg = RepoPulseConfig.parse("failUnder: notanumber\n");
        assertFalse(cfg.hasFailGate());
        assertEquals(-1, cfg.failUnder());
    }
}
