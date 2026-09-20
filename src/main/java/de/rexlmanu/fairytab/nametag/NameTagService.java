package de.rexlmanu.fairytab.nametag;

import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class NameTagService {
  private final Server server;
  private final JavaPlugin plugin;
  private final PlayerManager packets;
  private final MiniMessage miniMessage;
  private final Map<UUID, NameTag> tags = new ConcurrentHashMap<>();
  private final Map<UUID, Viewer> viewers = new ConcurrentHashMap<>();
  private final AtomicInteger nextTeam = new AtomicInteger();
  private volatile boolean closed;

  private record NameTag(String team, TabEntry entry) {}

  private record Viewer(Player player, Map<String, NameTag> sent, AtomicBoolean queued) {}

  // Call on the subject's region. Other regions receive immutable data, not player reads.
  public void update(UUID playerId, TabEntry entry) {
    if (this.closed) return;
    this.tags.compute(
        playerId,
        (id, previous) ->
            new NameTag(
                previous == null
                    ? "ft-" + Integer.toUnsignedString(this.nextTeam.getAndIncrement(), 16)
                    : previous.team(),
                entry));
    this.refreshAll();
  }

  public void remove(UUID playerId) {
    this.tags.remove(playerId);
    this.viewers.remove(playerId);
    this.refreshAll();
  }

  private void refreshAll() {
    if (this.closed) return;
    for (Player player : this.server.getOnlinePlayers()) {
      UUID id = player.getUniqueId();
      Viewer viewer =
          this.viewers.computeIfAbsent(
              id, ignored -> new Viewer(player, new HashMap<>(), new AtomicBoolean()));
      if (!viewer.queued().compareAndSet(false, true)) continue;
      var task =
          player
              .getScheduler()
              .run(
                  this.plugin,
                  scheduled -> {
                    viewer.queued().set(false);
                    this.render(viewer);
                  },
                  () -> this.viewers.remove(id, viewer));
      if (task == null) this.viewers.remove(id, viewer);
    }
  }

  private void render(Viewer viewer) {
    Player player = viewer.player();
    if (this.closed || !player.isOnline()) return;
    // Disable can run outside this viewer's region, so it shares this lock.
    synchronized (viewer) {
      if (this.closed) return;
      Map<String, NameTag> current = new HashMap<>();
      this.tags.values().forEach(tag -> current.put(tag.team(), tag));
      for (String team : viewer.sent().keySet().toArray(String[]::new)) {
        if (!current.containsKey(team)) {
          this.sendRemove(player, team);
          viewer.sent().remove(team);
        }
      }
      for (NameTag tag : current.values()) {
        NameTag previous = viewer.sent().get(tag.team());
        if (tag.equals(previous)) continue;
        TabEntry entry = tag.entry();
        ScoreBoardTeamInfo info =
            new ScoreBoardTeamInfo(
                Component.empty(),
                this.miniMessage.deserialize(entry.prefix()),
                this.miniMessage.deserialize(entry.suffix()),
                NameTagVisibility.ALWAYS,
                CollisionRule.ALWAYS,
                entry.color(),
                OptionData.NONE);
        this.packets.sendPacket(
            player,
            new WrapperPlayServerTeams(
                tag.team(),
                previous == null ? TeamMode.CREATE : TeamMode.UPDATE,
                info,
                entry.playerName()));
        viewer.sent().put(tag.team(), tag);
      }
    }
  }

  private void sendRemove(Player player, String team) {
    this.packets.sendPacket(
        player, new WrapperPlayServerTeams(team, TeamMode.REMOVE, (ScoreBoardTeamInfo) null));
  }

  public void close() {
    this.closed = true;
    for (Viewer viewer : this.viewers.values()) {
      synchronized (viewer) {
        // PacketEvents sends through Netty. No entity state is changed during disable.
        viewer.sent().keySet().forEach(team -> this.sendRemove(viewer.player(), team));
        viewer.sent().clear();
      }
    }
    this.viewers.clear();
    this.tags.clear();
  }
}
