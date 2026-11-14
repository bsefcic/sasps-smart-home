package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MotionSensorTest {
  private final MotionSensor sensor = new MotionSensor("living-motion");
  private final List<String> notifications = new ArrayList<>();

  @Test
  void notifiesEverySubscriberOnlyWhenMotionStarts() {
    sensor.subscribe(notifications::add);
    sensor.subscribe(notifications::add);

    sensor.report(false);
    sensor.report(true);
    sensor.report(true);

    assertEquals(List.of("living-motion", "living-motion"), notifications);
  }

  @Test
  void notifiesAgainAfterMotionStopsAndStartsAgain() {
    sensor.subscribe(notifications::add);

    sensor.report(true);
    sensor.report(false);
    sensor.report(true);

    assertEquals(2, notifications.size());
  }

  @Test
  void anUnsubscribedListenerIsNotNotified() {
    MotionListener listener = notifications::add;
    sensor.subscribe(listener);
    sensor.unsubscribe(listener);

    sensor.report(true);

    assertEquals(List.of(), notifications);
  }
}
