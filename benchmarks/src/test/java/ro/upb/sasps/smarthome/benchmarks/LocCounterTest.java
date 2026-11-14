package ro.upb.sasps.smarthome.benchmarks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocCounterTest {

  /** Fixed mock source; the comment on each line says whether it counts as code. */
  private static final String MOCK_SOURCE =
      """
      // header comment
      package demo;

      /**
       * Javadoc.
       */
      class Demo {
        /* inline */ int a = 1;
        String s = "// not a comment";
        int b = 2; // trailing comment
        /* block
           still comment */
      }
      """;

  /** Lines with code: package, class, int a, String s, int b, closing brace. */
  private static final long EXPECTED_CODE_LINES = 6;

  @Test
  void countsOnlyLinesWithCodeOutsideComments(@TempDir Path dir) throws IOException {
    Path file = dir.resolve("DemoLoc.java");
    Files.writeString(file, MOCK_SOURCE, StandardCharsets.UTF_8);

    assertEquals(EXPECTED_CODE_LINES, LocCounter.countCodeLines(file));
  }
}
