package de.rexlmanu.fairytab.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.tab.renderer.TabRenderer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabService {
  private final Server server;
  private final TabEntryProvider tabEntryProvider;
  private final TabRenderer tabRenderer;
  private final Logger logger;

  public void render(Player viewer) {
    try {
      this.tabRenderer.render(
          viewer,
          this.server.getOnlinePlayers().stream().map(this.tabEntryProvider::getEntry).toList());
    } catch (Exception e) {
      this.logger.error("Failed to render tab for " + viewer.getName(), e);
    }
  }

  public void renderAll() {
    this.server.getOnlinePlayers().forEach(this::render);
  }
}
