package ro.upb.sasps.smarthome.patterns;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Simple in-memory household for tests, independent of the application singleton. */
final class FakeHouseholdMembers implements HouseholdMembers {
  private final Map<String, Role> members = new LinkedHashMap<>();

  FakeHouseholdMembers(Map<String, Role> initialMembers) {
    members.putAll(initialMembers);
  }

  @Override
  public synchronized void add(String name, Role role) {
    if (members.putIfAbsent(name, role) != null) {
      throw new IllegalArgumentException("Member already exists: " + name);
    }
  }

  @Override
  public synchronized void changeRole(String name, Role role) {
    if (members.replace(name, role) == null) {
      throw new IllegalArgumentException("Unknown member: " + name);
    }
  }

  @Override
  public synchronized boolean remove(String name) {
    return members.remove(name) != null;
  }

  @Override
  public synchronized void transferOwnership(String newOwner) {
    members.replaceAll((name, role) -> role == Role.OWNER ? Role.ADULT : role);
    members.replace(newOwner, Role.OWNER);
  }

  @Override
  public synchronized Optional<Role> roleOf(String name) {
    return Optional.ofNullable(members.get(name));
  }

  @Override
  public synchronized Map<String, Role> members() {
    return Map.copyOf(members);
  }
}
