jPCBSim
=======

GUI front-end and post processing software for simulating Eagle Cad pcb-based RF filters and antennas with the openEMS FDTD field solver.


To Run:

In project folder

java -jar dist/jPCBSim.jar


To Build:

First Time:  Open project folder in netbeans.  Open sources/PCBSimClient.  Press Build button.  This will create private folder under nbproject.

After that...
In project folder

type 'ant jar'  to build.



Initial Configuration:

1) Edit paths using the menu  'edit/preferences'

2) Choose the paths to the openEMS binaries

3) Choose the path for your projects.  

4) Close application

5) Copy the examples/default folder to the project folder you specified.  

6) restart software.  The default example will be opened on startup.


Importing a new design:

1) Use hyperlynx 6.3 or greater to export your eagle cad brd file

2) Choose the menu option 'file/import/hyperlynx using hyp2mat'


Sorry for the poor documentation.  Will update later as I get a chance.
