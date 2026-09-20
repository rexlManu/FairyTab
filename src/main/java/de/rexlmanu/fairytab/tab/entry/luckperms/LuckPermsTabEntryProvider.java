package de.rexlmanu.fairytab.tab.entry.luckperms;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.tab.TabService;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.utility.HelperUtils;
import java.util.Locale;
import java.util.Objects;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LuckPermsTabEntryProvider implements TabEntryProvider {

  private final LuckPerms luckPerms;

  @Inject
  public LuckPermsTabEntryProvider(JavaPlugin plugin, Provider<TabService> tabServiceProvider) {
    this.luckPerms = LuckPermsProvider.get();
    EventBus eventBus = this.luckPerms.getEventBus();
    eventBus.subscribe(
        plugin, UserDataRecalculateEvent.class, event -> tabServiceProvider.get().renderAll());
    eventBus.subscribe(
        plugin, GroupDataRecalculateEvent.class, event -> tabServiceProvider.get().renderAll());
  }

  @Override
  public @NotNull TabEntry getEntry(Player player) {
    User user = this.luckPerms.getPlayerAdapter(Player.class).getUser(player);
    Group group = this.luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
    CachedMetaData userMetaData = user.getCachedData().getMetaData();
    CachedMetaData groupMetaData =
        group == null ? userMetaData : group.getCachedData().getMetaData();

    NamedTextColor teamColor =
        Objects.requireNonNullElse(
            NamedTextColor.NAMES.value(
                HelperUtils.getOrDefault(
                        NamedTextColor.GRAY.toString(),
                        () -> userMetaData.getMetaValue("tab-color"),
                        () -> groupMetaData.getMetaValue("tab-color"))
                    .toLowerCase(Locale.ROOT)),
            NamedTextColor.GRAY);

    return new TabEntry(
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
        this.getOrder(group == null ? 0 : group.getWeight().orElse(0)),
        player.getName());
  }

  // Dense ranks keep negative and extreme weights within the API's nonnegative range.
  private int getOrder(int weight) {
    return 1
        + (int)
            this.luckPerms.getGroupManager().getLoadedGroups().stream()
                .mapToInt(group -> group.getWeight().orElse(0))
                .filter(groupWeight -> groupWeight < weight)
                .distinct()
                .count();
  }
}
