package de.rexlmanu.fairytab.tab.entry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface TabEntryProvider {
  @NotNull TabEntry getEntry(Player player);
}
