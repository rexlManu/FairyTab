package de.rexlmanu.fairytab.tab;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.rexlmanu.fairytab.nametag.NameTagService;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.tab.renderer.TabRenderer;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.Logger;

class TabServiceTest {
  @Test
  void updatesOnlyInsideThePlayersScheduledTask() {
    Server server = mock(Server.class);
    JavaPlugin plugin = mock(JavaPlugin.class);
    Player player = mock(Player.class);
    EntityScheduler scheduler = mock(EntityScheduler.class);
    TabEntryProvider provider = mock(TabEntryProvider.class);
    TabRenderer renderer = mock(TabRenderer.class);
    when(server.getOnlinePlayers()).thenAnswer(invocation -> List.of(player));
    when(player.getScheduler()).thenReturn(scheduler);
    TabEntry entry = new TabEntry("", "", NamedTextColor.GRAY, 1, "Alex");
    when(provider.getEntry(player)).thenReturn(entry);

    new TabService(
            server, plugin, provider, renderer, mock(Logger.class), mock(NameTagService.class))
        .renderAll();

    ArgumentCaptor<Consumer<ScheduledTask>> action = ArgumentCaptor.captor();
    verify(scheduler).run(eq(plugin), action.capture(), isNull());
    verifyNoInteractions(provider, renderer);
    action.getValue().accept(mock(ScheduledTask.class));
    verify(provider).getEntry(player);
    verify(renderer).render(player, entry);
  }
}
