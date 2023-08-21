package de.rexlmanu.fairytab.tab.entry.luckperms;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.Constants;
import de.rexlmanu.fairytab.tab.TabService;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.utility.HelperUtils;
import java.util.OptionalInt;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LuckPermsTabEntryProvider implements TabEntryProvider {

  private final LuckPerms luckPerms;
  private int highestWeight;

  @Inject
  public LuckPermsTabEntryProvider(Provider<TabService> tabServiceProvider) {
    this.luckPerms = LuckPermsProvider.get();

    EventBus eventBus = this.luckPerms.getEventBus();

    eventBus.subscribe(
        UserDataRecalculateEvent.class,
        event -> {
          this.calculateHighestWeight();
          tabServiceProvider.get().renderAll();
        });

    eventBus.subscribe(
        GroupDataRecalculateEvent.class,
        event -> {
          this.calculateHighestWeight();
        });

    this.calculateHighestWeight();
  }

  public void calculateHighestWeight() {
    this.luckPerms
        .getGroupManager()
        .getLoadedGroups()
        .forEach(
            group -> {
              OptionalInt weight = group.getWeight();
              if (weight.isPresent() && weight.getAsInt() > highestWeight) {
                highestWeight = weight.getAsInt();
              }
            });
  }

  @Override
  public @NotNull TabEntry getEntry(Player player) {
    User user = this.luckPerms.getPlayerAdapter(Player.class).getUser(player);
    Group group = this.luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
    CachedMetaData userMetaData = user.getCachedData().getMetaData();
    CachedMetaData groupMetaData = group.getCachedData().getMetaData();

    NamedTextColor teamColor =
        NamedTextColor.NAMES.value(
            HelperUtils.getOrDefault(
                    NamedTextColor.GRAY.toString(),
                    () -> userMetaData.getMetaValue("tab-color"),
                    () -> groupMetaData.getMetaValue("tab-color"))
                .toLowerCase());

    return new TabEntry(
        group.getName(),
        HelperUtils.getOrDefault(
            "",
            () -> userMetaData.getMetaValue("tab-prefix"),
            userMetaData::getPrefix,
            () -> groupMetaData.getMetaValue("tab-prefix"),
            groupMetaData::getPrefix),
        HelperUtils.getOrDefault(
            "",
            () -> userMetaData.getMetaValue("tab-suffix"),
            userMetaData::getSuffix,
            () -> groupMetaData.getMetaValue("tab-suffix"),
            groupMetaData::getSuffix),
        teamColor,
        this.getTeamName(group.getWeight().orElse(0), player.getName()),
        player.getName());
  }

  public String getTeamName(int weight, String playerName) {
    return String.format(
            "%0" + Integer.toString(this.highestWeight).length() + "d", this.highestWeight - weight)
        + playerName
        + Constants.TEAM_INDICATOR;
  }
}
