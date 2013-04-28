/**
CSC457 Assignment 03 
Author: Liang Xin
In order to test, codes must run under simulation code rdt_sim.cpp, which is provided by the professor.
*/
===============================================================================
Files submitted:
	1. rdt_sender.cc	
	2. rdt_receiver.cc	
	3. makefile	(default makefile)
	4. run(Bash Scripts)
	5. ReadMe.txt
	6. result.txt
===============================================================================
To compile: (the default makefile is used)
	$ make clean
	$ make
===============================================================================
To run:
	./rdt_sim <sim_time> <mean_msg_arrivalint> <mean_msg_size> <reorder_rate> <loss_rate> <corrupt_rate> <tracing_level>
	./rdt_sim 1000 		 0.1                   100             0                 0           0              0  
    ./rdt_sim 1000       0.1                   100             0.02              0           0              0 
	./rdt_sim 1000 		 0.1                   100             0                 0.02        0              0  
    ./rdt_sim 1000       0.1                   100             0.02              0           0.02           0 
	./rdt_sim 1000 		 0.1                   100             0.02              0.02        0.02           0  
	
	OR
	./run
	
===============================================================================
Analysis and Summary:
GO_BACK_N algorithm is implemented for this assignment.

In this assignment, you will be implementing the sending and receiving side of a reliable data transport (RDT) protocol. 
Your protocol should achieve error-free, loss-free, and in-order data delivery on top of a link medium that can lose, reorder, and corrupt packets. 
Your implementation can follow any variation of the sliding window protocol.
Since we don't have machines with OS that we can modify, your implementation will run in a simulated environment.
 
***Both sender and receiver have to check whether or not the packet is corrupted***
The way to deal with corruption is simple, just ignore the corrupted packet.

=======  SENDER  =======
Initial values: 	
	TIME_OUT_TIME == 0.3
	WINDOW_SIZE == 10
	send_base == 1
	nextseqnum == 1
	
TIME_OUT_TIME depends on the average Round Trip Time but if it is
about 0.1 seconds one way, as recommended by Kai, I set TIME_OUT_TIME to be 0.3.

WINDOW_SIZE, a too big window size may affect the efficiency of Go-Back-N 
because it needs to retransmit the entire window at a timeout. 

For GO_BACK_N, the most important three variables to implement this algorithm at sender are:
1. WINDOW_SIZE
   The size of the window at sender. The performance is heavily dependent on this.
2. send_base
   the index of first sent-yet-not-ACKed packet 
3. nextseqnum
   the next packet will be sent as long as window still has room

The hardest part in GBN is to use one physical timer to simulate that each packet has a timer.

Thought:
1. before sending each packet, record the absolute timeout time of this packet by adding current time to TIME_OUT_TIME
2. every packet has its own absolute timeout time, and put it in the packet object
3. Sender_StopTimer() make a timer not "set" any more
4. only one physical timer, its time is always eclipsing;
   because it is go back N to resend, it should know current time, based on current time to calculate absolute expiration time point again.
   The old expiration time point become a new start time point, analogy to current time plus TIME_OUT_TIME.
   it just find current time point by using last expiration time point at a timeout.
   
   This part is done by update_timeout()
   
5. Sender_StartTimer(double timeout): The relative timeout should be absolute timeout time point minus
   the time point when timer starts, which is current time point.
   The time when timer starts is easily known by calling GetSimulationTime(),
   since you know two absolute values, and some packet's timer must be expired at an absolute timeout time point we calculated before,
   it is natural to set timeout to be difference these two absolute values.
   
In order to calculate conveniently, becuase the number of packets could be equal or greater than 65,536(2^16); 
SEQ_NUM and ACK_NUM are given 4 bytes (32 bits), respectively, to simply binary operation.

The packet size is at most 64 bytes, shown as below

	 size		SEQ_NUM				ACK_NUM			  checksum         data
	|----|------------------|----------------------|-----------|---------------------|
	|    |                  |                      |		   |		  			 |
	|  1 |        4         |         4            |	 2	   |	    53           |
	|----|------------------|----------------------|-----------|---------------------|
    0    1                  5                      9           11                   63  

	
At final stage of implementing this algorithm, the code cannot handle corrupted packet being sent and received.
The reason being I did not check if corrupted at sender side, which will cause to parse a corrupted packet
to get a wrong ACK, and thus send_base is wrongfully changed to a not expected one accordingly.
	
=======  RECEIVER  =======
For GO_BACK_N, the most important one variable to implement this algorithm at receiver is:
expected_seq_num == 1

Thought:
0. check if packet is corrupted or not first
   if corrupted, ignore it.
1. extract(rcvpkt,data)
2. deliver_data(data)
3. sndpkt = make_pkt(expectedseqnum,ACK,checksum)
4. udt_send(sndpkt)
5. expectedseqnum++
	
===============================================================================
Results and Performance:

Results:
=====
please see result.txt
=====

Performance:

./rdt_sim 1000 0.1 100 0.1 0 0 0 (10% out_of_order rate)
This implementation is performing normally against out of order packets.

./rdt_sim 1000 0.1 100 0 0.2 0 0 (20% lost packets)
./rdt_sim 100 0.1 100 0 0.28 0 0 (28% lost packets)
works pretty good for high loss_rate

rdt_sim 1000 0.1 100 0 0 0.1 0 (10% corrupt packets)
can handle corruption well currently. Very occasionally error occurred.

So far,
1. when outoforder_rate is much bigger than 0.02, errors might occur. So far, 0.1 is fine.
2. handling packet loss is good, when loss_rate reaches almost 0.3, result is still correct.
3. can handle corruption well currently
