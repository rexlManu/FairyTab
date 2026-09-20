package de.rexlmanu.fairytab;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class FairyTabLoader implements PluginLoader {
  @Override
  public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
    Properties libraries = new Properties();
    try (InputStream input = getClass().getResourceAsStream("/libraries.properties")) {
      if (input == null) {
        throw new IllegalStateException("Missing libraries.properties");
      }
      libraries.load(input);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load library versions", e);
    }

    MavenLibraryResolver resolver = new MavenLibraryResolver();
    for (String name : libraries.stringPropertyNames()) {
      resolver.addDependency(
          new Dependency(new DefaultArtifact(libraries.getProperty(name)), null));
    }
    resolver.addRepository(
        new RemoteRepository.Builder(
                "central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR)
            .build());
    classpathBuilder.addLibrary(resolver);
  }
}
