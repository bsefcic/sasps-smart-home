package ro.upb.sasps.smarthome.baseline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Database of household members and their roles, as an ordinary class without the Singleton */
public final class Household {
  private final Map<String, Role> members = new LinkedHashMap<>();

  public Household() {
    members.put("ana", Role.OWNER);
    members.put("mihai", Role.ADULT);
    members.put("ioana", Role.CHILD);
  }

  /** Adds a new member; the household has exactly one owner, so nobody else can be added as one. */
  public synchronized void add(String name, Role role) {
    String lowerCaseName = normalize(name);
    Objects.requireNonNull(role, "role");
    if (role == Role.OWNER) {
      throw new IllegalArgumentException("The household already has an owner: " + owner());
    }
    if (members.putIfAbsent(lowerCaseName, role) != null) {
      throw new IllegalArgumentException("Member already exists: " + name);
    }
  }

  /**
   * Changes the role of an existing member; fails if the member does not exist. The owner role is
   * moved only by {@link #transferOwnership}.
   */
  public synchronized void changeRole(String name, Role role) {
    String lowerCaseName = normalize(name);
    Objects.requireNonNull(role, "role");
    if (role == Role.OWNER) {
      throw new IllegalArgumentException("Use transferOwnership to make " + name + " the owner");
    }
    if (lowerCaseName.equals(owner())) {
      throw new IllegalArgumentException(
          "The role of the owner changes only through transferOwnership");
    }
    if (members.replace(lowerCaseName, role) == null) {
      throw new IllegalArgumentException("Unknown member: " + name);
    }
  }

  /** Removes a member and returns whether it existed; the owner cannot be removed. */
  public synchronized boolean remove(String name) {
    String lowerCaseName = normalize(name);
    if (lowerCaseName.equals(owner())) {
      throw new IllegalArgumentException("The owner cannot be removed");
    }
    return members.remove(lowerCaseName) != null;
  }

  /** Makes an existing member the owner in one atomic step; the previous owner becomes an adult. */
  public synchronized void transferOwnership(String newOwner) {
    String lowerCaseName = normalize(newOwner);
    if (!members.containsKey(lowerCaseName)) {
      throw new IllegalArgumentException("Unknown member: " + newOwner);
    }
    members.replace(owner(), Role.ADULT);
    members.replace(lowerCaseName, Role.OWNER);
  }

  public synchronized Optional<Role> roleOf(String name) {
    return Optional.ofNullable(members.get(normalize(name)));
  }

  /** Read-only copy of the current members, in the order they were added. */
  public synchronized Map<String, Role> members() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(members));
  }

  /** The single owner; the methods above keep exactly one. */
  private String owner() {
    for (Map.Entry<String, Role> member : members.entrySet()) {
      if (member.getValue() == Role.OWNER) {
        return member.getKey();
      }
    }
    throw new IllegalStateException("The household has no owner");
  }

  /** Validates the name and returns its lowercase form, used as the storage and lookup key. */
  private static String normalize(String name) {
    if (name == null || name.isEmpty() || name.length() > 64) {
      throw new IllegalArgumentException("member name must have 1–64 characters");
    }
    int[] characters = name.codePoints().toArray();
    if (!Character.isLetter(characters[0])) {
      throw new IllegalArgumentException("member name must start with a letter");
    }
    for (int i = 1; i < characters.length; i++) {
      int c = characters[i];
      if (!Character.isLetter(c) && !isDigit(c)) {
        throw new IllegalArgumentException(
            "invalid character in member name: '" + Character.toString(c) + "'");
      }
    }
    return name.toLowerCase(Locale.ROOT);
  }

  private static boolean isDigit(int c) {
    return c >= '0' && c <= '9';
  }
}
