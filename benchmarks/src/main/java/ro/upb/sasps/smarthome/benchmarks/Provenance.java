package ro.upb.sasps.smarthome.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Pattern;

/** Environment and source revision recorded next to every result. */
public record Provenance(
    String commitSha,
    String commitShaSource,
    Boolean commitDirty,
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

  /** Written into the JAR by git-commit-id-maven-plugin (see benchmarks/pom.xml). */
  static final String BUILD_INFO_RESOURCE = "/benchmarks-git.properties";

  /** Commit the running code was built from, as embedded at build time. */
  record BuildInfo(String commitSha, boolean dirty) {}

  /**
   * For runtime: the SHA embedded in the JAR at build time, since it describes the code actually
   * being measured. Recorded as unknown if the JAR carries none.
   */
  public static Provenance forRuntime(List<String> arguments) {
    Optional<BuildInfo> build = readBuildInfo();
    return build.isPresent()
        ? of(build.get().commitSha(), "build", build.get().dirty(), arguments)
        : of("unknown", "unavailable", null, arguments);
  }

  /**
   * For loc: HEAD of the checkout being counted, read from its .git directory. Recorded as unknown
   * if it cannot be read; commitDirty is always null, since .git alone does not reveal it.
   */
  public static Provenance forLoc(Path repositoryRoot, List<String> arguments) {
    Optional<String> head = readCommitSha(repositoryRoot);
    return head.isPresent()
        ? of(head.get(), ".git", null, arguments)
        : of("unknown", "unavailable", null, arguments);
  }

  private static Provenance of(String sha, String source, Boolean dirty, List<String> arguments) {
    return new Provenance(
        sha,
        source,
        dirty,
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

  static Optional<BuildInfo> readBuildInfo() {
    try (InputStream in = Provenance.class.getResourceAsStream(BUILD_INFO_RESOURCE)) {
      if (in == null) {
        return Optional.empty();
      }
      Properties properties = new Properties();
      properties.load(in);
      return validSha(properties.getProperty("git.commit.id", ""))
          .map(
              sha -> new BuildInfo(sha, Boolean.parseBoolean(properties.getProperty("git.dirty"))));
    } catch (IOException | RuntimeException e) {
      return Optional.empty();
    }
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
