/*
 * FILE: rdt_sender.cc
 * DESCRIPTION: Reliable data transfer sender.
 * VERSION: 0.2
 * AUTHOR: Kai Shen (kshen@cs.rochester.edu)
 * NOTE: This implementation assumes there is no packet loss, corruption, or 
 *       reordering.  You will need to enhance it to deal with all these 
 *       situations.  In this implementation, the packet format is laid out as 
 *       the following:
 *       
 *       |<-  1 byte  ->|<-             the rest            ->|
 *       | payload size |<-             payload             ->|
 *
 *       The first byte of each packet indicates the size of the payload
 *       (excluding this single-byte header)
 */

/**
Time Simulation is a big problem???
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vector>
#include <list>
#include <iostream>

#include "rdt_struct.h"
#include "rdt_sender.h"

using namespace std;

#define WINDOW_SIZE 10
#define TIME_OUT_TIME 0.3
#define PKTSIZE_START 0
#define SEQ_NUM_START 1
// #define ACK_NUM_START 3
#define ACK_NUM_START 5
// #define CHECKSUM_START 5
#define CHECKSUM_START 9
// #define HEADER_SIZE 7
#define HEADER_SIZE 11
// #define MAX_PAYLOAD_SIZE 57
#define MAX_PAYLOAD_SIZE 53

class Fragment;
static int send_next();
static void update_timeout();
// static void go_back_N_send();
static void mark_ACKed(unsigned int rcv_seq_num);
// static void remove();
// static void make_pkt(struct packet* pkt, const Fragment* n);
static void make_pkt(struct packet* pkt, Fragment* n);
static unsigned short cal_checksum(struct packet *pkt);
// static unsigned long get_header_sum(struct packet *pkt);
// static unsigned long get_data_sum(struct packet *pkt);
static unsigned long get_header_data_sum(struct packet *pkt);
static bool not_corrupt(struct packet *pkt);

// how many packets are sent 
static int sent = 0;
// vector of ACKs so far
std::vector<unsigned short> ACK_vector;

// sequence number starts from 1
// static unsigned short seq_number = (unsigned short)1;
static unsigned int seq_number = (unsigned int)1;

// three parameters for GO_BACK_N, WINDOW_SIZE is already there
static unsigned int send_base = 1;
static unsigned int next_to_send = 1; // 32 bit

class Fragment {
public:
	char *message;
	int size;
	// unsigned short seqNum;
	// unsigned short ackNum;
	unsigned int seqNum;
	unsigned int ackNum;
	unsigned short checkSum;
	double time;
	bool isSent;
	bool isACKed;
// constructor
	Fragment(int size = 0) {
		message = new char[size];
		this->size = size;
		isSent = false;
		isACKed = false;
	}
// set checksum
	void setChecksum(unsigned short cs) {
		checkSum = cs;
	}
	
// set timeout time absolute 
	void setTime(double t) {
		time = t;
	}
// destructor
	~Fragment() {
		delete message;
	}
	// copy assignment operator
	// declaration and definition
	Fragment & operator=(const Fragment &n) {
		delete this->message;
		this->message = new char[n.size];
		memcpy(this->message, n.message, n.size);
		this->size = n.size;
		this->seqNum = n.seqNum;
		this->ackNum = n.ackNum;
		this->checkSum = n.checkSum;
		this->time = n.time;
		this->isSent = n.isSent;
		this->isACKed = n.isACKed;

		return (*this);
	}
};

// divide message into packets, before packet, only data part are stored in data structure of LinkedList of Fragment
template <class Fragment>
class FragmentList : public list<Fragment> {
public:
	// class list default constructor
	FragmentList() : list<Fragment>::list() {}
	// method to make a list of segments
	void make_list(char *message, int msg_length) {
		// the cursor always points to the first unsent byte in the message
		int cursor = 0;
		while(msg_length > 0) {
			if(msg_length >= MAX_PAYLOAD_SIZE) {
				// initialization of this object
				Fragment *n = new Fragment(MAX_PAYLOAD_SIZE);
				memcpy(n->message, message+cursor, MAX_PAYLOAD_SIZE);
				n->seqNum = seq_number;
				this->push_back(*n);
			
				seq_number++;
				cursor += MAX_PAYLOAD_SIZE;
				msg_length -= MAX_PAYLOAD_SIZE;
			}
			else {
				// initialization of this object
				Fragment *n = new Fragment(msg_length);
				memcpy(n->message, message+cursor, msg_length);
				n->seqNum = seq_number;
				this->push_back(*n);
			
				seq_number++;
				cursor += msg_length;
				msg_length = 0;
			}
		}	
	}
};

// Important: Notice how if we declare a new object and we want to use its default constructor (the one without parameters),
// we do not include parentheses():
FragmentList<Fragment> fragmentList;

/* sender initialization, called once at the very beginning */
void Sender_Init()
{
    fprintf(stdout, "At %.2fs: sender initializing ...\n", GetSimulationTime());
}

/* sender finalization, called once at the very end.
   you may find that you don't need it, in which case you can leave it blank.
   in certain cases, you might want to take this opportunity to release some 
   memory you allocated in Sender_init(). */
void Sender_Final()
{
    fprintf(stdout, "At %.2fs: sender finalizing ...\n", GetSimulationTime());
}

/* event handler, called when a message is passed from the upper layer at the 
   sender */
void Sender_FromUpperLayer(struct message *msg)
{	// make segment list which is from message, only 57 bytes payload
	fragmentList.make_list(msg->data, msg->size);
	send_next();
}

/* event handler, called when a packet is passed from the lower layer at the 
   sender */
// 1. when called from upper layer, need to send
// 2. when timeout
// 3. when rcvACK >= send_base
void Sender_FromLowerLayer(struct packet *pkt)
{	
	if(!not_corrupt(pkt)) {
		return;
	}
	unsigned int rcvACK = (unsigned int)
				((((unsigned int)pkt->data[ACK_NUM_START] & 0xFF) << 24) + (((unsigned int)pkt->data[ACK_NUM_START + 1] & 0xFF) << 16) + 
				(((unsigned int)pkt->data[ACK_NUM_START + 2] & 0xFF) << 8) + (((unsigned int)pkt->data[ACK_NUM_START + 3] & 0xFF)));
	// cout << "At sender receives ACK == " << rcvACK << endl;	
	// ideal state
	// cumulative ACK
	// ???
	if(rcvACK >= (unsigned int)(send_base)) {
		// cout << "Has it ever entered into." << endl;
		mark_ACKed(rcvACK);		
		// send_base = rcvACK; ???
		// window slides one step
		send_base = rcvACK + 1;
		
		if(send_base == next_to_send) {
			// next_to_send has to be updated???
			// next_to_send = send_base;
			Sender_StopTimer();			
		}	
		else {
			if(Sender_isTimerSet()) {
				Sender_StopTimer();
				// start another timer which is done in send_next()
			}
			send_next();
			// Sender_StartTimer(send_base.time - t3);			
			/*
			send_next();
			Fragment &n = fragmentList.front();
			double x = n.time + TIME_OUT_TIME - (GetSimulationTime() - n.time);
			Sender_StartTimer(x);
			*/
		}		
	}	
}

/* event handler, called when the timer expires */
// go back N
void Sender_Timeout()
{
	Sender_StopTimer();		
	next_to_send = send_base;
	// update packet absolute timeout time from next_to_send to window end
	// Î´·¢ ºÎÀ´timeout?
	update_timeout();
	send_next();
}
// loop through the list, use conditions isSent and isACKed to find the next one to be sent
// automatically find the unsent packets 
// guarantee less than window size
// set timer
// make_pkt

// rewrite send_next()
static int send_next() {
	struct packet p_array[WINDOW_SIZE];	
	unsigned int index = 1;
	int count = 0;
	FragmentList<Fragment>::iterator itr;
	for(itr=fragmentList.begin(); itr!=fragmentList.end(); itr++) {
		// "!itr->isSent" is added, key point
		if(index >= next_to_send && index < (send_base + WINDOW_SIZE) && !itr->isSent && !itr->isACKed) {
			itr->isSent = true;
			itr->isACKed = false;
			// record current time
			double current_t1 = GetSimulationTime();
			itr->time = current_t1 + TIME_OUT_TIME;
			// itr->time = GetSimulationTime();
			make_pkt(p_array + count, &(*itr));
			if(!Sender_isTimerSet()) {
				// Sender_StartTimer(TIME_OUT_TIME);
				// send_base timeout - current_time
				double current_t2 = GetSimulationTime();
				Sender_StartTimer(itr->time - current_t2);
			}
			Sender_ToLowerLayer(p_array + count);
			sent++;
			count++;	
			/// cout << "At sender sends packet's CheckSum == " << itr->checkSum << " Seq_Num == " << itr->seqNum << endl;			
		}
		index++;
	}
	// cout << "So far, " << sent << " packets are sent." << endl;
	return 1;
}
// update timeout time for each packet
static void update_timeout() {
	unsigned int i = 1;
	int count = 0;
	FragmentList<Fragment>::iterator itr;
	for(itr=fragmentList.begin(); itr!=fragmentList.end(); itr++) {
		if(i >= next_to_send && i < (send_base + WINDOW_SIZE) && itr->isSent && !itr->isACKed) {
			itr->isSent = false;
			itr->time += itr->time + TIME_OUT_TIME;
			count++;	
		}
		i++;
	}
}
/*
static void go_back_N_send() {
	FragmentList<Fragment>::iterator itr;
	struct packet pkt_array[WINDOW_SIZE];
	int count = 0;	
	// stop timer for now
	Sender_StopTimer();
	for(itr=fragmentList.begin(); itr!=fragmentList.end(); itr++) {
		// skip the ones that are ACKed
		if(!itr->isACKed && itr->isSent && count<WINDOW_SIZE) {
			itr->time = GetSimulationTime();
			make_pkt(pkt_array + count, &(*itr));
			if(!Sender_isTimerSet()) {
				Sender_StartTimer(TIME_OUT_TIME);
			}
			Sender_ToLowerLayer(pkt_array + count);
			count++;
		}
	}
}
*/
// mark isACKed via rcv_seq_num
static void mark_ACKed(unsigned int rcv_seq_num) {
	FragmentList<Fragment>::iterator itr;
	for(itr=fragmentList.begin(); itr!=fragmentList.end(); itr++) {		
		if(itr->seqNum == rcv_seq_num) {
			itr->isSent = true;
			itr->isACKed = true;
		}
	}
}
/*
// remove isSent and isACKed packet
static void remove() {
	FragmentList<Fragment>::iterator itr;
	for(itr=fragmentList.begin(); itr!=fragmentList.end();) {		
		if(itr->isSent && itr->isACKed) {
			// fragmentList.remove(*itr);
			itr = fragmentList.erase(itr);
		}
		else {
			itr++;
		}
	}
}
*/
// make_pkt from fragmenet, wrap up to 64 byte
/* fill in the packet */
static void make_pkt(struct packet* pkt, Fragment* n) {
	// fill size to this packet
	// (char)
	pkt->data[PKTSIZE_START] = (n->size);
	/// printf("%d,%d\n", pkt->data[PKTSIZE_START], n->size);
	// cout << "Packet data size in data[0] " << pkt->data[PKTSIZE_START] << endl;
	// cout << "Packet wrap n->size == " << n->size << endl;
	// fill sequence number to this packet	
	// unsigned short seq_num = n->seqNum;
	unsigned int seq_num = n->seqNum;
	// pkt->data[SEQ_NUM_START] = (char)(seq_num >> 8) & 0xFF;
	// pkt->data[SEQ_NUM_START + 1] = (char)(seq_num & 0xFF);
	
	pkt->data[SEQ_NUM_START] = (char)((seq_num >> 24) & 0xFF);
	pkt->data[SEQ_NUM_START + 1] = (char)((seq_num >> 16) & 0xFF);
	pkt->data[SEQ_NUM_START + 2] = (char)((seq_num >> 8) & 0xFF);
	pkt->data[SEQ_NUM_START + 3] = (char)(seq_num & 0xFF);
	
	// fill acknowledge number to this packet  
	// pkt->data[ACK_NUM_START] = (char)((0 >> 8) & 0xFF);
	// pkt->data[ACK_NUM_START + 1] = (char)(0 & 0xFF);
	
	pkt->data[ACK_NUM_START] = (char)((0 >> 24) & 0xFF);
	pkt->data[ACK_NUM_START + 1] = (char)((0 >> 16) & 0xFF);
	pkt->data[ACK_NUM_START + 2] = (char)((0 >> 8) & 0xFF);
	pkt->data[ACK_NUM_START + 3] = (char)(0 & 0xFF);

	// copy 53-byte data to pkt
	// ??? +1
	/*
	memcpy(pkt->data + HEADER_SIZE + 1, n->message, n->size);
	printf("Sender Data from message:%s\n", pkt->data + HEADER_SIZE + 1);
	printf("Sender Data after turing into packet:%s\n", pkt->data + HEADER_SIZE + 1);
	*/	
	memcpy(pkt->data + HEADER_SIZE, n->message, n->size);
	/// printf("Sender Data from message:%s\n", n->message);
	/// printf("Sender Data after turing into packet:%s\n", pkt->data + HEADER_SIZE);
	
	// based on what is in pkt so far to calculate checksum
	// fill checksum to this packet
	unsigned short checksum = cal_checksum(pkt);
	n->checkSum = checksum;
	// n->setChecksum(checksum);
	pkt->data[CHECKSUM_START] = (char)((checksum >> 8) & 0xFF);
	pkt->data[CHECKSUM_START + 1] = (char)(checksum & 0xFF);
}
// helper method
static unsigned short cal_checksum(struct packet *pkt) {
	unsigned long sum = 0;
	// sum += get_header_sum(pkt);
	// sum += get_data_sum(pkt);
	sum += get_header_data_sum(pkt);
	
	// add the overflowed bit
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	// it is possible that the previous one overflowed
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	
	unsigned short checksum = sum & 0xFFFF;
	
	return ~checksum;
}

static unsigned long get_header_data_sum(struct packet *pkt) {
	unsigned long header_data_sum = 0;
	for(int i=0; i<64; i+=2) {
		if(i == 10)
			continue;
		header_data_sum += ((unsigned long)pkt->data[i]) & 0xFF;
	}
	for(int j=0+1; j<65; j+=2) {
		if(j == 9)
			continue;
		header_data_sum += (((unsigned long)pkt->data[j]) << 8) & 0xFF00;
	}
	return header_data_sum;
}
/*
static unsigned long get_header_sum(struct packet *pkt) {
	unsigned long header_sum = 0;
	header_sum += ((int)pkt->data[PKTSIZE_START] << 8) & 0xFF00;
	
	header_sum += (int)pkt->data[SEQ_NUM_START] & 0xFF;
	header_sum += ((int)pkt->data[SEQ_NUM_START + 1] << 8) & 0xFF00;
	header_sum += (int)pkt->data[ACK_NUM_START] & 0xFF;
	header_sum += ((int)pkt->data[ACK_NUM_START + 1] << 8) & 0xFF00;
	
	return header_sum;
}

static unsigned long get_data_sum(struct packet *pkt) {
	unsigned long data_sum = 0;
	int size = (unsigned char)pkt->data[PKTSIZE_START] & 0xFF;

	// HEADER SIZE is 7 so start from adding with mask 0xFF rather than 0xFF00
	for(int i = HEADER_SIZE; i < size + HEADER_SIZE; i+=2) {
		data_sum += ((unsigned long)pkt->data[i]) & 0xFF;
	}
	for(int j = HEADER_SIZE + 1; j < size + HEADER_SIZE; j+=2) {
		data_sum += (((unsigned long)pkt->data[j]) << 8) & 0xFF00;
	}
	return data_sum;
}


static bool not_corrupt(struct packet *pkt) {
	int size = (unsigned char)pkt->data[PKTSIZE_START] & 0xFF;
	if(size > MAX_PAYLOAD_SIZE) 
		return false;
	// add the entire thing
	unsigned long sum = 0;
	sum += get_header_data_sum(pkt);
	unsigned short check_sum = (unsigned short)(((unsigned short)pkt->data[CHECKSUM_START] & 0xFF) << 8) + ((unsigned short)pkt->data[CHECKSUM_START + 1] & 0xFF);
	sum += check_sum;
	// add the overflowed bit
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	// it is possible that the previous one also overflowed
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);

	if(sum == 0xFFFF) {
		return true;
	}
	else {
		return false;
	}
}
*/
static bool not_corrupt(struct packet *pkt) {
	int size = (unsigned char)pkt->data[PKTSIZE_START] & 0xFF;
	if(size > MAX_PAYLOAD_SIZE) 
		return false;
	/*	
	unsigned long sum = 0;
	// recalculate except checksum again by receiver self
	sum += get_header_data_sum(pkt);
	// sum += cal_checksum(pkt);
	// parse packet checksum
	unsigned short check_sum = (unsigned short)((((unsigned short)pkt->data[CHECKSUM_START] & 0xFF) << 8) + ((unsigned short)pkt->data[CHECKSUM_START + 1] & 0xFF));
	sum += check_sum;
	// add the overflowed bit
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	// it is possible that the previous one also overflowed
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);

	// compare them
	if(sum == 0xFFFF) {
		return true;
	}
	else {
		return false;
	}
	*/
	unsigned short sum = 0;
	// recalculate checksum again by receiver self
	sum += cal_checksum(pkt);
	// sum +=(unsigned short) get_header_data_sum(pkt);
	// sum = (((checksum >> 8) & 0xFF) << 8) + (checksum & 0xFF);	
	// sum = sum & 0xFFFF;
	// parse packet checksum
	unsigned short check_sum = ((unsigned short)(((unsigned short)pkt->data[CHECKSUM_START] & 0xFF) << 8) + ((unsigned short)pkt->data[CHECKSUM_START + 1] & 0xFF));

	// compare them
	if(sum == check_sum) {
		return true;
	}
	else {
		return false;
	}		
}



