@echo off

set IR_DIR=%~dp0
set LIB_DIR=%IR_DIR%\lib

set CLASSPATH=%CLASSPATH%;%IR_DIR%\ImageRail.jar;%LIB_DIR%\SDCube_API.jar;%LIB_DIR%\jhdf5.jar

cd /d %IR_DIR%
jre\bin\java -Xmx1000M -Djava.library.path="%LIB_DIR%\native" gui.MainGUI
pause