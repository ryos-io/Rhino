The goal of the document is provide a comprehensive guide for programmers and contributors whose 
aim is to write performance tests as well as contribute Rhino framework. Rhino is 
started as a hobby project in Hamburg, Germany early 2019 and matured with regular contributions.

### Prerequisites


Before you get started:

* Rhino Java framework libraries are compiled with JDK 11. So, the dependencies attached to your project must be compatible with Java 11. 

* Rhino projects are built as Docker containers, so you will need Docker on your machine to test your tests, as well.

### What is Rhino Performance Testing Framework

Rhino performance testing framework is a sub-project within the Rhino umbrella project, that 
enables developers to write load and performance tests in Java in JUnit fashion. With annotation 
based development model, the performance test developers can provide metadata for the Rhino tests 
and configure them with annotations afforded by the framework. 


### Creating your first project

You can create Rhino projects, by using Rhino Archetype. Maven archetype project allows 
developers to create new Rhino performance testing projects. Currently under development. 
If you want to use the archetype to create a new Rhino performance testing project, you need to install the snapshot version in your local Maven repository, e.g 

```bash
mvn -e clean install
```

after having successfully installed the archetype, you can now create a new Rhino project:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.ryos.rhino \
  -DarchetypeArtifactId=rhino-archetype \
  -DarchetypeVersion=1.1.8 \
  -DgroupId=my-group-id \
  -DartifactId=my-artifact-id
```
For groupId, you need to enter the group-id of your project like io.ryos.myteam and the 
artifactId is some artifactId specific to the project like `my-test-project`. 
After creating the performance testing project, you now can open it up in your IDE and start off 
writing some performance tests.

Select the project directory created by the Maven archetype and after the IDE completes with 
opening the project, in the project window you will find project files with which you can set about 
implementing your performance tests.

### Writing your first test

Rhino projects does comprise a main-method to run simulations and simulation
entities, that are annotated with Rhino annotations. An example application might look as follows: 

```java
class Application {
  private static final String CLASSPATH_RHINO_PROPERTIES = "classpath:///rhino.properties";
  private static final String SIMULATION = "Example Simulation";

  public static void main() {
        var simulationMetadata = new SimulationSpecImpl(CLASSPATH_RHINO_PROPERTIES, SIMULATION);
        simulationMetadata.start();
  }
}
```
The `SimulationSpecImpl` does expect a configuration file in classpath - classpath:// prefix is important nonetheless only classpath configuration files are supported currently, and the name of the simulationMetadata to run. The name of the simulationMetadata 
must match the name, set in Simulation annotation:

```java
@Simulation(name = "Example Simulation")
@Logging(file = "/foo/bar.out", formatter = GatlingLogFormatter.class)
@Influx
public class PerformanceTestingExample {

  @UserFeeder(max = 1, factory = IMSUserRepositoryFactoryImpl.class)
  private User user;

  @SessionFeeder
  private UserSession userSession;

  @Feeder(factory = UUIDFeeder.class)
  private String uuid;

  @Before
  public void setUp() {
    // setup code
  }

  @Scenario(name = "Hello World")
  public void testHelloWorld(Measurement measurement) {
    // hello world
  }

  @Scenario(name = "Health Check")
  public void testHealthCheck(Measurement measurement) {
    // healthcheck 
  }

  @After
  public void cleanUp() {
    // clean up code
  }
}

```

The configuration properties file does contain test application properties like in which package 
to look up the Simulation entities and further configuration options described in Configuration 
section: 

```properties
packageToScan=com.myproject.tests
testName=HelloWorld

```