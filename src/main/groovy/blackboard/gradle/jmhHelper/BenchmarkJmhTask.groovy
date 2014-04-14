package blackboard.gradle.jmhHelper

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.plugins.JavaPlugin

class BenchmarkJmhTask extends DefaultTask {
  String outputFile = "$project.buildDir/jmh-output.txt";
  @TaskAction
  def benchmarkJmh() {
    //How do I specifiy in the TaskAction that this is a JavaExec.
    project.plugin.apply(JavaPlugin.class)
    //Find a way to expose commandline options, as the Option annotation is an internal gradle API can can't be used.
    if (project.hasProperty('custargs')){
      args(custargs.split(','))
    }       

    //Actually exec the task.
    main = 'org.openjdk.jmh.Main'
    classPath = project.sourceSets.benchmark.output + project.configurations.benchmarkRuntime
    args = ['-o', outputFile]
  }
}
