package de.rexlmanu.fairytab.tab.entry;

import net.kyori.adventure.text.format.NamedTextColor;

public record TabEntry(
    String prefix, String suffix, NamedTextColor color, int order, String playerName) {}
