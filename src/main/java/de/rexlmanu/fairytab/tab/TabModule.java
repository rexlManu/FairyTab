package de.rexlmanu.fairytab.tab;

import com.google.inject.AbstractModule;
import de.rexlmanu.fairytab.Constants;
import de.rexlmanu.fairytab.tab.entry.TabEntryProvider;
import de.rexlmanu.fairytab.tab.entry.luckperms.LuckPermsTabEntryProvider;
import de.rexlmanu.fairytab.tab.renderer.ScoreboardTabRenderer;
import de.rexlmanu.fairytab.tab.renderer.TabRenderer;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.PluginManager;

@RequiredArgsConstructor
public class TabModule extends AbstractModule {
  private final PluginManager pluginManager;

  @Override
  protected void configure() {
    this.bind(TabRenderer.class).to(ScoreboardTabRenderer.class);

    if (this.pluginManager.getPlugin(Constants.LUCKPERMS_PLUGIN_NAME) != null) {
      this.bind(TabEntryProvider.class).to(LuckPermsTabEntryProvider.class);
    } else {
      throw new RuntimeException("No LayoutRecordProvider found.");
    }
  }
}
