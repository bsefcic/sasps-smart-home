package ro.upb.sasps.smarthome.benchmarks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class StatsTest {

  @Test
  void medianOfOddCountIsMiddleSample() {
    Stats stats = Stats.of(List.of(5.0, 1.0, 3.0));

    assertEquals(3.0, stats.median());
    assertEquals(1.0, stats.min());
    assertEquals(5.0, stats.max());
  }

  @Test
  void medianOfEvenCountIsMeanOfMiddleTwo() {
    Stats stats = Stats.of(List.of(4.0, 1.0, 3.0, 2.0));

    assertEquals(2.5, stats.median());
    assertEquals(1.0, stats.min());
    assertEquals(4.0, stats.max());
  }
}
