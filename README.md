jPCBSim
=======

What is it?
Front-end and post processing software for simulating planar RF filters and antennas with the openEMS FDTD field solver.

Installation:
Please see the wiki page for installation / configuration information.


To Run:

In project folder

From command line
java -jar dist/jPCBSim.jar

In Windows, you can just change to the dist folder and click on jPCBSim.jar  (assuming you have java installed)


To Build:

First Time:  Open project folder in netbeans.  Open sources/PCBSimClient.  Press Build button.  This will create private folder under nbproject.

After that...
In project folder

type 'ant jar'  to build.


Importing a new design:

1) Use hyperlynx 6.3 or greater to export your eagle cad brd file to the .hyp file format.

2) Choose the menu option 'file/import/hyperlynx using hyp2mat' to import the file using the external converter 'hyp2mat'.



