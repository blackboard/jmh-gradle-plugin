package blackboard.gradle.jmhHelper

import org.gradle.api.Action;
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention;
//import org.openjdk.jmh.Main
import org.gradle.api.artifacts.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

class JMHPlugin implements Plugin<Project> {
    
  public static final BENCHMARK_JMH_TASK_NAME = "benchmarkJmh";
  public static final BENCHMARK_SOURCESET_NAME = "benchmark";

  private static final JMH_CONFIG_NAME = "jmh";
  private static final JMH_RUNNER = "org.openjdk.jmh.Main";
  private static final JMH_VERSION = "0.5.6";
  //private static final JMH_VERSION = "1.0-SNAPSHOT";
  
  private Project project;
  private benchmarkJmhTask;

  void apply(Project project) {
    // Applying the JavaPlugin gives access to the sourceSets object. 
    project.plugins.apply(JavaPlugin.class)
    this.project = project;

    configureJMHBenchmarkLocation()
    configureDependencies()
    defineBenchmarkJmhTask()
  }
  
  private void configureJMHBenchmarkLocation() {
   SourceSet benchmark = project.getSourceSets().create(BENCHMARK_SOURCESET_NAME);
   //benchmark.setCompileClasspath(project.files(project.sourceSets.main.getOutput(), project.getConfigurations().getByName("benchmarkCompile")));
   //benchmark.setRuntimeClasspath(project.files(benchmark.getOutput() , project.sourceSets.main.getOutput(), project.getConfigurations().getByName("benchmarkRuntime")));   
  }

  private void configureDependencies() {
    project.dependencies {
      benchmarkCompile "org.openjdk.jmh:jmh-core:$JMH_VERSION"
      benchmarkCompile "org.openjdk.jmh:jmh-generator-annprocess:$JMH_VERSION"
    }
    project.configurations {
      jmh  
    }
  }

  private void defineBenchmarkJmhTask() {
    /* JavaPluginConvention jpc = project.getConvention().getPlugin(JavaPluginConvention.class);
    project.getTasks().withType(BenchmarkJmhTask.class, new Action<BenchmarkJmhTask>() {
      public void execute(final BenchmarkJmhTask task) {
        task.getConventionMapping.map("benchmarkClassesDir", new Callable<Object>() {
          public Object call() throws Exception {
            return jpc.getSourceSets().getByName("benchmark").getOutput().getClassesDir();
          }
        });
        task.getConventionMapping.map("classpath", new Callable<Object>() {
          public Object call() throws Exception {
            return jpc.getSourceSets.getByName("benchmark").getRuntimeClasspath();
          }
        });
        task.getConventionMapping.map("benchmarkSrcDirs", new Callable<Object>() {
          public Object call() throws Exception {
            return new ArrayList<File>(jpc.getSourceSets().getByName("benchmark").getJava().getSrcDirs());
          }
        });
      }
    }); */
    benchmarkJmhTask = project.task(BENCHMARK_JMH_TASK_NAME, description: "Runs provided JMH Benchmark Tests", type: BenchmarkJmhTask);
    benchmarkJmhTask.dependsOn(project.tasks.compileJava);
    //benchmarkJmhTask.setMain(JMH_RUNNER);
  }


} 

