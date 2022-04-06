package oscar.controller.util;

import java.util.Arrays;
import java.util.List;

public class ControllerOption {
  private final List<String> aliases;
  private final String description;
  private final String name;

  public ControllerOption(String name, String description, String... aliases) {
    this.name = name;
    this.description = description;
    this.aliases = Arrays.asList(aliases);
  }

  public String getName() {
    return name;
  }

  public List<String> getAliases() {
    return aliases;
  }

  public String getAliasesString() {
    return aliases.stream().reduce("", (a, b) -> a + b + " ");
  }

  public boolean matchesAlias(String alias) {
    return aliases.contains(alias);
  }

  public String getDescription() {
    return description;
  }
}

