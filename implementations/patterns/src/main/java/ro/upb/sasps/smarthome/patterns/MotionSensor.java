package ro.upb.sasps.smarthome.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MotionSensor {
  private final String id;
  private final List<MotionListener> subscribers = new ArrayList<>();
  private boolean motion;

  public MotionSensor(String id) {
    this.id = Objects.requireNonNull(id, "id");
  }

  public String id() {
    return id;
  }

  public void subscribe(MotionListener subscriber) {
    subscribers.add(Objects.requireNonNull(subscriber, "subscriber"));
  }

  public void unsubscribe(MotionListener subscriber) {
    subscribers.remove(subscriber);
  }

  public void report(boolean motion) {
    boolean started = motion && !this.motion;
    this.motion = motion;
    if (started) {
      for (MotionListener subscriber : subscribers) {
        subscriber.onMotionDetected(id);
      }
    }
  }
}
