package de.rexlmanu.fairytab.tab.renderer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.rexlmanu.fairytab.tab.entry.TabEntry;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerListTabRenderer implements TabRenderer {
  private final MiniMessage miniMessage;

  @Override
  public void render(Player player, TabEntry entry) {
    Component name =
        Component.empty()
            .color(entry.color())
            .append(this.miniMessage.deserialize(entry.prefix()))
            .append(Component.text(entry.playerName()))
            .append(this.miniMessage.deserialize(entry.suffix()));
    player.playerListName(name);
    player.setPlayerListOrder(entry.order());
  }
}
