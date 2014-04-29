package com.blackboard.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.UnknownDomainObjectException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


/**
 * This task is used to run JMH benchmark files. By default, this task runs every
 * benchmark for a project in the src/benchmark/java/... folder.
 */
public class BenchmarkJmhTask extends DefaultTask {

  private static final String JMH_RUNNER = "org.openjdk.jmh.Main";
  private String defaultOutputFile = String.valueOf(getProject().getBuildDir()) + "/jmh-output.txt";
  private JavaExec jexec = new JavaExec();

  private final HashSet<String> VALID_JMH_ARGS = new HashSet<>(Arrays.asList("-bm", "-bs", "-e","-f", "-foe","-gc", "-h", "-i", "-jvm", "-jvmArgs", "-jvmArgsAppend", "-jvmArgsPrepend", "-l", "-lprof", "-lrf", "-o", "-p", "-prof", "-r", "-rf", "-rff", "-si", "-t","-tg","-tu","-v", "-wbs", "-wf", "-wi", "-wm", "-wmb"));


  private static final String[] allowedArgs = {"customOutputFile", "help", "-bm"};

  @TaskAction
  public void benchmarkJmh() throws UnknownDomainObjectException {

    jexec.setMain(JMH_RUNNER);

    /* Sets the classpath for the JMH runner. This requires the output of the benchmark sourceSet
     * as well as the runtime-classpath of the benchmarks. */
    JavaPluginConvention jpc = this.getProject().getConvention().getPlugin(JavaPluginConvention.class);
    FileCollection fcClasspath = jpc.getSourceSets().getByName("benchmark").getRuntimeClasspath();
    fcClasspath.add(jpc.getSourceSets().getByName("benchmark").getOutput());
    jexec.setClasspath(fcClasspath);

    //Sends arguments defined in the gradle syntax of -P to the JMH runner. Example: -PcustomOutputFile="/my_path/text.txt"
    jexec.setArgs(processArgs());

    jexec.exec();
  }

  private ArrayList<String> processArgs(){
    ArrayList<String> toJmhRunner = new ArrayList<>();
    Project pj = this.getProject();

    HashSet<String> props = new HashSet<>(pj.getProperties().keySet());
    /* Changes props to be the set-intersection of all project properties and VALID_JMH_ARGS. This gives me only the
     * the arguments passed into the project that are JMH arguments. (As opposed to all project arguments that may
     * exist from gradle doing its magic */
    props.retainAll(VALID_JMH_ARGS);

    for (String prop : props){
      if (!prop.equals("help") || !prop.equals("customOutputFile")) {
        toJmhRunner.add(prop);
        toJmhRunner.add((String) pj.getProperties().get(prop));
      }
    }

    /*if (this.getProject().hasProperty("customOutputFile")) {
      toJmhRunner.addAll(processOutputFileLocation((String) this.getProject().getProperties().get("customOutputFile")));
    } else { //Project doesn't have a customOutput file location specified, so use the default location.
      toJmhRunner.addAll(processOutputFileLocation(defaultOutputFile));
    } */

    if (pj.hasProperty("help")){
      displayUsage();
      toJmhRunner.clear();
      toJmhRunner.add("-h");
    }


    //TODO: The create logic to change the output to a safe location other than the project build directory.
    if (pj.hasProperty("-o")){
      System.err.println("Trying to set custom output location!");
      System.err.println("Custom output locations appear to freak-out gradle.");
      System.err.println((String) pj.getProperties().get("-o"));
    } else {
      new File(defaultOutputFile).getParentFile().mkdirs();
      toJmhRunner.addAll(new ArrayList<>(Arrays.asList("-o", defaultOutputFile)));
    }

    return toJmhRunner;
  }

  private void displayUsage(){
    System.out.println("This task depends on the fact that all benchmarks are compilable, valid, java jmh benchmarks.");
    System.out.println("For this task to function, benchmarks must be placed in src/benchmark/java/<your own folder structure here>");
    System.out.println("Arguments to this task are defined with the gradle -P syntax.");
    System.out.println("By default, the output of JMH benchmarks are located in jmh-output.txt in the build directory of this project.");
    System.out.println("Arguments to this task are as follows:");
    for (String s : allowedArgs){
      System.out.println(s);
    }
  }


}