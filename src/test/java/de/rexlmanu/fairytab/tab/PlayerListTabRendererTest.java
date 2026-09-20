package de.rexlmanu.fairytab.tab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import de.rexlmanu.fairytab.tab.entry.TabEntry;
import de.rexlmanu.fairytab.tab.renderer.PlayerListTabRenderer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PlayerListTabRendererTest {
  @Test
  void formatsMetadataAndKeepsThePlayerNameLiteral() {
    Player player = mock(Player.class);
    TabEntry entry =
        new TabEntry(
            "<red>[Admin] </red>", "<gold>!</gold>", NamedTextColor.GREEN, 3, "<blue>Alex");

    new PlayerListTabRenderer(MiniMessage.miniMessage()).render(player, entry);

    ArgumentCaptor<Component> name = ArgumentCaptor.forClass(Component.class);
    verify(player).playerListName(name.capture());
    assertEquals(
        Component.empty()
            .color(NamedTextColor.GREEN)
            .append(Component.text("[Admin] ", NamedTextColor.RED))
            .append(Component.text("<blue>Alex"))
            .append(Component.text("!", NamedTextColor.GOLD)),
        name.getValue());
    verify(player).setPlayerListOrder(3);
    verifyNoMoreInteractions(player);
  }
}
