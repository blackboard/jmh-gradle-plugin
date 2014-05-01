package com.blackboard.gradle;

import groovy.lang.Closure;
import org.gradle.api.NamedDomainObjectCollection;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;




public class JMHPlugin implements Plugin<Project> {

  public static final String BENCHMARK_JMH_TASK_NAME = "benchmarkJmh";
  public static final String BENCHMARK_SOURCESET_NAME = "benchmark";
  private static final String JMH_CONFIG_NAME = "jmh";
  private static final String JMH_VERSION = "0.5.6";


  protected Project project;

  public void apply(Project project) {
    // Applying the JavaPlugin gives access to the sourceSets object.
    this.project = project;
    this.project.getPlugins().apply(JavaPlugin.class);


    JavaPluginConvention javaConvention = project.getConvention().getPlugin(JavaPluginConvention.class);

    configureJMHBenchmarkLocation(javaConvention);
    configureDependencies();
    defineBenchmarkJmhTask();
  }

  private void configureJMHBenchmarkLocation(JavaPluginConvention pluginConvention) {
    SourceSet benchmark = pluginConvention.getSourceSets().create(BENCHMARK_SOURCESET_NAME);
    SourceSet mainSourceSet = this.project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().getByName("main");
    benchmark.getCompileClasspath().plus(mainSourceSet.getOutput());
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

//    sourceSets {
//      benchmark {
//        compileClasspath += main.output + test.output
//        runtimeClasspath += main.output + test.output
//      }
//    }





    Configuration benchmarkCompile = this.project.getConfigurations().getByName("benchmarkCompile");
    benchmarkCompile.extendsFrom(this.project.getConfigurations().getByName(JavaPlugin.COMPILE_CONFIGURATION_NAME));




  }

  private void defineBenchmarkJmhTask() {
    Task benchmarkJmhTask = project.getTasks().create(BENCHMARK_JMH_TASK_NAME, BenchmarkJmhTask.class);
    benchmarkJmhTask.setDescription("Runs JMH benchmark tasks");
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileJava"));
    benchmarkJmhTask.dependsOn(project.getTasks().getByName("compileBenchmarkJava"));
  }

}
