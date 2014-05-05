package blackboard.gradle.jmhHelper

import com.blackboard.gradle.BenchmarkJmhTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.tasks.SourceSet
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;


class TestBenchmarkPlugin {

  Project project;

  @Before
  void setUp(){
    project = ProjectBuilder.builder().build();
    project.apply plugin: 'jmh';
  }
	
  @Test
  public void checkPluginAppliedToProject() {
    assertTrue("Project should have JMHPlugin", project.getPlugins().hasPlugin("jmh"));
  }
  
  @Test
  public void checkBenchmarkJMHTaskIsAddedWhenPluginIsApplied() {
    assertNotNull(project.getTasks().getByName("benchmarkJmh"));
    assertTrue("Project is missing task of type BenchmarkJmhTask",project.getTasks().getByName("benchmarkJmh") instanceof BenchmarkJmhTask);
  }

  /* This test is pedantic, I created it to increase my understanding of how the project.apply plugin mechanism works.
     From this, I can see that invoking JMHPlugin causes the JavaPlugin to be applied; this is the desired behavior, as the
     JMHPlugin uses a sourceSet. SourceSets can only exist after invoking the JavaPlugin. */
  @Test
  public void checkThatJavaPluginGetsAppliedWhenUsingJMHPlugin(){
    assertTrue("Project should have JavaPlugin", project.getPlugins().hasPlugin("java")); 
  }
  
  @Test
  public void checkBenchmarkSourceSet(){
    /* sourceSets, is an object of type SourceSetContainer, that appears after invoking apply JMHPlugin.
       This object is created (inherited?) from the java plugin which JMHPlugin invokes. */
    SourceSet tar = null;
    for (SourceSet s : project.sourceSets){
      if (s.getName().equals("benchmark")) {
        tar = s;
        break;
      }
    }
    assertNotNull("Expected benchmark sourceSet to be present", tar);

    /* Verifying that the benchmark sourceSet actually refers to a valid location for JMH benchmarks.
       By our convention, this will be src/benchmark/java/<more folders forming packaage names here>.
       This test is currently not robust, it is just looking for "benchmark" in a filepath somewhere, and 
       can easily be fooled if someone makes a package named benchmark. This is because I don't want to do
       OS detection to determine filepaths in other operating systems. */

    SourceDirectorySet sd = tar.getAllSource();
    HashSet<File> fs = sd.getSrcDirs();
    boolean valid_sourceSet = false;
    for (File f : fs ) {
      if (f.getPath().contains("benchmark")) {
        valid_sourceSet = true;
        break;
      }
    }
    assertTrue(valid_sourceSet);
  }

  @Test
  public void testSourceSetHasRuntimeClasspath() {
    SourceSet s = project.sourceSets.benchmark;
    assertNotNull(s); 
    assertFalse(s.runtimeClasspath.isEmpty());
  }

  @Test
  public void testDescriptionOfTask(){
    Task t = null;
    try {
         t = project.getTasks().getByName("benchmarkJmh");
    } catch (UnknownTaskException e){
      fail("Task benchmarkJmh could not be found by name");
    }
    assertNotNull(t);
    assertNotNull(t.getDescription());
  }

}
