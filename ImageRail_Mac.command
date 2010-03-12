#!/bin/sh

here="${0%/*}"
cd "$here"
java -Xmx1000M main/MainGUI
