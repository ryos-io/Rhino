Archetype
==

Maven archetype project allows developers to create new rhino load testing projects. Currently under development. 
If you want to use the archetype to create a new Rhino load testing project, you need to install the snapshot version in your local Maven repository, e.g 

```bash
mvn -e clean install
```

after having successfully installed the archetype, you can now create a new Rhino project:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.ryos.rhino \
  -DarchetypeArtifactId=rhino-archetype \
  -DarchetypeVersion=1.1.4 \
  -DgroupId=my-group-id \
  -DartifactId=my-artifact-id
```

After creating the load testing project, you now can open it up in your IDE and start off developing your load tests.

Need Help?
---
Please refer to the Wiki page, ask questions by using Github issues, or contact 

erhan@ryos.io for further questions.
