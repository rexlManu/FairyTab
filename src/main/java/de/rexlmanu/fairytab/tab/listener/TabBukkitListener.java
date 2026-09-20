package de.rexlmanu.fairytab.tab.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.nametag.NameTagService;
import de.rexlmanu.fairytab.tab.TabService;
import de.rexlmanu.fairytab.utility.autoregister.AutoInit;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Singleton
@AutoInit
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TabBukkitListener implements Listener {
  private final TabService tabService;
  private final NameTagService nameTags;

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  public void handlePlayerJoin(PlayerJoinEvent event) {
    this.tabService.render(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void handlePlayerQuit(PlayerQuitEvent event) {
    this.nameTags.remove(event.getPlayer().getUniqueId());
  }
}
