package de.rexlmanu.fairytab.tab.entry;

import net.kyori.adventure.text.format.NamedTextColor;

public record TabEntry(
    String groupName,
    String prefix,
    String suffix,
    NamedTextColor teamColor,
    String teamName,
    String playerName) {}
