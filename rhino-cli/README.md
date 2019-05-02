Rhino
---

`rhino` is a command line tool to deploy and run your load tests on the load-testing container platform.

How to install
---

Clone the project into your workspace and then run the rhino-install script:
```
$ cd ./rhino-cli && chmod u+x rhino-install && ./rhino-install
```

This will build a docker container including all tools baked in it.
All rhino commands will be forwarded to the docker container.

After the build a `~/.rhino` directory will be created containing the `rhino` run script.
By default `~/.rhino/bin` will be added to your `.bashrc` or `.zshrc`.
Don't forget to source your shell rc:
```
$ source ~/.bashrc
```

How to use
---

`rhino` needs access to SC Artifactoy repository to be able to release the load tests.


Hit help to know more about `rhino`

```
$ rhino help
```

The only configuration `rhino` needs is the load-test context as environment variable, `export RHINO_CONTEXT=some_context_id`.
The context id is needed to keep the track of your load tests and persist the metadata at the backend e.g.

```
$ export RHINO_CONTEXT="storage_SyntheticLoadGenerator"
```

If not set, the context defaults to `storage_SyntheticLoadGenerator,`
which is all right for storage service load testing purpose.

You need to acquire the AWS credentials using [KLAM](https://klam-sj.corp.adobe.com/)
The load test cluster is located in `Sharedcloud` project and `development`
environment on KLAM. Request `power_user` privileges for 8 hours and click on "Get CLI"
to obtain the credentials for "in Bash".

Once you set up your system, you can use the `rhino` on your console:
```
$ rhino list
```

Keep in mind that some Rhino tasks like `run` require SSH access on the backend
load testing cluster e.g to download simulation results, cleaning-up, etc. Thus,
make sure that you have already used [ssh-out](http://hamburg-ssh-out.eur.adobe.com/).
Moreover, to access the cluster through SSH you need the private key
[sc-dev-eu.pem](https://pim.corp.adobe.com/SecretView.aspx?secretid=1080)
in your ~/.ssh/ directory.

Custom configuration
---

Rhino allows you to define service specific tasks to be executed.
For example, if you add `--service aclservice` to the `run` command,
the `aclservice.tmpl` will be read which is essentially a AWS task definition.
The template file can contain shell environment variables for the sake of parametrization.
Since docker doesn't allow to pass environment variables on the fly,
you need to write the variables down in a environment file.
By default `~/.rhino/env` is used but you can let Rhino use another file by defining
`ENV_FILE` before the execution, i.e.:
```
ENV_FILE=~/aclservice.env rhino run -c 2 -s aclservice.SharingPhotos -p stage-ew1 --service aclservice
```

Adding your own task
---

To add your own service specific task to the script, you have to

* Define a name, e.g. `aclservice`
* Add a template file named `aclservice.tmpl` in the `rhino-cli` directory
* Add a method `create_aclservice_definition()` to `create-definition.sh` (the name need to match the service name)
* Add the task name to the valid values to `create_definition.sh#KNOWN_SERVICES`

Questions?
---

Please contact [bagdemir@adobe.com](mailto:bagdemir@adobe)
or [caylak@adobe.com](mailto:caylak@adobe) for any questions.
