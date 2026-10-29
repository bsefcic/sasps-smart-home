package ro.upb.sasps.smarthome.cli;

import java.io.PrintStream;
import ro.upb.sasps.smarthome.baseline.BaselineHome;
import ro.upb.sasps.smarthome.contracts.CommandResult;
import ro.upb.sasps.smarthome.contracts.LightSnapshot;
import ro.upb.sasps.smarthome.contracts.SetLightPower;
import ro.upb.sasps.smarthome.contracts.SmartHome;
import ro.upb.sasps.smarthome.patterns.PatternsHome;

/** Fixed starter demo, not yet a general JSON scenario parser. */
public final class Demo {
  private Demo() {}

  public static void main(String[] args) {
    System.exit(run(args, System.out, System.err));
  }

  public static int run(String[] args, PrintStream out, PrintStream err) {
    String variant = args.length == 0 ? "baseline" : args[0];
    if (args.length > 1 || (!variant.equals("baseline") && !variant.equals("patterns"))) {
      err.println("Usage: java -jar app/target/smart-home.jar baseline|patterns");
      return 1;
    }
    SmartHome home = variant.equals("baseline") ? new BaselineHome() : new PatternsHome();
    CommandResult result = home.execute(new SetLightPower("living-light", true));
    boolean power = ((LightSnapshot) home.getSnapshot().devices().get(0)).power();
    // Fixed values only. A general JSON serializer/parser belongs to the CLI story.
    out.printf(
        "{\"result\":{\"ok\":%s},\"snapshot\":{\"devices\":[{\"id\":\"living-light\",\"kind\":\"light\",\"power\":%s}]}}%n",
        result.ok(), power);
    return 0;
  }
}
