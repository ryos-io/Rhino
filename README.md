<p align="right">
  <img src="https://github.com/ryos-io/Rhino/blob/master/jetbrains.png"  width="90"/>
</p>
<p align="center">
  <img src="https://github.com/bagdemir/rhino/blob/master/rhino_works.png"  width="300"/>
</p>

<p align="center">
  <a href="https://gitter.im/ryos-io/Rhino"><img src="https://badges.gitter.im/ryos-io/Rhino.svg" border=0></a>
  <img src="https://travis-ci.org/ryos-io/Rhino.svg?branch=master" />
  <img src="https://img.shields.io/badge/rhino--core-1.6.2-72c247" />
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" />
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

<p align="center">
<img src="http://ryos.io/static/integration.jpg" width="640"/>
</p>

**Rhino's philosophy is:**

* one repository for project's source code as well as Unit, Integration, Load and Performance Tests.
* to speed up load and performance test development,
* to afford a debuggable test framework written in Java, so that you can go through the breakpoints while 
investigating problems in your load testing code, 
* to provide a Cloud-native which is elastic, scalable, covering region and environment aware load scenarios,
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
  <version>1.6.2</version>
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

The simulation above does nothing unless we add some scenarios to it. A scenario is a method 
annotated with `@Scenario` annotation and contains the implementation of the load generation. A simulation
might have multiple scenarios defined which are run during testing, independently and parallel:

```java
@Simulation(name = "Server-Status Simulation")
@UserRepository(factory = OAuthUserRepositoryFactory.class)
public class RhinoEntity {
  
  // Some http client
  private Client client = ClientBuilder.newClient();

  @Provider(factory = UUIDProvider.class)
  private String uuid;

  @Scenario(name = "Health")
  public void performHealth(final Measurement measurement) {
    var response = client
            .target(TARGET)
            .request()
            .header(X_REQUEST_ID, "Rhino-" + uuid)
            .get();

    measurement.measure("Health API Call", response.getStatus());
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

    private static final String PROPS = "classpath:///rhino.properties";
    private static final String SIM_NAME = "Server-Status Simulation";

    public static void main(String ... args) {
        Simulation.create(PROPS, SIM_NAME).start();
    }
}
```

[Simulations and Scenarios](https://github.com/bagdemir/Rhino/wiki/Simulations-and-Scenarios)

## How to run? 

If you choose to use the Rhino archetype, the maven project is configured to create a Docker container:

```shell
$ mvn -e clean install
$ docker run -t yourproject:latest
```


## Getting Started

If you think that the Rhino is the right framework for you, you can follow the wiki to get started:

* [Getting Started with Rhino](https://github.com/bagdemir/rhino/wiki/Getting-Started)
* [Simulations](https://github.com/ryos-io/Rhino/wiki/Simulations) - The annotated load testing entities.
* [Providers](https://github.com/bagdemir/rhino/wiki/Providers) - Data feeders used at injection points in Simulations.
* [Configuration](https://github.com/bagdemir/rhino/wiki/Configuration) - Configure your load testing project.
* [Test Users in Simulations](https://github.com/bagdemir/rhino/wiki/Testing-with-Users) - Users in Simulations.
* [Cross-region Tests](https://github.com/ryos-io/Rhino/wiki/Cross-region-Tests) - Writing Simulation for cross-region scenarios.
* [Service Tokens and Service-to-Service Authentication](https://github.com/ryos-io/Rhino/wiki/Service-to-Service-Authentication) - How to enable S2S authentication (OAuth 2.0)
* [Reporting](https://github.com/bagdemir/Rhino/wiki/Reporting) - Reporting the load metrics.
* [Measurements](https://github.com/bagdemir/Rhino/wiki/Measurements) - Record measurement. 

### Integrations
* [Influx DB Integration](https://github.com/bagdemir/Rhino/wiki/Influx-DB-Integration) - Push the metrics into Influx DB. 
* [Grafana Integration](https://github.com/bagdemir/Rhino/wiki/Grafana-Integration) - Show the metrics on Grafana. 
* [Gatling Integration](https://github.com/ryos-io/Rhino/wiki/Generating-Gatling-Reports) - To create Gatling simulation reports.

Questions/Contributions?
---

Feel free to fork the project and make contributions in terms of Pull Requests. For bigger 
proposals, architectural discussions and bug reports, use the Github's issue tracker.
