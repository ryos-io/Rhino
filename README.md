
<p align="center">
  <a href="https://gitter.im/ryos-io/Rhino"><img src="https://badges.gitter.im/ryos-io/Rhino.svg" border=0></a>
  <img src="https://travis-ci.org/ryos-io/Rhino.svg?branch=master" />
  <img src="https://img.shields.io/badge/rhino--core-2.2.0.M1-72c247" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" />
  <img src="https://sonarcloud.io/api/project_badges/measure?project=ryos-io_Rhino&metric=alert_status"/>
  <a href="https://javadoc.io/doc/io.ryos.rhino/rhino-core"><img src="https://javadoc.io/badge2/io.ryos.rhino/rhino-core/javadoc.svg" border=0></a>
</p>

## Rhino: Dockerized Load and Performance Testing for Web Services

Rhino is a lightweight annotation-based JUnit-style load and performance testing framework tailored 
for 
testing web services which consists of the Rhino Java SDK as well as a collection of libraries
 and tools which enable developers to develop load and performance tests very fast. The Rhino can be 
 added as library dependency into your project or the Rhino Maven archetype can be used to create a new maven project. The deployable artifact will be a **Docker** container which can be run on container orchestration platforms like **Mesos, Kubernetes, AWS ECS**, and so on.
 
<p align="center">
  <img src="https://github.com/bagdemir/rhino/blob/master/rhino_grafana.png"  width="882"/>
</p>

> **_1.5.0:_** Grafana/InfluxDB Integration is included!


With **Influx DB** and **Grafana** Integration, you can also monitor the current load testing and watch how services under test behaves under certain load pattern.  
**Rhino's philosophy is:**

* one repository for project's source code as well as Unit, Integration, Load and Performance Tests.
* to speed up load and performance test development,
* to afford a debuggable test framework written in Java, so that you can go through the breakpoints while 
investigating problems in your load testing code, 
* to provide a Cloud-native load testing environment, that is elastic, scalable, covering region and environment- aware load scenarios,
* to enable the integration with the existing code, e.g your integration test frameworks to 
make them reusable in your load and performance test,
* to provide an intuitive framework so that the engineers do not need to re-learn the language, or
the framework every time they need to write new load tests.

Considering all these aspects, we began with Project Rhino in 2018 and it is available as F/OSS  with Apache 2.0 License, now.


## How to use?

Add maven dependency into your project:

```xml
<dependency>
  <groupId>io.ryos.rhino</groupId>
  <artifactId>rhino-core</artifactId>
  <version>2.2.0.M1</version>
</dependency>
```

For more information about project setup: [Getting Started with Rhino Load Testing](https://github.com/bagdemir/rhino/wiki/Getting-Started).

## How do the load tests look like?

Simulation is a test entity which will be executed and generates load according to the 
implementation provided in the test classes against the instance under test, e.g a web 
service. So as to create a new simulation entity, create a plain Java object with `@Simulation` 
annotation: 

```java
@Simulation(name = "Example Simulation")
public class PerformanceTestingExample {
}
```

The simulation above does nothing unless we add DSL methods into it. DSL methods are  
annotated with `@Dsl` annotation which contain encompass DSL method calls and returns a LoadDsl instance. A simulation
might have multiple DSL methods defined which will be materialized into reactive components to build a load generation pipeline:

```java
@Simulation(name = "Server-Status Simulation")
public class RhinoEntity {

  @Provider(factory = UUIDProvider.class)
  private UUIDProvider uuidProvider;

  @Dsl(name = "Health")
  public DslBuilder performHealth() {
    return dsl()
        .run(http("Health API Call")
            .header(c -> from(X_REQUEST_ID, "Rhino-" + UUID.randomUUID().toString()))
            .endpoint(TARGET)
            .get()
            .saveTo("result"));
  }
}
```

The name of the simulation is important. In a performance testing project, it is very likely that 
you will have multiple simulations. Rhino does know which simulation is to be run from the 
simulation name provided, so the names must be unique. 

A simple Rhino application would look like:
```java
import io.ryos.rhino.sdk.Simulation;

public class Rhino {

    public static void main(String ... args) {
        Simulation.create(PROPS, SIM_NAME).start();
    }
}
```

## How to run? 

If you choose to use the Rhino archetype, the maven project is configured to create a Docker container:

```shell
$ mvn -e clean install
$ docker run -t yourproject:latest
```


Questions/Contributions?
---

Feel free to fork the project and make contributions in terms of Pull Requests. For bigger 
proposals, architectural discussions and bug reports, use the Github's issue tracker.

Sponsors
--- 

<a href="https://www.jetbrains.com/?from=Ryos"><img src="https://user-images.githubusercontent.com/1160613/92041635-e01bd400-ed78-11ea-8fa4-3c2325fc680b.png" width="100"></a>
