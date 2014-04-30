# JMH Gradle Plugin #
A gradle plugin designed to run JMH benchmarks associated with a project. This plugin was designed with three goals in mind:

1. Create a standard location which logically separates main code from benchmark code.
2. Provide all dependencies necessary to compile and run JMH benchmarks.
3. Provide a gradle task that can be invoked on the command line to run all benchmarks associated with a project.

## How to use the Plugin ##

gdl benchmarkJmh

This task, by default, will run all JMH benchmarks found in the src/benchmark/java/... folder, and,
by default, will produce an output text file, named jmh-output.txt ,in the project build directory.

### Where to put benchmarks ###
The JMH Gradle plugin uses the standard gradle sourceSet layout for managing the location of its source-files. Therefore,
all JMH benchmarks for your project, must be placed in *src/benchmark/java/...*

### Installing the plugin for use in your own build.gradle files ###
1. Clone this repository.
2. run gdl jar
3. Install the jar file created from the gdl jar task into your local maven repository.
    ```mvn install:install-file -Dfile=jmh-gradle-plugin-1.0-SNAPSHOT.jar -DgroupId=blackboard.gradle.jmhHelper  -DartifactId=JMHHelper  -Dversion=1.0-SNAPSHOT -Dpackaging=jar```
4. Add the dependency to your build.gradle file for your project.

```
buildscript {
    repositories {
        mavenLocal()
       }
        dependencies {
            classpath group: 'blackboard.gradle.jmhHelper', name: 'JMHHelper', version: '1.0-SNAPSHOT'
       }
}
    apply plugin: com.blackboard.gradle.JMHPlugin
```
