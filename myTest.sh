#!/usr/bin/env bash

javac ./ticketingsystem/Test.java
java ticketingsystem.Test $@
rm ./ticketingsystem/*.class
