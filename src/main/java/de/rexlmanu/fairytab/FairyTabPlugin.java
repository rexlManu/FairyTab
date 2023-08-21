package de.rexlmanu.fairytab;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.rexlmanu.fairytab.tab.TabModule;
import de.rexlmanu.fairytab.utility.autoregister.ClassAutoLoader;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class FairyTabPlugin extends JavaPlugin {
  private Injector injector;
  private Metrics metrics;

  @Override
  public void onEnable() {
    this.injector =
        Guice.createInjector(
            new FairyTabModule(this), new TabModule(this.getServer().getPluginManager()));
    this.metrics = new Metrics(this, Constants.BSTATS_SERVICE_ID);

    ClassAutoLoader.init(this.injector, this.getClass().getPackageName());
  }

  @Override
  public void onDisable() {
    this.metrics.shutdown();
  }
}
