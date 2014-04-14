package blackboard.gradle.jmhHelper

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.plugins.JavaPlugin

class JMHPlugin implements Plugin<Project> {

  void apply(Project project){
    //Set benchmarkSourceSet for project
    project.plugins.apply(JavaPlugin.class)
    configureJMHBenchmarkLocation(project)
    //Set the needed dependencies for the JMH Benchmark.java files here
    configureDependencies(project)
    //Actually add the task that does the work.
    project.task('benchmarkJmh', type: BenchmarkJmhTask)    
  }
  
  //Just copying and pasting the sourceSet block doens't work. 
  void configureJMHBenchmarkLocation(Project project) {
   project.getSourceSets().create('benchmark')
  }

  void configureDependencies(Project project) {
	project.dependencies {
		benchmarkCompile "org.openjdk.jmh:jmh-core:0.5.6"
        	benchmarkCompile "org.openjdk.jmh:jmh-generator-annprocess:0.5.6"
        }
  }


} 

