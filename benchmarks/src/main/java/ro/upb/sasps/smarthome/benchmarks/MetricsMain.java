package ro.upb.sasps.smarthome.benchmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Entry point: {@code java -jar benchmarks/target/smart-home-benchmarks.jar [loc|runtime] [--root
 * <repo>]}. Without a mode, runs loc and then runtime. Writes one JSON report per mode to {@code
 * <repo>/benchmarks/output/results_DDMMYY_HHMMSS/}.
 */
public final class MetricsMain {

  static final String USAGE =
      "Usage: java -jar benchmarks/target/smart-home-benchmarks.jar [loc|runtime] [--root <repo>]"
          + System.lineSeparator()
          + "  Without a mode, runs loc and then runtime into the same results folder.";

  static final List<String> MODES = List.of("loc", "runtime");

  /** Run folder name in local time, e.g. results_141126_093005 for 14 Nov 2026, 09:30:05. */
  static final DateTimeFormatter RUN_FOLDER =
      DateTimeFormatter.ofPattern("'results_'ddMMyy_HHmmss");

  /** Top-level JSON document. */
  public record Report(String mode, Provenance provenance, Object results) {}

  private MetricsMain() {}

  public static void main(String[] args) {
    int exitCode = run(args, System.out, System.err);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  static int run(String[] args, PrintStream out, PrintStream err) {
    List<String> remaining = List.of(args);
    Path root = Path.of(".");
    int rootIndex = remaining.indexOf("--root");
    if (rootIndex >= 0) {
      if (rootIndex != remaining.size() - 2) {
        err.println(USAGE);
        return 2;
      }
      root = Path.of(remaining.get(rootIndex + 1));
      remaining = remaining.subList(0, rootIndex);
    }
    if (remaining.size() > 1) {
      err.println(USAGE);
      return 2;
    }
    List<String> modes = remaining.isEmpty() ? MODES : remaining;
    if (!MODES.contains(modes.get(0))) {
      err.println("Unknown mode: " + modes.get(0));
      err.println(USAGE);
      return 2;
    }
    root = root.toAbsolutePath().normalize();
    if (!Files.isRegularFile(root.resolve("pom.xml"))
        || !Files.isDirectory(root.resolve("implementations"))) {
      err.println("Not the repository root: " + root);
      err.println("Run from the repository root or pass --root <repo>.");
      return 2;
    }

    LocalDateTime started = LocalDateTime.now();
    try {
      for (String mode : modes) {
        Path file = runMode(mode, root, List.of(args), out, started);
        out.println("Wrote " + file);
      }
      return 0;
    } catch (IOException | RuntimeException e) {
      err.println("Failed: " + e.getMessage());
      return 1;
    }
  }

  /** Runs one mode, prints its summary and writes its report into the run folder. */
  private static Path runMode(
      String mode, Path root, List<String> args, PrintStream out, LocalDateTime started)
      throws IOException {
    Provenance provenance = Provenance.capture(root, args);
    Object results;
    if ("loc".equals(mode)) {
      LocCounter.LocReport loc = new LocCounter().count(root);
      printLoc(out, loc);
      results = loc;
    } else {
      out.println("Running runtime benchmark " + RuntimeBenchmark.Config.DEFAULT + " ...");
      RuntimeBenchmark.Result runtime =
          new RuntimeBenchmark(RuntimeBenchmark.Config.DEFAULT, RuntimeBenchmark.defaultEngines())
              .run();
      printRuntime(out, runtime);
      results = runtime;
    }
    out.println("Commit: " + provenance.commitSha() + " (" + provenance.commitShaSource() + ")");
    return write(root, new Report(mode, provenance, results), started);
  }

  /**
   * Writes to benchmarks/output/results_DDMMYY_HHMMSS/<mode>.json. Runs within the same second
   * share the folder; an existing file is never overwritten (a -2, -3... suffix is added instead).
   */
  static Path write(Path root, Report report, LocalDateTime now) throws IOException {
    Path runDir = root.resolve("benchmarks").resolve("output").resolve(now.format(RUN_FOLDER));
    Files.createDirectories(runDir);
    Path file = runDir.resolve(report.mode() + ".json");
    for (int suffix = 2; Files.exists(file); suffix++) {
      file = runDir.resolve(report.mode() + "-" + suffix + ".json");
    }
    new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(file.toFile(), report);
    return file;
  }

  private static void printLoc(PrintStream out, LocCounter.LocReport loc) {
    out.println(loc.definition());
    for (Map.Entry<String, LocCounter.CategoryCount> entry : loc.categories().entrySet()) {
      out.printf(
          Locale.ROOT,
          "  %-10s %6d lines in %4d files%n",
          entry.getKey(),
          entry.getValue().codeLines(),
          entry.getValue().files());
    }
    out.printf(
        Locale.ROOT,
        "  %-10s %6d lines in %4d files%n",
        "total",
        loc.total().codeLines(),
        loc.total().files());
  }

  private static void printRuntime(PrintStream out, RuntimeBenchmark.Result result) {
    for (Map.Entry<String, RuntimeBenchmark.EngineSummary> entry : result.engines().entrySet()) {
      RuntimeBenchmark.EngineSummary summary = entry.getValue();
      out.printf(
          Locale.ROOT,
          "  %-10s median %.3f ms (min %.3f, max %.3f), %.1f ns/command%n",
          entry.getKey(),
          summary.medianMs(),
          summary.minMs(),
          summary.maxMs(),
          summary.medianNsPerCommand());
    }
  }
}
