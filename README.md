# JMH Gradle Plugin #
A gradle plugin designed to run JMH benchmarks associated with a project. This plugin was designed with three goals in mind:

1. Create a standard location which logically separates main code from benchmark code.
2. Provide all dependencies necessary to compile and run JMH benchmarks.
3. Provide a gradle task that can be invoked on the command line to run all benchmarks associated with a project.

## How to use the Plugin ##

```
 gdl benchmarkJmh
```

This task, by default, will run all JMH benchmarks found in the src/benchmark/java/... folder, and,
by default, will produce an output text file, named `jmh-output.txt`, in the project build directory.

## How to choose a specific benchmark ##

By default, the benchmarkJmh task will run all benchmarks in the sourceSet. Sometimes this is undesirable, when you  
only want to run a small set of benchmarks, or a specific benchmark. You may give the benchmarkJmh task a regular expression to  
match a specific benchmark or group of benchmarks with the -regexp option.

```
gdl benchmarkJmh -P-regexp=".*SomeSpecificBenchmarkName.*"
```

It is important to include the first .* and last .* around the name, as JMH generates several supporting class files  
per benchmark class. In general, if your benchmark class is MyBenchmark.java, a regex of .*MyBenchmark.* will run that benchmark.

### Where to put benchmarks ###
The JMH Gradle plugin uses the standard gradle sourceSet layout for managing the location of its source-files. Therefore,
all JMH benchmarks for your project, must be placed in *src/benchmark/java/...*


### Using the plugin on an existing Blackboard project ###
The plugin is located in maven.pd.local and is currently included as part of the Blackboard Common plugin. If you are working  
on a blackboard project, you already have access to the benchmarkJmh task, and don't have to do anything.


### Adding the Plugin to a non-blackboard project ###
If you are connected to blackboard's network, adding the following buildscript block to your project under test will
give you access to the benchmarkJmh task, by installing the necessary artifacts to your local maven repository.

```
buildscript {
    repositories {
            maven {
                url "http://maven.pd.local/content/repositories/snapshots"
            }
       }
        dependencies {
            classpath group: 'com.blackboard.gradle', name: 'jmh-gradle-plugin', version: '1.1-SNAPSHOT'
       }
}
    apply plugin: com.blackboard.gradle.JMHPlugin
```
### Command Line Options ###
Command line options are specified as gradle project parameters, prefaced with -P.
To see which parameters are accepted by JMH, run

```gdl benchmarkJmh -Phelp```

All the commands that JMH supports can be specified in this manner. (Not all commands have been tested, your mileage may vary)
For example, specifying the name of the output file can be done by doing the following:

`
gdl benchmarkJmh -P-o="different_name.txt"
`

>Caution, the output file option just changes the name, not the directory of the output file.

Multiple options may be specified:

`gdl benchmarkJmh -P-wi=3 -P-i=2 -P-o="different_name.txt"`


In this case, the number of warmup iterations be test has been changed to 3,  
the number of measurement iterations have been changed to 2,
and the output of the tests is going to a file named `different_name.txt`
