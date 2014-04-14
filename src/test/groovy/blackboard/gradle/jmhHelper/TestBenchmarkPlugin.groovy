package blackboard.gradle.jmhHelper

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import static org.junit.Assert.*

class TestBenchmarkPlugin {
	
	@Test
	public void BenchmarkJmhPluginAddsTaskToProject() {
	  Project project = ProjectBuilder.builder().build();
	  project.apply plugin: 'JMHPlugin'
	  assertTrue(project.tasks.benchmarkJmh instanceof BenchmarkJmhTask);
	}	
}
