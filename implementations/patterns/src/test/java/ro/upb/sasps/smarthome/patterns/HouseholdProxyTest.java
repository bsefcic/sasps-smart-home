package ro.upb.sasps.smarthome.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HouseholdProxyTest {
  private static final Map<String, Role> INITIAL =
      Map.of("ana", Role.OWNER, "mihai", Role.ADULT, "ioana", Role.CHILD);

  private final FakeHouseholdMembers household = new FakeHouseholdMembers(INITIAL);

  @Test
  void anOwnerCanAddChangeAndRemoveMembers() {
    HouseholdMembers ana = new HouseholdProxy(household, "ana");

    ana.add("radu", Role.ADULT);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("radu"));

    ana.changeRole("radu", Role.CHILD);
    assertEquals(Optional.of(Role.CHILD), household.roleOf("radu"));

    assertTrue(ana.remove("radu"));
    assertEquals(Optional.empty(), household.roleOf("radu"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"mihai", "ioana"})
  void aMemberWhoIsNotAnOwnerCannotChangeTheListAndNothingChanges(String requester) {
    HouseholdMembers member = new HouseholdProxy(household, requester);

    assertThrows(AccessDeniedException.class, () -> member.add("radu", Role.ADULT));
    assertThrows(AccessDeniedException.class, () -> member.changeRole("ioana", Role.OWNER));
    assertThrows(AccessDeniedException.class, () -> member.remove("ana"));
    assertThrows(AccessDeniedException.class, () -> member.transferOwnership(requester));

    assertEquals(INITIAL, household.members());
  }

  @ParameterizedTest
  @ValueSource(strings = {"ana", "mihai", "ioana"})
  void everyMemberCanReadTheList(String requester) {
    HouseholdMembers member = new HouseholdProxy(household, requester);

    assertEquals(INITIAL, member.members());
    assertEquals(Optional.of(Role.OWNER), member.roleOf("ana"));
  }

  @Test
  void someoneWhoIsNotAMemberCannotReadOrChangeAnything() {
    HouseholdMembers stranger = new HouseholdProxy(household, "radu");

    assertThrows(AccessDeniedException.class, stranger::members);
    assertThrows(AccessDeniedException.class, () -> stranger.roleOf("ana"));
    assertThrows(AccessDeniedException.class, () -> stranger.add("radu", Role.OWNER));

    assertEquals(INITIAL, household.members());
  }

  @Test
  void theRoleIsCheckedOnEveryCall() {
    HouseholdMembers ana = new HouseholdProxy(household, "ana");
    HouseholdMembers mihai = new HouseholdProxy(household, "mihai");

    ana.transferOwnership("mihai");

    assertThrows(AccessDeniedException.class, () -> ana.add("radu", Role.ADULT));
    assertThrows(AccessDeniedException.class, () -> ana.transferOwnership("ana"));
    mihai.add("radu", Role.ADULT);
    assertEquals(Optional.of(Role.ADULT), household.roleOf("radu"));
  }

  @Test
  void aRemovedMemberLosesAccess() {
    HouseholdMembers ioana = new HouseholdProxy(household, "ioana");

    new HouseholdProxy(household, "ana").remove("ioana");

    assertThrows(AccessDeniedException.class, ioana::members);
  }

  @Test
  void deniedCallsOnTheApplicationHouseholdLeaveItUnchanged() {
    Map<String, Role> before = HouseholdProxy.forMember("ana").members();

    assertThrows(
        AccessDeniedException.class, () -> HouseholdProxy.forMember("ioana").remove("ana"));
    assertThrows(
        AccessDeniedException.class,
        () -> HouseholdProxy.forMember("mihai").add("radu", Role.OWNER));
    assertThrows(
        AccessDeniedException.class,
        () -> HouseholdProxy.forMember("ioana").transferOwnership("ioana"));

    assertEquals(before, HouseholdProxy.forMember("ana").members());
  }

  @Test
  void rejectsMissingTargetOrRequester() {
    assertThrows(NullPointerException.class, () -> new HouseholdProxy(null, "ana"));
    assertThrows(NullPointerException.class, () -> new HouseholdProxy(household, null));
  }
}
