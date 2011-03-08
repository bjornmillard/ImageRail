#!/bin/sh

########## PROJECT PATH
projPath="/Users/blm13/Desktop/test2.ir"

#################
#################
#	Enter Parameters here:

########## PLATES to RUN
# Start PlateID
sP=0
# End PlateID
eP=1

########## WELLS to RUN
# Start WellID
sW=0
# End WellID
eW=95


########## SEGMENTATION PARAMETERS
# NuclearThreshold
nucThresh=1000

# CytoThreshold
cytoThreshold=300

# BackgroundThreshold
bkgdThreshold=100

# CoordinatesToStore
coordsToStore=0

#################
#################




###############################
#	RUNNING
#################
# For all Plates... 
#################
p=0
while [ $p -lt $eP ]
do

#################
# For all Wells... 
#################
w=0
while [ $w -lt $eW ]
  do
  	 java -Xmx1000M -cp ./HDF5XML.jar:. run.Segment $projPath $p $w $nucThresh $cytoThreshold $bkgdThreshold $coordsToStore
	 w=`expr $w + 1`
  done

 p=`expr $p + 1`

done





