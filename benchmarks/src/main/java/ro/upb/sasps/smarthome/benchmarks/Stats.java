package ro.upb.sasps.smarthome.benchmarks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Descriptive statistics over raw samples. */
public record Stats(double median, double min, double max) {

  public static Stats of(List<Double> samples) {
    if (samples.isEmpty()) {
      throw new IllegalArgumentException("samples must not be empty");
    }
    List<Double> sorted = new ArrayList<>(samples);
    Collections.sort(sorted);
    int size = sorted.size();
    int middle = size / 2;
    double median =
        size % 2 == 1 ? sorted.get(middle) : (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
    return new Stats(median, sorted.get(0), sorted.get(size - 1));
  }
}
