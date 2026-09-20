package de.rexlmanu.fairytab.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.nametag.NameTagService;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.tab.renderer.TabRenderer;
import lombok.RequiredArgsConstructor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabService {
  private final Server server;
  private final JavaPlugin plugin;
  private final TabEntryProvider tabEntryProvider;
  private final TabRenderer tabRenderer;
  private final Logger logger;
  private final NameTagService nameTags;

  // Player data and tab updates must run on the player's owning region.
  public void render(Player player) {
    player
        .getScheduler()
        .run(
            this.plugin,
            task -> {
              try {
                var entry = this.tabEntryProvider.getEntry(player);
                this.tabRenderer.render(player, entry);
                this.nameTags.update(player.getUniqueId(), entry);
              } catch (Exception e) {
                this.logger.error("Failed to render tab for " + player.getName(), e);
              }
            },
            null);
  }

  public void renderAll() {
    this.server.getOnlinePlayers().forEach(this::render);
  }
}
