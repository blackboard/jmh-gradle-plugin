package blackboard.gradle.jmhHelper

import com.blackboard.gradle.BenchmarkJmhTask
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec;
import org.gradle.testfixtures.ProjectBuilder

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
    assertTrue(task instanceof JavaExec);
  }

  @Test
  public void testApplyJVMArgs(){
    project.apply plugin: com.blackboard.gradle.JMHPlugin
    project.setProperty("-jvmArgs", "-Xmx2048m -Xms1024m");
    BenchmarkJmhTask t = task;
    t.benchmarkJmh();
    assertTrue(t.getMinHeapSize().equals("1024m"));
    assertTrue(t.getMaxHeapSize().equals("2048m"));
  }

  @Test
  public void testPrintDefaultJvmArgs(){
    project.apply plugin: com.blackboard.gradle.JMHPlugin
    BenchmarkJmhTask t = task;
    println "Banana"
    t.getAllJvmArgs().each { println it}
  }
} 
