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
        Map.of("ana", Role.OWNER, "mihai", Role.ADULT, "ioana", Role.CHILD),
        household.members("ana"));
    assertEquals(List.of("ana", "mihai", "ioana"), List.copyOf(household.members("ana").keySet()));
  }

  @Test
  void eachInstanceIsASeparateDatabase() {
    Household first = new Household();
    Household second = new Household();

    first.remove("ana", "ioana");

    assertNotSame(first, second);
    assertEquals(Optional.empty(), first.roleOf("ana", "ioana"));
    assertEquals(Optional.of(Role.CHILD), second.roleOf("ana", "ioana"));
  }

  @Test
  void addsChangesAndRemovesMembers() {
    Household household = new Household();

    household.add("ana", "radu", Role.ADULT);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("ana", "radu"));
    assertEquals(
        List.of("ana", "mihai", "ioana", "radu"), List.copyOf(household.members("ana").keySet()));

    household.changeRole("ana", "radu", Role.CHILD);
    assertEquals(Optional.of(Role.CHILD), household.roleOf("ana", "radu"));

    assertTrue(household.remove("ana", "radu"));
    assertEquals(Optional.empty(), household.roleOf("ana", "radu"));
    assertFalse(household.remove("ana", "radu"));
  }

  @Test
  void changingTheRoleKeepsThePositionAndReAddingMovesToTheEnd() {
    Household household = new Household();

    household.changeRole("ana", "mihai", Role.CHILD);
    assertEquals(List.of("ana", "mihai", "ioana"), List.copyOf(household.members("ana").keySet()));

    household.remove("ana", "mihai");
    household.add("ana", "mihai", Role.ADULT);
    assertEquals(List.of("ana", "ioana", "mihai"), List.copyOf(household.members("ana").keySet()));
  }

  @Test
  void nobodyElseCanBeAddedOrPromotedAsOwner() {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.add("ana", "radu", Role.OWNER));
    assertThrows(
        IllegalArgumentException.class, () -> household.changeRole("ana", "mihai", Role.OWNER));

    assertEquals(INITIAL, household.members("ana"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana", "ANA"})
  void theOwnerCannotBeRemovedOrDemoted(String owner) {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.remove("ana", owner));
    assertThrows(
        IllegalArgumentException.class, () -> household.changeRole("ana", owner, Role.ADULT));

    assertEquals(INITIAL, household.members("ana"));
  }

  @Test
  void transferringOwnershipMakesThePreviousOwnerAnAdult() {
    Household household = new Household();

    household.transferOwnership("ana", "Mihai");

    assertEquals(
        Map.of("ana", Role.ADULT, "mihai", Role.OWNER, "ioana", Role.CHILD),
        household.members("ana"));
    assertThrows(IllegalArgumentException.class, () -> household.remove("mihai", "mihai"));
    assertTrue(household.remove("mihai", "ana"));
  }

  @Test
  void transferringOwnershipToTheCurrentOwnerChangesNothing() {
    Household household = new Household();

    household.transferOwnership("ana", "ana");

    assertEquals(INITIAL, household.members("ana"));
  }

  @Test
  void transferringOwnershipToAnUnknownMemberChangesNothing() {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.transferOwnership("ana", "radu"));

    assertEquals(INITIAL, household.members("ana"));
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
              for (String requester : candidates) {
                try {
                  household.transferOwnership(requester, candidate);
                } catch (AccessDeniedException notTheOwner) {
                  // only the current owner can hand the role on
                }
              }
            }
            return null;
          });
    }

    runConcurrently(tasks);

    long owners =
        household.members("ana").values().stream().filter(role -> role == Role.OWNER).count();
    assertEquals(1, owners);
  }

  @Test
  void theOwnerCanAddChangeAndRemoveMembers() {
    Household household = new Household();

    household.add("Ana", "radu", Role.ADULT);
    household.changeRole("ANA", "radu", Role.CHILD);
    assertEquals(Optional.of(Role.CHILD), household.roleOf("ana", "radu"));
    assertTrue(household.remove("ana", "radu"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"mihai", "ioana"})
  void aMemberWhoIsNotTheOwnerCannotChangeTheListAndNothingChanges(String requester) {
    Household household = new Household();

    assertThrows(AccessDeniedException.class, () -> household.add(requester, "radu", Role.ADULT));
    assertThrows(
        AccessDeniedException.class, () -> household.changeRole(requester, "ioana", Role.ADULT));
    assertThrows(AccessDeniedException.class, () -> household.remove(requester, "ioana"));
    assertThrows(
        AccessDeniedException.class, () -> household.transferOwnership(requester, requester));

    assertEquals(INITIAL, household.members("ana"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana", "mihai", "ioana"})
  void everyMemberCanReadTheList(String requester) {
    Household household = new Household();

    assertEquals(INITIAL, household.members(requester));
    assertEquals(Optional.of(Role.OWNER), household.roleOf(requester, "ana"));
  }

  @Test
  void someoneWhoIsNotAMemberCannotReadOrChangeAnything() {
    Household household = new Household();

    assertThrows(AccessDeniedException.class, () -> household.members("radu"));
    assertThrows(AccessDeniedException.class, () -> household.roleOf("radu", "ana"));
    assertThrows(AccessDeniedException.class, () -> household.add("radu", "radu", Role.ADULT));

    assertEquals(INITIAL, household.members("ana"));
  }

  @Test
  void rightsFollowTheCurrentRole() {
    Household household = new Household();

    household.transferOwnership("ana", "mihai");

    assertThrows(AccessDeniedException.class, () -> household.add("ana", "radu", Role.ADULT));
    household.add("mihai", "radu", Role.ADULT);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("mihai", "radu"));
  }

  @Test
  void aRemovedMemberLosesAccess() {
    Household household = new Household();

    household.remove("ana", "ioana");

    assertThrows(AccessDeniedException.class, () -> household.members("ioana"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Ana", "ANA", "aNa"})
  void namesAreCaseInsensitive(String name) {
    Household household = new Household();

    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana", name));
    assertThrows(IllegalArgumentException.class, () -> household.add("ana", name, Role.CHILD));
    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana", "ana"));
  }

  @Test
  void storesNamesInLowercase() {
    Household household = new Household();

    household.add("ana", "Radu", Role.ADULT);

    assertTrue(household.members("ana").containsKey("radu"));
    assertFalse(household.members("ana").containsKey("Radu"));
  }

  @Test
  void rejectsChangingTheRoleOfAnUnknownMember() {
    Household household = new Household();

    assertThrows(
        IllegalArgumentException.class, () -> household.changeRole("ana", "radu", Role.ADULT));

    assertEquals(Optional.empty(), household.roleOf("ana", "radu"));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1ana", "ana maria", "ana_maria", "ana-maria", "ana-", "ana."})
  void rejectsInvalidNamesWithoutChangingTheMembers(String name) {
    Household household = new Household();

    assertThrows(IllegalArgumentException.class, () -> household.add("ana", name, Role.ADULT));

    assertEquals(3, household.members("ana").size());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Radu", "ELENA", "ștefan", "Ăndrei", "radu2", "Δημήτρης", "Иван"})
  void acceptsAnyLetterFollowedByLettersOrDigits(String name) {
    Household household = new Household();

    household.add("ana", name, Role.ADULT);

    assertEquals(Optional.of(Role.ADULT), household.roleOf("ana", name));
  }

  @Test
  void acceptsNamesUpTo64CharactersAndRejectsLongerOnes() {
    Household household = new Household();

    household.add("ana", "a".repeat(64), Role.ADULT);

    assertThrows(
        IllegalArgumentException.class, () -> household.add("ana", "a".repeat(65), Role.ADULT));
  }

  @Test
  void rejectsANullRole() {
    Household household = new Household();

    assertThrows(NullPointerException.class, () -> household.add("ana", "radu", null));
    assertThrows(NullPointerException.class, () -> household.changeRole("ana", "ana", null));
    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana", "ana"));
  }

  @Test
  void membersIsAReadOnlySnapshot() {
    Household household = new Household();
    Map<String, Role> snapshot = household.members("ana");

    household.add("ana", "radu", Role.ADULT);

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
              household.add("ana", "member" + thread + "x" + i, Role.ADULT);
            }
            return null;
          });
    }

    runConcurrently(tasks);

    assertEquals(3 + threads * perThread, household.members("ana").size());
  }

  @Test
  void concurrentAddsOfTheSameMemberSucceedExactlyOnce() throws Exception {
    Household household = new Household();
    List<Callable<Boolean>> tasks = new ArrayList<>();
    for (int t = 0; t < 16; t++) {
      tasks.add(
          () -> {
            try {
              household.add("ana", "radu", Role.ADULT);
              return true;
            } catch (IllegalArgumentException duplicate) {
              return false;
            }
          });
    }

    long successes = runConcurrently(tasks).stream().filter(Boolean::booleanValue).count();

    assertEquals(1, successes);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("ana", "radu"));
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
