package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Read-only checks: no test changes the state of the application singleton. */
class HouseholdTest {
  private final Household household = Household.INSTANCE;

  @Test
  void alwaysReturnsTheSameInstance() {
    assertSame(Household.INSTANCE, Household.valueOf("INSTANCE"));
    assertEquals(1, Household.values().length);
  }

  @Test
  void startsWithThreeMembersInTheOrderTheyWereAdded() {
    assertEquals(
        Map.of("ana", Role.OWNER, "mihai", Role.ADULT, "ioana", Role.CHILD), household.members());
    assertEquals(List.of("ana", "mihai", "ioana"), List.copyOf(household.members().keySet()));
  }

  @Test
  void returnsTheRoleOfKnownMembersOnly() {
    assertEquals(Optional.of(Role.OWNER), household.roleOf("ana"));
    assertEquals(Optional.of(Role.ADULT), household.roleOf("mihai"));
    assertEquals(Optional.of(Role.CHILD), household.roleOf("ioana"));
    assertEquals(Optional.empty(), household.roleOf("radu2"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Ana", "ANA", "aNa"})
  void findsMembersRegardlessOfLetterCase(String name) {
    assertEquals(Optional.of(Role.OWNER), household.roleOf(name));
  }

  @Test
  void rejectsEveryChangeThatWouldLeaveAnythingOtherThanOneOwnerAndChangesNothing() {
    Map<String, Role> before = household.members();

    assertThrows(IllegalArgumentException.class, () -> household.add("radu", Role.OWNER));
    assertThrows(IllegalArgumentException.class, () -> household.changeRole("mihai", Role.OWNER));
    assertThrows(IllegalArgumentException.class, () -> household.changeRole("ana", Role.ADULT));
    assertThrows(IllegalArgumentException.class, () -> household.remove("ana"));
    assertThrows(IllegalArgumentException.class, () -> household.transferOwnership("radu"));

    assertEquals(before, household.members());
  }

  @Test
  void membersCannotBeModifiedThroughTheReturnedMap() {
    assertThrows(
        UnsupportedOperationException.class, () -> household.members().put("radu", Role.OWNER));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "1ana", "ana maria", "ana_maria", "ana-maria", "ana-", "ana."})
  void rejectsInvalidNames(String name) {
    assertThrows(IllegalArgumentException.class, () -> household.roleOf(name));
  }

  @ParameterizedTest
  @ValueSource(strings = {"Radu", "ELENA", "ștefan", "andrei", "radu2"})
  void acceptsAnyLetterFollowedByLettersOrDigits(String name) {
    assertEquals(Optional.empty(), household.roleOf(name));
  }

  @Test
  void acceptsNamesUpTo64CharactersAndRejectsLongerOnes() {
    assertEquals(Optional.empty(), household.roleOf("a".repeat(64)));
    assertThrows(IllegalArgumentException.class, () -> household.roleOf("a".repeat(65)));
  }
}
