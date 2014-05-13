package com.blackboard.gradle;

import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.plugins.ide.eclipse.EclipsePlugin;
import org.gradle.plugins.ide.eclipse.model.EclipseClasspath;
import org.gradle.plugins.ide.idea.IdeaPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModule;


public class JMHPlugin implements Plugin<Project> {

  public static final String BENCHMARK_JMH_TASK_NAME = "benchmarkJmh";
  public static final String BENCHMARK_SOURCESET_NAME = "benchmark";
  private static final String JMH_CONFIG_NAME = "jmh";
  private static final String JMH_VERSION = "0.5.6";

  private static final String COMPILE_BENCHMARK_NAME = "benchmarkCompile";
  private static final String RUNTIME_BENCHMARK_NAME = "benchmarkRuntime";

  protected Project project;

  public void apply(Project project) {
    // Applying the JavaPlugin gives access to the sourceSets object.
    this.project = project;
    this.project.getPlugins().apply(JavaPlugin.class);

    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

    configureJMHBenchmarkLocation(javaConvention);
    configureDependencies();
    configureIDESupport(javaConvention);
    defineBenchmarkJmhTask();
  }

  private void configureJMHBenchmarkLocation(JavaPluginConvention pluginConvention) {
    SourceSet benchmarkSourceSet = pluginConvention.getSourceSets().create(BENCHMARK_SOURCESET_NAME);
    SourceSet mainSourceSet = this.project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

    benchmarkSourceSet.setCompileClasspath(this.project.files(mainSourceSet.getOutput(), project.getConfigurations().getByName(COMPILE_BENCHMARK_NAME)));
    benchmarkSourceSet.setRuntimeClasspath(this.project.files(mainSourceSet.getOutput(), benchmarkSourceSet.getOutput(),
                                           project.getConfigurations().getByName(RUNTIME_BENCHMARK_NAME)));
  }

  private void configureDependencies() {
    project.repositories(new Closure<Object>(this, this) {
      public Object doCall(Object it) {
        return invokeMethod("mavenCentral", new Object[0]);
      }

      public Object doCall() {
        return doCall(null);
      }

    });
    project.dependencies(new Closure<Object>(this, this) {
      public Object doCall(Object it) {
        invokeMethod("benchmarkCompile", new Object[]{"org.openjdk.jmh:jmh-core:" + JMH_VERSION});
        invokeMethod("compile", new Object[]{"org.openjdk.jmh:jmh-generator-annprocess:" + JMH_VERSION});
        return invokeMethod("benchmarkCompile", new Object[]{"org.openjdk.jmh:jmh-generator-annprocess:" + JMH_VERSION});
      }

      public Object doCall() {
        return doCall(null);
      }

    });
    project.configurations(new Closure<Object>(this, this) {
      public Object doCall(Object it) {
        return JMH_CONFIG_NAME;
      }

      public Object doCall() {
        return doCall(null);
      }

    });

    Configuration benchmarkCompile = this.project.getConfigurations().getByName("benchmarkCompile");
    benchmarkCompile.extendsFrom(this.project.getConfigurations().getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME));
  }

  private void defineBenchmarkJmhTask() {
    Task benchmarkJmhTask = project.getTasks().create(BENCHMARK_JMH_TASK_NAME, BenchmarkJmhTask.class);
    benchmarkJmhTask.setDescription("Runs JMH benchmark tasks");
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileJava"));
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileBenchmarkJava"));
  }

  private void configureIDESupport(JavaPluginConvention javaPluginConvention){

    //Gives Eclipse IDE support to the benchmarkSourceSet if EclipsePlugin is applied to the project.
    if (project.getPlugins().hasPlugin(EclipsePlugin.class)) {
      EclipsePlugin eclipsePlugin = project.getPlugins().getPlugin(EclipsePlugin.class);
      EclipseClasspath oldEclipseClassPath = eclipsePlugin.getModel().getClasspath();

      oldEclipseClassPath.getPlusConfigurations().add(project.getConfigurations().getByName(COMPILE_BENCHMARK_NAME));
      oldEclipseClassPath.getPlusConfigurations().add(project.getConfigurations().getByName(RUNTIME_BENCHMARK_NAME));
      eclipsePlugin.getModel().setClasspath(oldEclipseClassPath);
    }

    //Gives IntelliJ IDE support if IDEA plugin is applied.
    if (project.getPlugins().hasPlugin(IdeaPlugin.class)) {
      IdeaPlugin ideaPlugin = project.getPlugins().getPlugin(IdeaPlugin.class);
      IdeaModule ideaModule = ideaPlugin.getModel().getModule();
      SourceSet benchmarkSourceSet = javaPluginConvention.getSourceSets().getByName(BENCHMARK_SOURCESET_NAME);

      ideaModule.getTestSourceDirs().addAll(benchmarkSourceSet.getAllJava().getSrcDirs());
      ideaModule.getTestSourceDirs().addAll(benchmarkSourceSet.getResources().getSrcDirs());
      //Sets IDEA scopes
      ideaModule.getScopes().get("TEST").get("plus").add(project.getConfigurations().getByName(COMPILE_BENCHMARK_NAME));
      ideaModule.getScopes().get("TEST").get("plus").add(project.getConfigurations().getByName(RUNTIME_BENCHMARK_NAME));
    }

  }

}
