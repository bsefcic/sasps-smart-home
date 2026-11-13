package ro.upb.sasps.smarthome.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** A source/POM guard for direct dependencies, not a complete Java or Maven analyzer. */
class ArchitectureBoundaryTest {
  private static final List<String> MODULES =
      List.of(
          "shared/contracts",
          "shared/simulators",
          "implementations/baseline",
          "implementations/patterns");
  private static final Pattern COMMENTS = Pattern.compile("(?s)/\\*.*?\\*/|//[^\\r\\n]*");
  private static final Pattern IMPORT =
      Pattern.compile("\\bimport\\s+(?:static\\s+)?([\\w.]+(?:\\*)?)\\s*;");

  @TempDir Path temporaryRoot;

  @Test
  void productionModulesRespectDirectDependencyBoundaries() throws Exception {
    Path root = Path.of(System.getProperty("repositoryRoot")).toAbsolutePath().normalize();
    List<String> violations = new ArrayList<>();
    for (String module : MODULES) {
      violations.addAll(inspect(root, module));
    }
    assertTrue(violations.isEmpty(), () -> String.join(System.lineSeparator(), violations));
  }

  @ParameterizedTest
  @CsvSource({
    "implementations/baseline, import ro.upb.sasps.smarthome.patterns.PatternsHome;",
    "implementations/patterns, import ro.upb.sasps.smarthome.baseline.*;",
    "shared/contracts, import org.springframework.context.ApplicationContext;",
    "shared/simulators, import com.fasterxml.jackson.annotation.JsonIgnore;",
    "implementations/baseline, import static org.springframework.util.Assert.isTrue;",
    "implementations/patterns, import com.fasterxml.jackson.databind.*;"
  })
  void detectsForbiddenProductionImports(String module, String declaration) throws Exception {
    fixture(module, declaration, "");
    assertEquals(
        List.of(module + "/src/main/java/Sample.java: forbidden " + declaration),
        inspect(temporaryRoot, module));
  }

  @ParameterizedTest
  @CsvSource({
    "implementations/baseline, ro.upb.sasps, smart-home-patterns, compile",
    "implementations/patterns, ro.upb.sasps, smart-home-baseline, runtime",
    "shared/contracts, org.springframework, spring-core, provided",
    "shared/simulators, org.springframework.boot, spring-boot-starter, compile",
    "implementations/baseline, com.fasterxml.jackson.core, jackson-databind, compile",
    "implementations/patterns, com.fasterxml.jackson.module, jackson-module-parameter-names, compile"
  })
  void detectsForbiddenProductionDependencies(
      String module, String group, String artifact, String scope) throws Exception {
    fixture(module, "", dependency(group, artifact, scope));
    assertEquals(
        List.of(module + "/pom.xml: forbidden dependency " + group + ":" + artifact),
        inspect(temporaryRoot, module));
  }

  @Test
  void detectsDependenciesDeclaredInsideProfiles() throws Exception {
    String module = "shared/contracts";
    fixture(module, "", "");
    Files.writeString(
        temporaryRoot.resolve(module).resolve("pom.xml"),
        "<project><profiles><profile><id>extra</id><dependencies>"
            + dependency("com.fasterxml.jackson.core", "jackson-databind", "")
            + "</dependencies></profile></profiles></project>");
    assertEquals(1, inspect(temporaryRoot, module).size());
  }

  @Test
  void allowsNeutralImportsTestDependenciesAndComments() throws Exception {
    String module = "implementations/baseline";
    fixture(
        module,
        """
        // import ro.upb.sasps.smarthome.patterns.PatternsHome;
        /* import org.springframework.context.ApplicationContext; */
        import ro.upb.sasps.smarthome.contracts.SmartHome;
        import ro.upb.sasps.smarthome.simulators.VendorALight;
        """,
        dependency("org.springframework", "spring-test", "test")
            + dependency("com.fasterxml.jackson.core", "jackson-databind", "test")
            + dependency("ro.upb.sasps", "smart-home-patterns", "test")
            + dependency("ro.upb.sasps", "smart-home-contracts", ""));
    Path testFile = temporaryRoot.resolve(module).resolve("src/test/java/ExampleTest.java");
    Files.createDirectories(testFile.getParent());
    Files.writeString(testFile, "import ro.upb.sasps.smarthome.patterns.PatternsHome;");
    assertEquals(List.of(), inspect(temporaryRoot, module));
  }

  private void fixture(String module, String imports, String dependencies) throws Exception {
    Path directory = temporaryRoot.resolve(module);
    Files.createDirectories(directory.resolve("src/main/java"));
    Files.writeString(
        directory.resolve("src/main/java/Sample.java"), imports + "\nclass Sample {}\n");
    Files.writeString(
        directory.resolve("pom.xml"),
        "<project><dependencies>" + dependencies + "</dependencies></project>");
  }

  private static String dependency(String group, String artifact, String scope) {
    return "<dependency><groupId>"
        + group
        + "</groupId><artifactId>"
        + artifact
        + "</artifactId>"
        + (scope.isEmpty() ? "" : "<scope>" + scope + "</scope>")
        + "</dependency>";
  }

  private static List<String> inspect(Path root, String module) throws Exception {
    Path directory = root.resolve(module);
    Path sources = directory.resolve("src/main/java");
    Path pom = directory.resolve("pom.xml");
    assertTrue(Files.isDirectory(sources), () -> "Missing source directory: " + sources);
    assertTrue(Files.isRegularFile(pom), () -> "Missing POM: " + pom);
    List<String> violations = new ArrayList<>();
    try (var paths = Files.walk(sources)) {
      List<Path> javaFiles = paths.filter(path -> path.toString().endsWith(".java")).toList();
      assertTrue(!javaFiles.isEmpty(), () -> "No Java sources found in " + sources);
      for (Path file : javaFiles) {
        var imports = IMPORT.matcher(COMMENTS.matcher(Files.readString(file)).replaceAll(""));
        while (imports.find()) {
          String reference = imports.group(1);
          if (frameworkPackage(reference)
              || (module.equals("implementations/baseline")
                  && reference.startsWith("ro.upb.sasps.smarthome.patterns."))
              || (module.equals("implementations/patterns")
                  && reference.startsWith("ro.upb.sasps.smarthome.baseline."))) {
            violations.add(
                root.relativize(file).toString().replace('\\', '/')
                    + ": forbidden "
                    + imports.group());
          }
        }
      }
    }
    var factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    var document = factory.newDocumentBuilder().parse(pom.toFile());
    var dependencies =
        (NodeList)
            XPathFactory.newInstance()
                .newXPath()
                .evaluate(
                    "/project/dependencies/dependency"
                        + " | /project/profiles/profile/dependencies/dependency",
                    document,
                    XPathConstants.NODESET);
    for (int index = 0; index < dependencies.getLength(); index++) {
      Element dependency = (Element) dependencies.item(index);
      String scope = childText(dependency, "scope");
      String group = childText(dependency, "groupId");
      String artifact = childText(dependency, "artifactId");
      if (!scope.equals("test")
          && (frameworkPackage(group + ".")
              || (module.equals("implementations/baseline")
                  && artifact.equals("smart-home-patterns"))
              || (module.equals("implementations/patterns")
                  && artifact.equals("smart-home-baseline")))) {
        violations.add(module + "/pom.xml: forbidden dependency " + group + ":" + artifact);
      }
    }
    return violations;
  }

  private static String childText(Element parent, String name) {
    NodeList children = parent.getElementsByTagName(name);
    return children.getLength() == 0 ? "" : children.item(0).getTextContent().strip();
  }

  private static boolean frameworkPackage(String reference) {
    return reference.startsWith("org.springframework.")
        || reference.startsWith("com.fasterxml.jackson.");
  }
}
