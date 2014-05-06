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
  //private static final String DEFAULT_INIT_HEAP_SIZE = "-Xms2048m";
  //private static final String DEFAULT_MAX_HEAP_SIZE = "-Xms2048m";
  private static final String OS_TYPE = System.getProperty("os.name").contains("windows")? "windows" : "linux" ;
  private String defaultOutputFile = String.valueOf(getProject().getBuildDir()) + File.separator + "jmh-output.txt";
  private JavaExec jexec = new JavaExec();

  private final HashSet<String> VALID_JMH_ARGS = new HashSet<>(Arrays.asList("-bm", "-bs", "-e","-f", "-foe","-gc", "-h", "-i", "-jvm", "-jvmArgs", "-jvmArgsAppend", "-jvmArgsPrepend", "-l", "-lprof", "-lrf", "-o", "-p", "-prof", "-r", "-rf", "-rff", "-si", "-t","-tg","-tu","-v", "-wbs", "-wf", "-wi", "-wm", "-wmb"));

  @TaskAction
  public void benchmarkJmh() {

    jexec.setMain(JMH_RUNNER);

    /* Sets the classpath for the JMH runner. This requires the output of the benchmark sourceSet
     * as well as the runtime-classpath of the benchmarks. */
    JavaPluginConvention jpc = this.getProject().getConvention().getPlugin(JavaPluginConvention.class);
    FileCollection fcClasspath = jpc.getSourceSets().getByName("benchmark").getRuntimeClasspath();
    //fcClasspath.add(jpc.getSourceSets().getByName("benchmark").getOutput());
    jexec.setClasspath(fcClasspath);
    //Sends arguments defined in the gradle syntax of -P to the JMH runner. Example: -P-o="/my_path/text.txt"
    jexec.setArgs(processArgs());
    jexec.exec();
  }

  private ArrayList<String> processArgs(){
    ArrayList<String> toJmhRunner = new ArrayList<>();
    Project pj = this.getProject();
    HashSet<String> props = new HashSet<>(pj.getProperties().keySet());
    /* Changes props to be the set-intersection of all project properties and VALID_JMH_ARGS. This gives me only the
     * the arguments passed into the project that are JMH arguments. (As opposed to all project arguments that may
     * exist from gradle doing its magic) */
    props.retainAll(VALID_JMH_ARGS);


    /*Adds args and their values to the list to be given to JMHRunner. (Minus the help and -o property, as I'm doing
    * my own manipulation of those arguments.) */
    props.remove("-o");
    props.remove("help");
    for (String prop : props){
        toJmhRunner.add(prop);
        toJmhRunner.add((String) pj.getProperties().get(prop));
    }

    //TODO: The create logic to change the output to a safe location other than the project build directory.
    if (pj.hasProperty("-o")){
      String fName = pj.getBuildDir() + File.separator + pj.getProperties().get("-o");
      new File(fName);
      toJmhRunner.add("-o");
      toJmhRunner.add(fName);
    } else {
      new File(defaultOutputFile).getParentFile().mkdirs();
      toJmhRunner.addAll(new ArrayList<>(Arrays.asList("-o", defaultOutputFile)));
    }


    //WARN: Blackboard specific code to follow inside of this if statement.
    if (pj.hasProperty("bbTestServiceConfig") && pj.hasProperty("bbHome")){
     int index = toJmhRunner.indexOf("-jvmArgs");
      if (index == -1){
        toJmhRunner.add("-jvmArgs");
        toJmhRunner.add("-Dbbservices_config=" +(String) pj.getProperties().get("bbTestServiceConfig"));
        //toJmhRunner.add(DEFAULT_INIT_HEAP_SIZE);
        //toJmhRunner.add(DEFAULT_MAX_HEAP_SIZE);
      } else {
        toJmhRunner.add(index+ 1,"-Dbbservices_config=" + (String) pj.getProperties().get("bbTestServiceConfig"));
        //toJmhRunner.add(index + 1, DEFAULT_INIT_HEAP_SIZE);
        //toJmhRunner.add(index + 1, DEFAULT_MAX_HEAP_SIZE);
      }
    }

    //Otherwise, we are in some other project, not depending on a gradle.properties file.

    //Help is displayed in the console, clears all other options.
    if (pj.hasProperty("help")){
      displayUsage();
      toJmhRunner.clear();
      toJmhRunner.add("-h");
    }

    return toJmhRunner;
  }

  private void displayUsage(){
    System.out.println("This task depends on the fact that all benchmarks are compilable, valid, java jmh benchmarks.");
    System.out.println("For this task to function, benchmarks must be placed in src/benchmark/java/<your own folder structure here>");
    System.out.println("Arguments to this task are defined with the gradle -P syntax.");
    System.out.println("By default, the output of JMH benchmarks are located in jmh-output.txt in the build directory of this project.");
  }

}
