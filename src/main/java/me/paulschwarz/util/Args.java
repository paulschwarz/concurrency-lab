package me.paulschwarz.util;

import java.util.List;
import java.util.Optional;

public class Args {

  private final List<String> args;

  public Args(List<String> args) {
    this.args = args;
  }

  public String get(String key, String defaultValue) {
    return find(key)
        .orElse(defaultValue);
  }

  public int get(String key, Integer defaultValue) {
    return find(key)
        .map(value -> {
          try {
            return Integer.parseInt(value);
          } catch (NumberFormatException e) {
            throw new RuntimeException(
                String.format("Expected \"%s\" to have an integer value. Unable to parse \"%s\"",
                    key, value), e);
          }
        })
        .orElse(defaultValue);
  }

  private Optional<String> find(String key) {
    final String prefix = String.format("--%s=", key);

    return args.stream().filter(entry -> entry.startsWith(prefix))
        .map(value -> value.replaceFirst(prefix, ""))
        .findFirst();
  }
}
