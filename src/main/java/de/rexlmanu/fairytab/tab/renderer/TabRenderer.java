package de.rexlmanu.fairytab.tab.renderer;

import de.rexlmanu.fairytab.tab.entry.TabEntry;
import java.util.List;
import org.bukkit.entity.Player;

public interface TabRenderer {
  void render(Player viewer, List<TabEntry> tabEntries);
}
