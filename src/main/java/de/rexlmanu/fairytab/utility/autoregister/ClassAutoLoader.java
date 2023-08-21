package de.rexlmanu.fairytab.utility.autoregister;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import io.github.classgraph.ClassGraph;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassAutoLoader extends AbstractModule {
  public static void init(Injector injector, String... packageNames) {
    try (var result = new ClassGraph().acceptPackages(packageNames).enableAnnotationInfo().scan()) {
      result
          .getClassesWithAnnotation(AutoInit.class)
          .forEach(
              classInfo -> injector.injectMembers(injector.getInstance(classInfo.loadClass())));
    }
  }
}
