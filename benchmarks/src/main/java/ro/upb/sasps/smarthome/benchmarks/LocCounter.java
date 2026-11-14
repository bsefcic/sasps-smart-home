package ro.upb.sasps.smarthome.benchmarks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Counts physical lines of code in .java files: lines with at least one non-whitespace character
 * outside comments. Blank lines, comment-only lines and Javadoc are excluded; a line with code and
 * a trailing comment counts once.
 */
public final class LocCounter {

  public static final String DEFINITION =
      "Physical lines in .java files with code outside comments; blank and comment-only lines"
          + " excluded (not logical LOC).";

  public static final String BASELINE = "baseline";
  public static final String PATTERNS = "patterns";
  public static final String SHARED = "shared";

  public record CategoryCount(long files, long codeLines) {}

  /** categoryRoots lists the repository-relative source folders actually walked per category. */
  public record LocReport(
      String definition,
      Map<String, List<String>> categoryRoots,
      Map<String, CategoryCount> categories,
      CategoryCount total) {}

  /**
   * Walks only the production source folders of the engines and of every shared module; nothing
   * else in the repository (tests, server, app, benchmarks, build output) is visited.
   */
  public LocReport count(Path root) throws IOException {
    Path base = root.toAbsolutePath().normalize();
    Map<String, List<Path>> roots = sourceRoots(base);

    Map<String, List<String>> categoryRoots = new LinkedHashMap<>();
    Map<String, CategoryCount> categories = new LinkedHashMap<>();
    long totalFiles = 0;
    long totalLines = 0;
    for (Map.Entry<String, List<Path>> entry : roots.entrySet()) {
      long files = 0;
      long lines = 0;
      List<String> relativeRoots = new ArrayList<>();
      for (Path sourceRoot : entry.getValue()) {
        relativeRoots.add(base.relativize(sourceRoot).toString().replace('\\', '/'));
        for (Path file : javaFiles(sourceRoot)) {
          files++;
          lines += countCodeLines(file);
        }
      }
      categoryRoots.put(entry.getKey(), List.copyOf(relativeRoots));
      categories.put(entry.getKey(), new CategoryCount(files, lines));
      totalFiles += files;
      totalLines += lines;
    }
    return new LocReport(
        DEFINITION, categoryRoots, categories, new CategoryCount(totalFiles, totalLines));
  }

  /** Source folders per category; fails instead of silently reporting zero for a missing one. */
  static Map<String, List<Path>> sourceRoots(Path base) throws IOException {
    Map<String, List<Path>> roots = new LinkedHashMap<>();
    roots.put(BASELINE, List.of(requireDirectory(base, "implementations/baseline/src/main/java")));
    roots.put(PATTERNS, List.of(requireDirectory(base, "implementations/patterns/src/main/java")));

    List<Path> shared = new ArrayList<>();
    Path sharedDir = requireDirectory(base, "shared");
    try (Stream<Path> modules = Files.list(sharedDir)) {
      for (Path module : modules.sorted().toList()) {
        Path sourceRoot = module.resolve("src/main/java");
        if (Files.isDirectory(sourceRoot)) {
          shared.add(sourceRoot);
        }
      }
    }
    if (shared.isEmpty()) {
      throw new IOException("No shared/*/src/main/java folder found under " + sharedDir);
    }
    roots.put(SHARED, List.copyOf(shared));
    return roots;
  }

  private static Path requireDirectory(Path base, String relative) throws IOException {
    Path dir = base.resolve(relative);
    if (!Files.isDirectory(dir)) {
      throw new IOException("Missing source folder: " + dir);
    }
    return dir;
  }

  private static List<Path> javaFiles(Path sourceRoot) throws IOException {
    try (Stream<Path> paths = Files.walk(sourceRoot)) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".java"))
          .sorted()
          .toList();
    }
  }

  static long countCodeLines(Path file) throws IOException {
    return countCodeLines(Files.readString(file, StandardCharsets.UTF_8));
  }

  private enum State {
    CODE,
    LINE_COMMENT,
    BLOCK_COMMENT,
    STRING,
    CHAR,
    TEXT_BLOCK
  }

  /**
   * Counts lines holding at least one non-whitespace character outside comments. Comment markers
   * inside string, char and text-block literals are not treated as comments. Unicode escapes
   * (backslash-u) are not decoded.
   */
  static long countCodeLines(String source) {
    long count = 0;
    boolean hasCode = false;
    State state = State.CODE;
    int length = source.length();
    for (int i = 0; i < length; i++) {
      char c = source.charAt(i);
      char next = i + 1 < length ? source.charAt(i + 1) : '\0';
      if (c == '\n') {
        if (hasCode) {
          count++;
        }
        hasCode = false;
        if (state == State.LINE_COMMENT) {
          state = State.CODE;
        }
        continue;
      }
      switch (state) {
        case CODE -> {
          if (c == '/' && next == '/') {
            state = State.LINE_COMMENT;
            i++;
          } else if (c == '/' && next == '*') {
            state = State.BLOCK_COMMENT;
            i++;
          } else if (c == '"') {
            hasCode = true;
            if (next == '"' && i + 2 < length && source.charAt(i + 2) == '"') {
              state = State.TEXT_BLOCK;
              i += 2;
            } else {
              state = State.STRING;
            }
          } else if (c == '\'') {
            hasCode = true;
            state = State.CHAR;
          } else if (!Character.isWhitespace(c)) {
            hasCode = true;
          }
        }
        case LINE_COMMENT -> {}
        case BLOCK_COMMENT -> {
          if (c == '*' && next == '/') {
            state = State.CODE;
            i++;
          }
        }
        case STRING, CHAR -> {
          hasCode = true;
          if (c == '\\' && next != '\n') {
            i++;
          } else if (c == (state == State.STRING ? '"' : '\'')) {
            state = State.CODE;
          }
        }
        case TEXT_BLOCK -> {
          if (!Character.isWhitespace(c)) {
            hasCode = true;
          }
          if (c == '\\' && next != '\n') {
            i++;
          } else if (c == '"' && next == '"' && i + 2 < length && source.charAt(i + 2) == '"') {
            state = State.CODE;
            i += 2;
          }
        }
      }
    }
    return hasCode ? count + 1 : count;
  }
}
