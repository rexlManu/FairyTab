package de.rexlmanu.fairytab.tab.renderer;

import de.rexlmanu.fairytab.tab.entry.TabEntry;
import org.bukkit.entity.Player;

public interface TabRenderer {
  void render(Player player, TabEntry entry);
}
