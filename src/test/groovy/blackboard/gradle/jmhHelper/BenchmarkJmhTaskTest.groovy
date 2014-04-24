package blackboard.gradle.jmhHelper

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.DefaultTask;

import org.junit.Test;
import org.junit.Before;
import org.junit.Ignore;
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

  @Ignore
  @Test
  public void testTaskExec() {
    project.apply plugin: "JMHPlugin"
    println "In the test";
    task.benchmarkJmh();
  }
  
} 
