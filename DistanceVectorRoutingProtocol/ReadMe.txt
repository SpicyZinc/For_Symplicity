/**

CSC 457 Assignment02
Liang Xin

*/
==========

Files to submit:

1. DVRouting.java
2. DVServer.java
3. DVClient.java
4. DistanceVector.java
5. RoutingTable.java
6. ReadMe
==========

To compile:
	javac DVRouting.java

To run:
	java DVRouting <port_num> <file_name>
	
Run the compiled code on different six hosts, 
the program at each host will repeatedly send the routing table information to its neighbors 
and a slow-starting neighbor will eventually get the information. 	

==========

Design and Implementation Strategies

At each host, the program is being both a server and a client by running with two different threads.

In one thread, server is always listening on new updates, and once get something new it will update distance vector (the hashtable)
by updating cost or add more destination hosts to the table.
In the other thread, client sends out routing table. For the first time, client reads the local host information which is "# of neighbors and cost to each neighbors
from current host"; for the rest of time, client thread analyzes the distance vector on this host and convert to routing table
to send out.

Class DistanceVector: information host-node as a client has
Class RoutingTable: format of information host-node as a server receives

A data structure of hashtable is maintained to keep record of each host's distance vectors.
There is a vector for each destination on this host node, figure as below shows 
how data structure looks like.

			Hashtable
			   ||
			   ||
			   ||
               \/
		|			 |     		  |	
		|			 |			  |
Source	| Neighbor 1 | Neighbor 2 | Neighbor 3
--------| ------------------------------------
Dest1	| <---------- Vector
--------| ------------------------------------
Dest2	| <---------- Vector
--------|-------------------------------------
Dest2	| <---------- Vector
--------|-------------------------------------
		|
		|
<Key : Value> in the hashtap is <Source-Destination : Vector<DistanceVector>>, in which "Source-Destination" is type of String.

The hashtable has to change and grow dynamically because either some cost between two hosts can be replaced with a smaller value 
or more destination host nodes are being added.

1. Find a new destination, add a new entry in the hashtable
2. Find a smaller cost, replace old cost with the new cost

==========
Test

1. log in to six of those machines from e01.cs.rochester.edu to e06.cs.rochester.edu
2. copy "a.dat", "b.dat", ... to six machines, respectively
3. change six files to names as host names
		a.dat ---> e01.cs.rochester.edu.dat
		b.dat ---> e02.cs.rochester.edu.dat
		c.dat ---> e03.cs.rochester.edu.dat
		d.dat ---> e04.cs.rochester.edu.dat
		e.dat ---> e05.cs.rochester.edu.dat
		f.dat ---> e06.cs.rochester.edu.dat
4. in each of the six machines, run java DVRouting <port_num> <file_name>
   port_num is the same, file_name is the file named after current host name.
   
==========
Results 

The while loop sequence times to get a stable and final result depends on the time gap to run the six programs on six machines.
The bigger time gap to run, the bigger sequence number will be to get the final result.
 
Results are presented in the form of "shortest path: <startHost> to <destHost> via <nextHop> <cost>"
startHost destHost, and nextHop are displayed in a short form with getting rid of ".cs.rochester.edu"

Take node e01.cs.rochester.edu as an example, I listed three types of change in the routing table.
In fact, it only takes 4 loops to get the shortest path for node e01.cs.rochester.edu

"Original local file"
=====
At Host e01.cs.rochester.edu, ## sequence number 1

Sent to 3 neighbors:
shortest path: e01 to e03 via e03 5.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e02 via e02 2.0

"add new destinations"
=====
At Host e01.cs.rochester.edu, ## sequence number 3

Sent to 3 neighbors:
shortest path: e01 to e03 via e04 4.0
shortest path: e01 to e05 via e04 2.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e06 via e02 10.0
shortest path: e01 to e02 via e02 2.0

"update cost"
=====
At Host e01.cs.rochester.edu, ## sequence number 4

Sent to 3 neighbors:
shortest path: e01 to e03 via e04 3.0
shortest path: e01 to e05 via e04 2.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e06 via e04 4.0
shortest path: e01 to e02 via e02 2.0
=====

Change the cost (e01 to e02) to 4.0 from 2.0 in both e01.cs.rochester.edu.dat and e02.cs.rochester.edu.dat, the results at both e01 and e02 are as below
For this example, I changed cost at ## sequence number 10, and the new routing table is generated at ## sequence number 12.

"Changes at host e01" 
=====

At Host e01.cs.rochester.edu, ## sequence number 10

Sent to 3 neighbors:
shortest path: e01 to e03 via e04 3.0
shortest path: e01 to e05 via e04 2.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e06 via e04 4.0
shortest path: e01 to e02 via e02 2.0
			   ||
			   ||
               \/
At Host e01.cs.rochester.edu, ## sequence number 11

Sent to 3 neighbors:
shortest path: e01 to e03 via e03 5.0
shortest path: e01 to e05 via e04 2.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e06 via e04 4.0
shortest path: e01 to e02 via e02 4.0
			   ||
			   ||
               \/
At Host e01.cs.rochester.edu, ## sequence number 12

Sent to 3 neighbors:
shortest path: e01 to e03 via e04 3.0
shortest path: e01 to e05 via e04 2.0
shortest path: e01 to e04 via e04 1.0
shortest path: e01 to e06 via e04 4.0
shortest path: e01 to e02 via e04 3.0

=====

"Changes at host e02"
=====
At Host e02.cs.rochester.edu, ## sequence number 10

Sent to 3 neighbors:
shortest path: e02 to e01 via e01 2.0
shortest path: e02 to e04 via e04 2.0
shortest path: e02 to e03 via e03 3.0
shortest path: e02 to e06 via e04 5.0
shortest path: e02 to e05 via e04 3.0
			   ||
			   ||
               \/
At Host e02.cs.rochester.edu, ## sequence number 11

Sent to 3 neighbors:
shortest path: e02 to e01 via e01 4.0
shortest path: e02 to e04 via e04 2.0
shortest path: e02 to e03 via e03 3.0
shortest path: e02 to e06 via e04 5.0
shortest path: e02 to e05 via e04 3.0
			   ||
			   ||
               \/
At Host e02.cs.rochester.edu, ## sequence number 12

Sent to 3 neighbors:
shortest path: e02 to e01 via e04 3.0
shortest path: e02 to e04 via e04 2.0
shortest path: e02 to e03 via e03 3.0
shortest path: e02 to e06 via e04 5.0
shortest path: e02 to e05 via e04 3.0



==========
The other five nodes final results
=====
At Host e02.cs.rochester.edu, ## sequence number 23

Sent to 3 neighbors:
shortest path: e02 to e01 via e01 2.0
shortest path: e02 to e04 via e04 2.0
shortest path: e02 to e03 via e03 3.0
shortest path: e02 to e06 via e04 5.0
shortest path: e02 to e05 via e04 3.0
=====
At Host e03.cs.rochester.edu, ## sequence number 22

Sent to 5 neighbors:
shortest path: e03 to e01 via e05 3.0
shortest path: e03 to e02 via e02 3.0
shortest path: e03 to e05 via e05 1.0
shortest path: e03 to e04 via e05 2.0
shortest path: e03 to e06 via e05 3.0
=====
At Host e04.cs.rochester.edu, ## sequence number 22

Sent to 4 neighbors:
shortest path: e04 to e03 via e05 2.0
shortest path: e04 to e06 via e05 3.0
shortest path: e04 to e05 via e05 1.0
shortest path: e04 to e02 via e02 2.0
shortest path: e04 to e01 via e01 1.0
=====
At Host e05.cs.rochester.edu, ## sequence number 22

Sent to 3 neighbors:
shortest path: e05 to e01 via e04 2.0
shortest path: e05 to e02 via e04 3.0
shortest path: e05 to e03 via e03 1.0
shortest path: e05 to e04 via e04 1.0
shortest path: e05 to e06 via e06 2.0
=====
At Host e06.cs.rochester.edu, ## sequence number 22

Sent to 2 neighbors:
shortest path: e06 to e02 via e05 5.0
shortest path: e06 to e01 via e05 4.0
shortest path: e06 to e04 via e05 3.0
shortest path: e06 to e03 via e05 3.0
shortest path: e06 to e05 via e05 2.0


==========

==========

Reference

http://docs.oracle.com/javase/tutorial/networking/datagrams/clientServer.html
http://stackoverflow.com/questions/5433378/run-application-both-as-server-and-client
http://stackoverflow.com/questions/12393231/break-statement-inside-two-while-loops
http://www.java2s.com/Code/Java/JDK-6/ProducerandconsumerbasedonBlockingQueue.htm

=====
Lesson:

Want to array to make use of index,
 
Think of Vector first
ArrayList
LinkedList
HashMap
Hashtable

they support iterator or enhanced for loop to go through elements of those data structures
this sequence should be thought of 
