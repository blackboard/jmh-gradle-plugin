package blackboard.gradle.jmhHelper

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test
import static org.junit.Assert.*

class BenchmarkJmhTaskTest {
	
	@Test
	public void canToAddTaskToProject() {

	Project project = ProjectBuilder.builder().build()
	def task = project.task('custom', type: BenchmarkJmhTask)
	assertTrue(task instanceof BenchmarkJmhTask)
	}	
} 
