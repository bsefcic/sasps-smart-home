package ro.upb.sasps.smarthome.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class DemoTest {
  @Test
  void producesIdenticalOutputForBothVariants() {
    ByteArrayOutputStream baseline = new ByteArrayOutputStream();
    ByteArrayOutputStream patterns = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    try (PrintStream a = new PrintStream(baseline, true, StandardCharsets.UTF_8);
        PrintStream b = new PrintStream(patterns, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
      assertEquals(0, Demo.run(new String[] {"baseline"}, a, err));
      assertEquals(0, Demo.run(new String[] {"patterns"}, b, err));
    }
    assertEquals(
        baseline.toString(StandardCharsets.UTF_8), patterns.toString(StandardCharsets.UTF_8));
    assertTrue(baseline.toString(StandardCharsets.UTF_8).contains("\"power\":true"));
    assertEquals("", errors.toString(StandardCharsets.UTF_8));
  }

  @Test
  void rejectsUnknownVariantWithoutOutput() {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    ByteArrayOutputStream errors = new ByteArrayOutputStream();
    try (PrintStream out = new PrintStream(output, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
      assertEquals(1, Demo.run(new String[] {"invalid"}, out, err));
    }
    assertEquals("", output.toString(StandardCharsets.UTF_8));
    assertTrue(errors.toString(StandardCharsets.UTF_8).contains("Usage:"));
  }
}
