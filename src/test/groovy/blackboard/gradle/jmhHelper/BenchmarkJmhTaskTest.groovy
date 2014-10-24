package blackboard.gradle.jmhHelper

import com.blackboard.gradle.BenchmarkJmhTask
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.DefaultTask;

import org.junit.Test;
import org.junit.Before

import static org.junit.Assert.*;


class BenchmarkJmhTaskTest {

  Project project;
  def task;

  @Before
  public void setUp() {
    project = ProjectBuilder.builder().build(); 
    task = project.task('custom', type: BenchmarkJmhTask);
  }

  @Test
  public void canToAddTaskToProject() {
    assertTrue(task instanceof BenchmarkJmhTask)
  }

  @Test
  public void testTaskIsTypeJavaExec() {
    assertTrue(task instanceof DefaultTask); 
  }

  @Test
  public void testHelpArg() {
    project.apply plugin: 'jmh'
    project.getExtensions().add("help", "")
    task.benchmarkJmh();
  }

  @Test
  public void setExtraJvmArgs_setsArgumentsISet(){
    project.apply plugin: 'jmh'
    BenchmarkJmhTask t = task;
    t.setExtraJvmArgs("bogusArg");
    assertEquals("Expected extraJvmArgs to be set to bogusArg",  "bogusArg", t.getExtraJvmArgs());
  }

} 
