package ro.upb.sasps.smarthome.patterns;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Database of household members and their roles. */
public enum Household {
  INSTANCE;

  private final Map<String, Role> members = new LinkedHashMap<>();

  Household() {
    members.put("ana", Role.OWNER);
    members.put("mihai", Role.ADULT);
    members.put("ioana", Role.CHILD);
  }

  public synchronized void add(String name, Role role) {
    String lowerCaseName = normalize(name);
    Objects.requireNonNull(role, "role");
    if (members.putIfAbsent(lowerCaseName, role) != null) {
      throw new IllegalArgumentException("Member already exists: " + name);
    }
  }

  /** Changes the role of an existing member; fails if the member does not exist. */
  public synchronized void changeRole(String name, Role role) {
    String key = normalize(name);
    Objects.requireNonNull(role, "role");
    if (members.replace(key, role) == null) {
      throw new IllegalArgumentException("Unknown member: " + name);
    }
  }

  /** Removes a member and returns whether it existed. */
  public synchronized boolean remove(String name) {
    return members.remove(normalize(name)) != null;
  }

  public synchronized Optional<Role> roleOf(String name) {
    return Optional.ofNullable(members.get(normalize(name)));
  }

  /** Read-only copy of the current members, in the order they were added. */
  public synchronized Map<String, Role> members() {
    return Collections.unmodifiableMap(new LinkedHashMap<>(members));
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
