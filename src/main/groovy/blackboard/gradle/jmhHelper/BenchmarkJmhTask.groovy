package blackboard.gradle.jmhHelper

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.JavaExec;

import java.io.File;

/** This task is used to run JMH benchmark files.  */

class BenchmarkJmhTask extends DefaultTask /*JavaExec */{
  /* Task which identifies all benchmark class files, and 
     runs them with the org.openjdk.jmh.Main runner.   */
  
  private static final JMH_RUNNER = "org.openjdk.jmh.Main"

  private String outputFile = "$project.buildDir/jmh-output.txt";
  private def jexec = new JavaExec();
 
  @TaskAction
  def benchmarkJmh() {
    /*Find a way to expose commandline options, as the Option annotation is an internal gradle API can can't be used.
    if (project.hasProperty("custargs")){
      args(custargs.split(','))
    } */     
    if (project.getConfigurations().getByName('benchmarkRuntime') == null) {
      throw new java.lang.RuntimeException("Missing the benchmarkRuntime configuration");
    }
    jexec.main = JMH_RUNNER
    jexec.classpath = project.sourceSets.benchmark.output + project.configurations.benchmarkRuntime
    new File(outputFile).getParentFile().mkdirs()
    jexec.args(['-o', outputFile])
    jexec.exec();
  }
}
