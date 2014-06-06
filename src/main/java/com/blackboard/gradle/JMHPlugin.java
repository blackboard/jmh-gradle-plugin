package com.blackboard.gradle;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModule;

import java.io.File;
import java.util.Collection;
import java.util.Set;

public class JMHPlugin implements Plugin<Project> {
  public static final String BENCHMARK_JMH_TASK_NAME = "benchmarkJmh";
  public static final String BENCHMARK_SOURCESET_NAME = "benchmark";
  private static final String JMH_VERSION = "0.8";

  private static final String COMPILE_BENCHMARK_NAME = "benchmarkCompile";
  private static final String RUNTIME_BENCHMARK_NAME = "benchmarkRuntime";
  private static final String JMH_CONFIGURATION_NAME = "jmh";

  protected Project project;

  public void apply(Project project) {
    this.project = project;
    this.project.getPlugins().apply(JavaPlugin.class);

    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

    configureJMHBenchmarkLocation(javaConvention);
    configureConfigurations();
    configureIDESupport(javaConvention);
    defineBenchmarkJmhTask();
  }

  private void configureJMHBenchmarkLocation(JavaPluginConvention pluginConvention) {
    SourceSet benchmarkSourceSet = pluginConvention.getSourceSets().create(BENCHMARK_SOURCESET_NAME);
    SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
    SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);

    ConfigurationContainer configurations = project.getConfigurations();
    benchmarkSourceSet.setCompileClasspath(project.files(mainSourceSet.getOutput(), testSourceSet.getOutput(), configurations.getByName(COMPILE_BENCHMARK_NAME)));
    benchmarkSourceSet.setRuntimeClasspath(project.files(mainSourceSet.getOutput(), testSourceSet.getOutput(), benchmarkSourceSet.getOutput(), configurations.getByName(RUNTIME_BENCHMARK_NAME)));
  }

  private void configureConfigurations() {
    project.getRepositories().add(project.getRepositories().mavenCentral());
    ConfigurationContainer configurations = project.getConfigurations();
    Configuration benchmarkCompile = configurations.getByName("benchmarkCompile");
    benchmarkCompile.extendsFrom(configurations.getByName(JavaPlugin.TEST_COMPILE_CONFIGURATION_NAME));

    Configuration jmh = configurations.create(JMH_CONFIGURATION_NAME);
    DependencyHandler dependencies = project.getDependencies();
    dependencies.add(JMH_CONFIGURATION_NAME, "org.openjdk.jmh:jmh-core:" + JMH_VERSION);
    dependencies.add(JMH_CONFIGURATION_NAME, "org.openjdk.jmh:jmh-generator-annprocess:" + JMH_VERSION);
    for (Dependency dependency : jmh.getDependencies()) {
      benchmarkCompile.getDependencies().add(dependency);
    }
  }

  private void defineBenchmarkJmhTask() {
    Task benchmarkJmhTask = project.getTasks().create(BENCHMARK_JMH_TASK_NAME, BenchmarkJmhTask.class);
    benchmarkJmhTask.setDescription("Runs JMH benchmark tasks");
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileJava"));
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileBenchmarkJava"));
  }

  /**
   * Add IDE support for benchmarks in test scopes if the IntelliJ or Eclipse plugins are available.
   */
  private void configureIDESupport(final JavaPluginConvention javaPluginConvention) {
    final ConfigurationContainer configurations = project.getConfigurations();
    final PluginContainer plugins = project.getPlugins();

    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
        if (plugins.hasPlugin(EclipsePlugin.class)) {
          EclipsePlugin eclipsePlugin = plugins.getPlugin(EclipsePlugin.class);
          EclipseClasspath eclipseClasspath = eclipsePlugin.getModel().getClasspath();
          eclipseClasspath.getPlusConfigurations().add(configurations.getByName(COMPILE_BENCHMARK_NAME));
          eclipseClasspath.getPlusConfigurations().add(configurations.getByName(RUNTIME_BENCHMARK_NAME));
          eclipsePlugin.getModel().setClasspath(eclipseClasspath);
        }

        if (plugins.hasPlugin(IdeaPlugin.class)) {
          IdeaPlugin ideaPlugin = plugins.getPlugin(IdeaPlugin.class);
          IdeaModule ideaModule = ideaPlugin.getModel().getModule();
          SourceSet benchmarkSourceSet = javaPluginConvention.getSourceSets().getByName(BENCHMARK_SOURCESET_NAME);

          Set<File> testSourceDirs = ideaModule.getTestSourceDirs();
          testSourceDirs.addAll(benchmarkSourceSet.getAllJava().getSrcDirs());
          testSourceDirs.addAll(benchmarkSourceSet.getResources().getSrcDirs());
          ideaModule.setTestSourceDirs(testSourceDirs);
          Collection<Configuration> testPlusScope = ideaModule.getScopes().get("TEST").get("plus");
          testPlusScope.add(configurations.getByName(COMPILE_BENCHMARK_NAME));
          testPlusScope.add(configurations.getByName(RUNTIME_BENCHMARK_NAME));
        }
      }
    });
  }
}
