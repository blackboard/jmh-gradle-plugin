package com.blackboard.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This task is used to run JMH benchmark files.
 */
public class BenchmarkJmhTask extends DefaultTask {
  @TaskAction
  public void benchmarkJmh() {
    /*Find a way to expose commandline options, as the Option annotation is an internal gradle API can can't be used.
    if (project.hasProperty("custargs")){
      args(custargs.split(','))
    } */
    if (getProject().getConfigurations().getByName("benchmarkRuntime") == null) {
      throw new RuntimeException("Missing the benchmarkRuntime configuration");
    }

    jexec.setMain(JMH_RUNNER);
    JavaPluginConvention jpc = this.getProject().getConvention().getPlugin(JavaPluginConvention.class);

    FileCollection fcClasspath = jpc.getSourceSets().getByName("benchmark").getRuntimeClasspath();
    fcClasspath.add(jpc.getSourceSets().getByName("benchmark").getOutput());


    jexec.setClasspath(fcClasspath);

    //jexec.setClasspath(project.sourceSets.benchmark.output + getProject().getConfigurations().getByName("benchmarkRuntime"));
    new File(outputFile).getParentFile().mkdirs();
    jexec.args(new ArrayList<String>(Arrays.asList("-o", outputFile)));
    jexec.exec();
  }

  private static final String JMH_RUNNER = "org.openjdk.jmh.Main";
  private String outputFile = String.valueOf(getProject().getBuildDir()) + "/jmh-output.txt";
  private JavaExec jexec = new JavaExec();
}
