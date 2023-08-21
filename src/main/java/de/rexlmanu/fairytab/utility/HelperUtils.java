package de.rexlmanu.fairytab.utility;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

public class HelperUtils {
  @SafeVarargs
  public static String getOrDefault(String defaultValue, Supplier<@Nullable String>... suppliers) {
    return Arrays.stream(suppliers)
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(defaultValue);
  }
}
