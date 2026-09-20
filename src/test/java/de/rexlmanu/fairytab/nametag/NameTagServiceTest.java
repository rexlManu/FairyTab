package de.rexlmanu.fairytab.nametag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.server.ServerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class NameTagServiceTest {
  private final Server server = mock(Server.class);
  private final JavaPlugin plugin = mock(JavaPlugin.class);
  private final PlayerManager packets = mock(PlayerManager.class);
  private final Player viewer = mock(Player.class);
  private final EntityScheduler scheduler = mock(EntityScheduler.class);
  private final ArrayDeque<Consumer<ScheduledTask>> pending = new ArrayDeque<>();
  private final UUID subject = UUID.randomUUID();
  private final TabEntry initial =
      new TabEntry("<red>[Admin] </red>", "!", NamedTextColor.GOLD, 1, "Alex");
  private NameTagService service;

  @BeforeEach
  void setup() {
    PacketEventsAPI<?> api = mock(PacketEventsAPI.class);
    ServerManager serverManager = mock(ServerManager.class);
    when(api.getServerManager()).thenReturn(serverManager);
    when(serverManager.getVersion()).thenReturn(ServerVersion.V_26_2);
    PacketEvents.setAPI(api);
    when(server.getOnlinePlayers()).thenAnswer(invocation -> List.of(viewer));
    when(viewer.getUniqueId()).thenReturn(UUID.randomUUID());
    when(viewer.isOnline()).thenReturn(true);
    when(viewer.getScheduler()).thenReturn(scheduler);
    when(scheduler.run(eq(plugin), any(), any()))
        .thenAnswer(
            invocation -> {
              pending.add(invocation.getArgument(1));
              return mock(ScheduledTask.class);
            });
    service = new NameTagService(server, plugin, packets, MiniMessage.miniMessage());
  }

  @AfterEach
  void resetApi() {
    PacketEvents.setAPI(null);
  }

  private void tick() {
    while (!pending.isEmpty()) pending.remove().accept(mock(ScheduledTask.class));
  }

  @Test
  void createsUpdatesAndRemovesTeamsWithoutDuplicateCreates() {
    service.update(subject, initial);
    service.update(subject, initial);
    assertEquals(1, pending.size());
    verifyNoInteractions(packets);
    tick();
    service.update(subject, initial);
    tick();
    service.update(
        subject, new TabEntry("<blue>[Mod] </blue>", "?", NamedTextColor.GREEN, 2, "Alex"));
    tick();
    service.remove(subject);
    tick();

    ArgumentCaptor<WrapperPlayServerTeams> sent =
        ArgumentCaptor.forClass(WrapperPlayServerTeams.class);
    verify(packets, times(3)).sendPacket(eq(viewer), sent.capture());
    var messages = sent.getAllValues();
    assertEquals(
        List.of(TeamMode.CREATE, TeamMode.UPDATE, TeamMode.REMOVE),
        messages.stream().map(WrapperPlayServerTeams::getTeamMode).toList());
    assertEquals(1, messages.stream().map(WrapperPlayServerTeams::getTeamName).distinct().count());
    assertEquals(List.of("Alex"), List.copyOf(messages.getFirst().getPlayers()));
    var info = messages.getFirst().getTeamInfo().orElseThrow();
    assertEquals(Component.text("[Admin] ", NamedTextColor.RED), info.getPrefix());
    assertEquals(NamedTextColor.GOLD, info.getColor());
    assertEquals(NamedTextColor.GREEN, messages.get(1).getTeamInfo().orElseThrow().getColor());
  }

  @Test
  void joiningViewerReceivesExistingAndNewNameTags() {
    service.update(subject, initial);
    tick();
    Player joining = mock(Player.class);
    UUID joiningId = UUID.randomUUID();
    when(joining.getUniqueId()).thenReturn(joiningId);
    when(joining.isOnline()).thenReturn(true);
    when(joining.getScheduler()).thenReturn(scheduler);
    when(server.getOnlinePlayers()).thenAnswer(invocation -> List.of(viewer, joining));

    service.update(joiningId, new TabEntry("", "", NamedTextColor.GRAY, 1, "Steve"));
    tick();

    ArgumentCaptor<WrapperPlayServerTeams> sent =
        ArgumentCaptor.forClass(WrapperPlayServerTeams.class);
    verify(packets, times(2)).sendPacket(eq(joining), sent.capture());
    assertTrue(
        sent.getAllValues().stream().allMatch(packet -> packet.getTeamMode() == TeamMode.CREATE));
    assertEquals(
        java.util.Set.of("Alex", "Steve"),
        sent.getAllValues().stream()
            .flatMap(packet -> packet.getPlayers().stream())
            .collect(java.util.stream.Collectors.toSet()));
    verify(packets, times(2)).sendPacket(eq(viewer), any(WrapperPlayServerTeams.class));
  }

  @Test
  void removesSentTeamsOnDisableAndIgnoresQueuedUpdates() {
    service.update(subject, initial);
    tick();
    service.update(subject, initial);
    service.close();
    tick();
    service.update(subject, initial);
    assertTrue(pending.isEmpty());
    ArgumentCaptor<WrapperPlayServerTeams> sent =
        ArgumentCaptor.forClass(WrapperPlayServerTeams.class);
    verify(packets, times(2)).sendPacket(eq(viewer), sent.capture());
    assertEquals(TeamMode.REMOVE, sent.getValue().getTeamMode());
  }
}
