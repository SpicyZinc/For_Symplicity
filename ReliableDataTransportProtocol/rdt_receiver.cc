/*
 * FILE: rdt_receiver.cc
 * DESCRIPTION: Reliable data transfer receiver.
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


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <iostream>
#include <vector>
#include <list>

#include "rdt_struct.h"
#include "rdt_receiver.h"

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


static void make_pkt(struct packet *p, int ACK_num);
// static int extract_to_message(const struct packet *p, struct message *m);
static void extract_to_message(const struct packet *p, struct message *m);
static unsigned short cal_checksum(struct packet *pkt);
// static unsigned long get_header_sum(struct packet *pkt);
// static unsigned long get_data_sum(struct packet *pkt);
static unsigned long get_header_data_sum(struct packet *pkt);
static bool not_corrupt(struct packet *pkt);

static unsigned int expected_seq_num = 1;

/* receiver initialization, called once at the very beginning */
void Receiver_Init()
{
    fprintf(stdout, "At %.2fs: receiver initializing ...\n", GetSimulationTime());
}

/* receiver finalization, called once at the very end.
   you may find that you don't need it, in which case you can leave it blank.
   in certain cases, you might want to use this opportunity to release some 
   memory you allocated in Receiver_init(). */
void Receiver_Final()
{
    fprintf(stdout, "At %.2fs: receiver finalizing ...\n", GetSimulationTime());
}

/* event handler, called when a packet is passed from the lower layer at the 
   receiver */
void Receiver_FromLowerLayer(struct packet *pkt)
{	// static unsigned short expected_seq_num = 1;
	// unsigned short seq_num = (unsigned short) (((unsigned short)pkt->data[SEQ_NUMBER_START] & 0xFF) << 8) + ((unsigned short)pkt->data[SEQ_NUMBER_START + 1] & 0xFF);
		
	unsigned int seq_num =
			(unsigned int)((((unsigned int)pkt->data[SEQ_NUM_START] & 0xFF) << 24) + (((unsigned int)pkt->data[SEQ_NUM_START + 1] & 0xFF) << 16) + 
						  (((unsigned int)pkt->data[SEQ_NUM_START + 2] & 0xFF) << 8) + (((unsigned int)pkt->data[SEQ_NUM_START + 3] & 0xFF)));
	/// unsigned short rcv_checksum = cal_checksum(pkt);
	/// unsigned short parse_check_sum = ((unsigned short)(((unsigned short)pkt->data[CHECKSUM_START] & 0xFF) << 8) + ((unsigned short)pkt->data[CHECKSUM_START + 1] & 0xFF));

	/// cout << "At Receiver receives Seq_Num == " << seq_num << " CheckSum == " << rcv_checksum << " parse_CheckSum == " << parse_check_sum <<endl;	
	
	// corrupted check
	struct packet sndpkt;
	if(!not_corrupt(pkt)) {
		// send off an ACK to last received packet
		// meaning corrupted packet
		if(expected_seq_num >= 2) {
			make_pkt(&sndpkt, expected_seq_num - 1);
			Receiver_ToLowerLayer(&sndpkt);
		}		
		return;
	}	
	// notcorrupt(rcvpkt) && hasseqnum(rcvpkt, expectedseqnum)
	// send off (expected_seq_num - 1)
	
	// if(not_corrupt(pkt)) {
		// do I have to consider two conditions?
		// seq_num < expected_seq_num: very behind or can this happen?
		// seq_num > expected_seq_num: out of order
		if(seq_num != expected_seq_num) {
			make_pkt(&sndpkt, expected_seq_num - 1);
			Receiver_ToLowerLayer(&sndpkt);
			return;
		}
		else {
			expected_seq_num++;
			// deliver to upper layer
			struct message msg;
			extract_to_message(pkt, &msg);
			// cout << "before to upper layer" << endl;
			Receiver_ToUpperLayer(&msg);
			// cout << "after to upper layer" << endl;		
			if(msg.data != NULL)
				free(msg.data);		
			// send empty packet only with ACK back to sender
			make_pkt(pkt, seq_num);
			Receiver_ToLowerLayer(pkt);
		}
	// }
}

// empty packet with only ACK, others are zero
static void make_pkt(struct packet *p, int ACK_num) {
	// only useful information in packet is ACK, others are set to zero
	// set size
	p->data[PKTSIZE_START] = ((char)0);
	// set sequence
	
	p->data[SEQ_NUM_START] = (char)((0 >> 24) & 0xFF);
	p->data[SEQ_NUM_START + 1] = (char)((0 >> 16) & 0xFF);
	p->data[SEQ_NUM_START + 2] = (char)((0 >> 8) & 0xFF);
	p->data[SEQ_NUM_START + 3] = (char)(0 & 0xFF);	
	
	// set ACK
	p->data[ACK_NUM_START] = (char)((ACK_num >> 24) & 0xFF);
	p->data[ACK_NUM_START + 1] = (char)((ACK_num >> 16) & 0xFF);
	p->data[ACK_NUM_START + 2] = (char)((ACK_num >> 8) & 0xFF);
	p->data[ACK_NUM_START + 3] = (char)(ACK_num & 0xFF);
	
	// set checksum
	unsigned short checksum = cal_checksum(p);
	p->data[CHECKSUM_START] = (char)((checksum >> 8) & 0xFF);
	p->data[CHECKSUM_START + 1] = (char)(checksum & 0xFF);	
	
}

static void extract_to_message(const struct packet *p, struct message *m) {	
	// unsigned int message_length = (unsigned char)p->data[PKTSIZE_START] && 0xFF;
	unsigned int message_length = (unsigned int)p->data[PKTSIZE_START];	
	// m->data = new char[message_length];
	// cout << "message_length == " << message_length << endl;
	/// printf("Received pkt direct-read data size == %d, parsed message_length == %d\n", p->data[PKTSIZE_START], message_length);
	m->size = message_length;
	m->data = new char[MAX_PAYLOAD_SIZE];
	// char * memcpy ( char * destination, const char * source, size_t num );
	// ?????
	// memcpy(m->data, p->data + HEADER_SIZE + 1, message_length);
	memcpy(m->data, p->data + HEADER_SIZE, message_length);
	// string datum;
	// getline(m->data, datum);
	// printf("Receiver Data from packet:%s\n", p->data + HEADER_SIZE + 1);
	/// printf("Receiver Data from packet:%s\n", p->data + HEADER_SIZE);
	/// printf("Receiver Data converts to message:%s\n", m->data);
	// cout << "What in there " << datum << endl; 
	// return m;
}

// helper method
/*
static unsigned short cal_checksum(struct packet *pkt) {
	unsigned long sum = 0;
	sum += get_header_sum(pkt);
	sum += get_data_sum(pkt);

	// add the overflowed bit
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	// it is possible that the previous one overflowed
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);

	unsigned short checksum = sum & 0xFFFF;
	
	return ~checksum;
}
*/
static unsigned short cal_checksum(struct packet *pkt) {
	unsigned long sum = 0;
	sum += get_header_data_sum(pkt);
	// add the overflowed bit
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);
	// it is possible that the previous one overflowed
	sum = ((sum >> 16) & 0xFFFF) + (sum & 0xFFFF);

	unsigned short checksum = sum & 0xFFFF;
	
	return ~checksum;
}
/*
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
*/
static unsigned long get_header_data_sum(struct packet *pkt) {
	unsigned long header_data_sum = 0;
	for(int i = 0; i < 64; i+=2) {
		if(i == 10)
			continue;
		header_data_sum += ((unsigned long)pkt->data[i]) & 0xFF;
	}
	for(int j = 0 + 1; j < 65; j+=2) {
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
	for(int i = HEADER_SIZE; i < size + HEADER_SIZE ; i+=2) {
		data_sum += ((unsigned long)pkt->data[i]) & 0xFF;
	}
	for(int j = HEADER_SIZE + 1; j < size + HEADER_SIZE ; j+=2) {
		data_sum += (((unsigned long)pkt->data[j]) << 8) & 0xFF00;
	}
	return data_sum;
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
