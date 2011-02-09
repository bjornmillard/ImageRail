#!/bin/sh

here="${0%/*}"
cd "$here"
java -Xmx1000M -d32 -cp ImageRail.jar:lib/jhdf5.jar:lib/SDCube_API.jar -Djava.library.path=lib/native gui.MainGUI
