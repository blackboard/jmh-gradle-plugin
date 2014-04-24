package blackboard.gradle.jmhHelper

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.process.ExecResult;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.plugins.JavaPlugin;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//import org.openjdk.jmh.Main;

//import org.apache.commons.exec.*;
import java.io.File;


class BenchmarkJmhTask extends DefaultTask /*JavaExec */{
  /* Task which identifies all benchmark class files, and 
     runs them with the org.openjdk.jmh.Main runner.   */
  
  private static final JMH_RUNNER = "org.openjdk.jmh.Main"

  String outputFile = "$project.buildDir/jmh-output.txt";
  def jexec = new JavaExec();
 
  @TaskAction
  def benchmarkJmh() {
    /*Find a way to expose commandline options, as the Option annotation is an internal gradle API can can't be used.
    if (project.hasProperty("custargs")){
      args(custargs.split(','))
    } */     
    if (project.getConfigurations().getByName('benchmarkRuntime') == null) {
      throw new java.lang.RuntimeException("Missing the benchmarkRuntime configuration");
    }
    jexec.classpath = project.sourceSets.benchmark.output + project.configurations.compile  /*project.configurations.runtimeClasspath*/
    jexec.main = "$JMH_RUNNER"
    jexec.args(['-h'])
    //jexec.setIgnoreExitValue(true);
    println "In the task, I really hope jexec can find the main class now.";
    jexec.exec();
    //classPath = project.sourceSets.benchmark.output + project.configurations.benchmarkRuntime
    //Find a way for the plugin to include the actual org.openjdk.jmh.Main in the plugin jar file.
    //Actually exec the task.
    //this.setMain("org.openjdk.jmh.Main"); 
    //args = ['-h'];
    // Options opt = new OptionsBuilder().include(".*").forks(1).build();
    //new Runner(opt).run();
    
  }

}
