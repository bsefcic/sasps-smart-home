package ro.upb.sasps.smarthome.patterns;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Protection proxy: the household as seen by one member. Every current member can read the list;
 * only the owner can add, change or remove members and hand the owner role to someone else.
 */
public final class HouseholdProxy implements HouseholdMembers {
  private final HouseholdMembers target;
  private final String requester;

  public HouseholdProxy(HouseholdMembers target, String requester) {
    this.target = Objects.requireNonNull(target, "target");
    this.requester = Objects.requireNonNull(requester, "requester");
  }

  /** The household of the application, as seen by the given member. */
  public static HouseholdProxy forMember(String requester) {
    return new HouseholdProxy(Household.INSTANCE, requester);
  }

  @Override
  public void add(String name, Role role) {
    synchronized (target) {
      requireOwner();
      target.add(name, role);
    }
  }

  @Override
  public void changeRole(String name, Role role) {
    synchronized (target) {
      requireOwner();
      target.changeRole(name, role);
    }
  }

  @Override
  public boolean remove(String name) {
    synchronized (target) {
      requireOwner();
      return target.remove(name);
    }
  }

  @Override
  public void transferOwnership(String newOwner) {
    synchronized (target) {
      requireOwner();
      target.transferOwnership(newOwner);
    }
  }

  @Override
  public Optional<Role> roleOf(String name) {
    synchronized (target) {
      requireMember();
      return target.roleOf(name);
    }
  }

  @Override
  public Map<String, Role> members() {
    synchronized (target) {
      requireMember();
      return target.members();
    }
  }

  private void requireOwner() {
    if (requireMember() != Role.OWNER) {
      throw new AccessDeniedException(requester + " is not an owner");
    }
  }

  private Role requireMember() {
    return target
        .roleOf(requester)
        .orElseThrow(() -> new AccessDeniedException(requester + " is not a household member"));
  }
}
