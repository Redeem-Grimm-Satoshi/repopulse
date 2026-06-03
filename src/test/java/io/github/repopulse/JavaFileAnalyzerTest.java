package io.github.repopulse;

import io.github.repopulse.model.FileMetrics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaFileAnalyzerTest {

    private final JavaFileAnalyzer analyzer = new JavaFileAnalyzer();

    @Test
    void countsGettersAndSetters() {
        String src = "public class User {\n"
                + "  private String name;\n"
                + "  private int age;\n"
                + "  public String getName() { return name; }\n"
                + "  public void setName(String n) { this.name = n; }\n"
                + "  public int getAge() { return age; }\n"
                + "  public void setAge(int a) { this.age = a; }\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("User.java", src);
        assertEquals(4, fm.getterSetterCount());
        assertEquals(1, fm.typeCount());
    }

    @Test
    void flagsRecordCandidate() {
        String src = "public class Point {\n"
                + "  private final int x;\n"
                + "  private final int y;\n"
                + "  public int getX() { return x; }\n"
                + "  public int getY() { return y; }\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("Point.java", src);
        assertTrue(fm.recordCandidate(), "data carrier should be a record candidate");
    }

    @Test
    void doesNotFlagBehaviourClass() {
        String src = "public class Calculator {\n"
                + "  public int add(int a, int b) { return a + b; }\n"
                + "  public int sub(int a, int b) { return a - b; }\n"
                + "  public int mul(int a, int b) { return a * b; }\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("Calculator.java", src);
        assertFalse(fm.recordCandidate(), "behaviour-rich class should not be flagged");
    }

    @Test
    void detectsTodoMarkers() {
        String src = "public class A {\n"
                + "  // TODO: refactor this\n"
                + "  // FIXME broken\n"
                + "  public void m() {}\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("A.java", src);
        assertEquals(2, fm.todoCount());
    }

    @Test
    void detectsEqualsAndHashCode() {
        String src = "public class B {\n"
                + "  @Override public boolean equals(Object o) { return true; }\n"
                + "  @Override public int hashCode() { return 1; }\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("B.java", src);
        assertTrue(fm.hasEquals());
        assertTrue(fm.hasHashCode());
    }

    @Test
    void countsCodeLinesIgnoringComments() {
        String src = "public class C {\n"
                + "  // a comment\n"
                + "  /* block\n"
                + "     comment */\n"
                + "\n"
                + "  public void m() {}\n"
                + "}\n";
        FileMetrics fm = analyzer.analyze("C.java", src);
        // class line, method line, closing brace = 3 code lines
        assertEquals(3, fm.codeLines());
    }
}
