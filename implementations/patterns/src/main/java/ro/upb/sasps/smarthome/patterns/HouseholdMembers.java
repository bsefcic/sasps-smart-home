package ro.upb.sasps.smarthome.patterns;

import java.util.Map;
import java.util.Optional;

/** Operations on the members of the household; implemented by the database and its proxy. */
public interface HouseholdMembers {
  void add(String name, Role role);

  void changeRole(String name, Role role);

  boolean remove(String name);

  void transferOwnership(String newOwner);

  Optional<Role> roleOf(String name);

  Map<String, Role> members();
}
