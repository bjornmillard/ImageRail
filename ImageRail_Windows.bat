@echo off

set IR_DIR=%PROGRAMFILES%\ImageRail
set IR_JARS=%IR_DIR%\jars

set PATH=%PATH%;%IR_JARS%\lib
set CLASSPATH=%CLASSPATH%;%IR_DIR%;%IR_DIR%\ImageRail.jar;%IR_DIR%\HDF5XML.jar;%IR_JARS%\jhdf5.jar;%IR_JARS%\jhdf5obj.jar;%IR_JARS%\jhdfobj.jar

cd /d %IR_DIR%
jre6\bin\java -Xmx1000M gui.MainGUI
pause