#!/bin/bash
export STOP_AFTER=1
export GPG_TTY=$(tty)
mvn -e release:prepare release:perform

