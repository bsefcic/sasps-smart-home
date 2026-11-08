package ro.upb.sasps.smarthome.baseline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Every test creates its own household, so tests can change it freely. */
class HouseholdTest {
  private static final Map<String, Role> INITIAL =
      Map.of("ana", Role.OWNER, "mihai", Role.ADULT, "ioana", Role.CHILD);

  @Test
  void startsWithThreeMembersInTheOrderTheyWereAdded() {
    Household household = new Household();

    assertEquals(
        Map.of("ana", Role.OWNER, "mihai", Role.ADULT, "ioana", Role.CHILD), household.members());
    assertEquals(List.of("ana", "mihai", "ioana"), List.copyOf(household.members().keySet()));
  }

  @Test
  void eachInstanceIsASeparateDatabase() {
    Household first = new Household();
    Household second = new Household();

    first.remove("ioana");

    assertNotSame(first, second);
    assertEquals(Optional.empty(), first.roleOf("ioana"));
    assertEquals(Optional.of(Role.CHILD), second.roleOf("ioana"));
  }

  @Test
  void addsChangesAndRemovesMembers() {
    Household household = new Household();

    household.add("radu", Role.ADULT);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("radu"));
    assertEquals(
        List.of("ana", "mihai", "ioana", "radu"), List.copyOf(household.members().keySet()));

    household.changeRole("radu", Role.CHILD);
    assertEquals(Optional.of(Role.CHILD), household.roleOf("radu"));

    assertTrue(household.remove("radu"));
    assertEquals(Optional.empty(), household.roleOf("radu"));
    assertFalse(household.remove("radu"));
  }

  @Test
  void changingTheRoleKeepsThePositionAndReAddingMovesToTheEnd() {
    Household household = new Household();

    household.changeRole("mihai", Role.CHILD);
    assertEquals(List.of("ana", "mihai", "ioana"), List.copyOf(household.members().keySet()));

    household.remove("mihai");
    household.add("mihai", Role.ADULT);
    assertEquals(List.of("ana", "ioana", "mihai"), List.copyOf(household.members().keySet()));
  }

  @Test
  void nobodyElseCanBeAddedOrPromotedAsOwner() {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.add("radu", Role.OWNER));
    assertThrows(IllegalArgumentException.class, () -> household.changeRole("mihai", Role.OWNER));

    assertEquals(INITIAL, household.members());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana", "ANA"})
  void theOwnerCannotBeRemovedOrDemoted(String owner) {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.remove(owner));
    assertThrows(IllegalArgumentException.class, () -> household.changeRole(owner, Role.ADULT));

    assertEquals(INITIAL, household.members());
  }

  @Test
  void transferringOwnershipMakesThePreviousOwnerAnAdult() {
    Household household = new Household();

    household.transferOwnership("Mihai");

    assertEquals(
        Map.of("ana", Role.ADULT, "mihai", Role.OWNER, "ioana", Role.CHILD), household.members());
    assertTrue(household.remove("ana"));
    assertThrows(IllegalArgumentException.class, () -> household.remove("mihai"));
  }

  @Test
  void transferringOwnershipToTheCurrentOwnerChangesNothing() {
    Household household = new Household();

    household.transferOwnership("ana");

    assertEquals(INITIAL, household.members());
  }

  @Test
  void transferringOwnershipToAnUnknownMemberChangesNothing() {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.transferOwnership("radu"));

    assertEquals(INITIAL, household.members());
  }

  @Test
  void concurrentTransfersAlwaysLeaveExactlyOneOwner() throws Exception {
    Household household = new Household();
    List<String> candidates = List.of("ana", "mihai", "ioana");
    List<Callable<Void>> tasks = new ArrayList<>();
    for (int t = 0; t < 16; t++) {
      String candidate = candidates.get(t % candidates.size());
      tasks.add(
          () -> {
            for (int i = 0; i < 200; i++) {
              household.transferOwnership(candidate);
            }
            return null;
          });
    }

    runConcurrently(tasks);

    long owners = household.members().values().stream().filter(role -> role == Role.OWNER).count();
    assertEquals(1, owners);
  }

  @ParameterizedTest
  @ValueSource(strings = {"Ana", "ANA", "aNa"})
  void namesAreCaseInsensitive(String name) {
    Household household = new Household();

    assertEquals(Optional.of(Role.OWNER), household.roleOf(name));
    assertThrows(IllegalArgumentException.class, () -> household.add(name, Role.CHILD));
    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana"));
  }

  @Test
  void storesNamesInLowercase() {
    Household household = new Household();

    household.add("Radu", Role.ADULT);

    assertTrue(household.members().containsKey("radu"));
    assertFalse(household.members().containsKey("Radu"));
  }

  @Test
  void rejectsChangingTheRoleOfAnUnknownMember() {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.changeRole("radu", Role.ADULT));

    assertEquals(Optional.empty(), household.roleOf("radu"));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1ana", "ana maria", "ana_maria", "ana-maria", "ana-", "ana."})
  void rejectsInvalidNamesWithoutChangingTheMembers(String name) {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.add(name, Role.ADULT));

    assertEquals(3, household.members().size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Radu", "ELENA", "ștefan", "Ăndrei", "radu2", "Δημήτρης", "Иван"})
  void acceptsAnyLetterFollowedByLettersOrDigits(String name) {
    Household household = new Household();

    household.add(name, Role.ADULT);

    assertEquals(Optional.of(Role.ADULT), household.roleOf(name));
  }

  @Test
  void acceptsNamesUpTo64CharactersAndRejectsLongerOnes() {
    Household household = new Household();

    household.add("a".repeat(64), Role.ADULT);

    assertThrows(IllegalArgumentException.class, () -> household.add("a".repeat(65), Role.ADULT));
  }

  @Test
  void rejectsANullRole() {
    Household household = new Household();

    assertThrows(NullPointerException.class, () -> household.add("radu", null));
    assertThrows(NullPointerException.class, () -> household.changeRole("ana", null));
    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana"));
  }

  @Test
  void membersIsAReadOnlySnapshot() {
    Household household = new Household();
    Map<String, Role> snapshot = household.members();

    household.add("radu", Role.ADULT);

    assertThrows(UnsupportedOperationException.class, () -> snapshot.put("x", Role.OWNER));
    assertFalse(snapshot.containsKey("radu"));
  }

  @Test
  void concurrentAddsOfDifferentMembersAreAllKept() throws Exception {
    Household household = new Household();
    int threads = 16;
    int perThread = 100;
    List<Callable<Void>> tasks = new ArrayList<>();
    for (int t = 0; t < threads; t++) {
      int thread = t;
      tasks.add(
          () -> {
            for (int i = 0; i < perThread; i++) {
              household.add("member" + thread + "x" + i, Role.ADULT);
            }
            return null;
          });
    }

    runConcurrently(tasks);

    assertEquals(3 + threads * perThread, household.members().size());
  }

  @Test
  void concurrentAddsOfTheSameMemberSucceedExactlyOnce() throws Exception {
    Household household = new Household();
    List<Callable<Boolean>> tasks = new ArrayList<>();
    for (int t = 0; t < 16; t++) {
      tasks.add(
          () -> {
            try {
              household.add("radu", Role.ADULT);
              return true;
            } catch (IllegalArgumentException duplicate) {
              return false;
            }
          });
    }

    long successes = runConcurrently(tasks).stream().filter(Boolean::booleanValue).count();

    assertEquals(1, successes);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("radu"));
  }

  private static <T> List<T> runConcurrently(List<Callable<T>> tasks) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<T>> futures = new ArrayList<>();
      for (Callable<T> task : tasks) {
        futures.add(
            executor.submit(
                () -> {
                  start.await();
                  return task.call();
                }));
      }
      start.countDown();
      List<T> results = new ArrayList<>();
      for (Future<T> future : futures) {
        results.add(future.get());
      }
      return results;
    } finally {
      executor.shutdownNow();
    }
  }
}
