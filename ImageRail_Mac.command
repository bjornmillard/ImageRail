#!/bin/sh

here="${0%/*}"
cd "$here"
java -Xmx1000M -cp ./HDF5XML.jar:. gui.MainGUI
