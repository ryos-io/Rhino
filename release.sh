#!/bin/bash

export GPG_TTY=$(tty)
mvn -e release:prepare release:perform

