Rhino Java Framework CHANGELOG
===

## 1.1.10
- Changelog update for 1.1.9 added.

## 1.1.9
- Reflective method call exception handling.
- Server endpoint configuration per environment.
- InfluxDB Java library dependency.
- Removed Jersey Client usage in InfluxDB calls.
- Duration clue at start-up.
- Javadocs improved.

## 1.1.8

* Javadocs added. 
* Small formatting fixes.
* Code hygiene.
* Archetype code templates are fixed.

## 1.1.7

* Archetype creation bug fix: BenchmarkEntity to RhinoEntity rename.

## 1.1.6
## 1.1.5

* Influx DB support.
* Simulation log accessibility check.
* NPE while shutting down the actor system, is fixed.

## 1.1.4

* Simulation duration calc. fix for minute.

## 1.1.3

* Simulation duration control.
* Configuration assignment per environment.
* CLI support.
* Branding and PING? PONG! heartbeat.
* Graceful shutdown the tests.

## 1.1.2

* Fix SDK version in archetype-parent.

## 1.1.1

* skip=false for maven deploy plugin in archetype-parent.

## 1.1.0

* UserSession support and @SessionFeeder annotation.
* Global prepare/cleanUp method and annotation support @Prepare and @CleanUp.
* archetype-parent contains Docker release configurations.

## 1.0.7

All releases up to 1.0.7 was to setup the build pipeline. The initial release of the project is not
1.0.0 rather 1.0.7. This feature set had been presented on Open Dev Days 2018.

* Initial release including the minimal feature set.
* Runnable simulations with scenarios.
* Custom feeders.
* Native UserFeeder and OAuth authorization server support.
* After/Before set-up and cleanUp methods.
* Classpath CSV user source.
