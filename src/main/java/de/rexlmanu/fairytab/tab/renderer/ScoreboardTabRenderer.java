package de.rexlmanu.fairytab.tab.renderer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.Constants;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScoreboardTabRenderer implements TabRenderer {
  private final MiniMessage miniMessage;

  @Override
  public void render(Player viewer, List<TabEntry> tabEntries) {
    Scoreboard scoreboard = viewer.getScoreboard();

    List<String> teamNames = tabEntries.stream().map(TabEntry::teamName).toList();
    Set<String> existingScoreboardTeamNames =
        scoreboard.getTeams().stream()
            .map(Team::getName)
            .filter(
                name ->
                    name.endsWith(
                        Constants.TEAM_INDICATOR)) // we only want to remove teams that we created
            .collect(Collectors.toSet());

    // Create or update teams for the tab entries
    for (TabEntry tabEntry : tabEntries) {
      Team team = getOrCreateTeam(scoreboard, tabEntry.teamName());
      team.prefix(this.miniMessage.deserialize(tabEntry.prefix()));
      team.suffix(this.miniMessage.deserialize(tabEntry.suffix()));
      team.color(tabEntry.teamColor());
      if (!team.hasEntry(tabEntry.playerName())) {
        team.addEntry(tabEntry.playerName());
      }
    }

    // Remove teams that are not needed anymore
    for (String teamName : existingScoreboardTeamNames) {
      Team team = scoreboard.getTeam(teamName);
      if (!teamNames.contains(teamName) && team != null) {
        team.unregister();
      }
    }

    viewer.setScoreboard(scoreboard);
  }

  public static Team getOrCreateTeam(Scoreboard scoreboard, String teamName) {
    return Optional.ofNullable(scoreboard.getTeam(teamName))
        .orElseGet(() -> scoreboard.registerNewTeam(teamName));
  }
}
