package ro.upb.sasps.smarthome.benchmarks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Environment and source revision recorded next to every result. */
public record Provenance(
    String commitSha,
    String commitShaSource,
    String timestampUtc,
    List<String> arguments,
    String javaVersion,
    String javaVendor,
    String javaVmName,
    String osName,
    String osVersion,
    String osArch,
    int availableProcessors) {

  private static final Pattern SHA = Pattern.compile("[0-9a-f]{40}|[0-9a-f]{64}");

  public static Provenance capture(Path repositoryRoot, List<String> arguments) {
    String sha = "unknown";
    String source = "unavailable";
    Optional<String> fromGit = readCommitSha(repositoryRoot);
    String fromEnv = System.getenv("GITHUB_SHA");
    if (fromGit.isPresent()) {
      sha = fromGit.get();
      source = ".git";
    } else if (fromEnv != null && SHA.matcher(fromEnv).matches()) {
      sha = fromEnv;
      source = "GITHUB_SHA";
    }
    return new Provenance(
        sha,
        source,
        Instant.now().truncatedTo(ChronoUnit.MILLIS).toString(),
        List.copyOf(arguments),
        System.getProperty("java.version"),
        System.getProperty("java.vendor"),
        System.getProperty("java.vm.name"),
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("os.arch"),
        Runtime.getRuntime().availableProcessors());
  }

  /**
   * Reads HEAD directly from the .git directory, so the git executable is not required. Supports
   * branches (loose or packed refs), detached HEAD and worktrees. Does not detect uncommitted
   * changes.
   */
  static Optional<String> readCommitSha(Path repositoryRoot) {
    try {
      Path dotGit = repositoryRoot.resolve(".git");
      Path gitDir;
      if (Files.isDirectory(dotGit)) {
        gitDir = dotGit;
      } else if (Files.isRegularFile(dotGit)) {
        String content = read(dotGit);
        if (!content.startsWith("gitdir:")) {
          return Optional.empty();
        }
        gitDir = repositoryRoot.resolve(content.substring("gitdir:".length()).trim()).normalize();
      } else {
        return Optional.empty();
      }

      Path commonDir = gitDir;
      Path commonDirFile = gitDir.resolve("commondir");
      if (Files.isRegularFile(commonDirFile)) {
        commonDir = gitDir.resolve(read(commonDirFile)).normalize();
      }

      String head = read(gitDir.resolve("HEAD"));
      if (!head.startsWith("ref:")) {
        return validSha(head);
      }
      String ref = head.substring("ref:".length()).trim();
      for (Path dir : List.of(gitDir, commonDir)) {
        Path loose = dir.resolve(ref);
        if (Files.isRegularFile(loose)) {
          return validSha(read(loose));
        }
      }
      Path packed = commonDir.resolve("packed-refs");
      if (Files.isRegularFile(packed)) {
        for (String line : Files.readAllLines(packed, StandardCharsets.UTF_8)) {
          String[] parts = line.trim().split(" ");
          if (parts.length == 2 && parts[1].equals(ref)) {
            return validSha(parts[0]);
          }
        }
      }
      return Optional.empty();
    } catch (IOException | RuntimeException e) {
      return Optional.empty();
    }
  }

  private static Optional<String> validSha(String value) {
    String trimmed = value.trim();
    return SHA.matcher(trimmed).matches() ? Optional.of(trimmed) : Optional.empty();
  }

  private static String read(Path file) throws IOException {
    return Files.readString(file, StandardCharsets.UTF_8).trim();
  }
}
