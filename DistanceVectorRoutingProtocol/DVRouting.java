import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DVRouting {
	
    public static void main(String[] args) throws IOException {
	
		Hashtable<String, Double> neighbor_pair = new Hashtable<String, Double>();		
		// Hashtable<String, Integer> neighbors = new Hashtable<String, Integer>();
		ArrayList<String> neighbors = new ArrayList<String>();
		// LinkedList<DistanceVector[]> currentHost = new LinkedList<DistanceVector[]>();
		Hashtable<String, Vector<DistanceVector>> currentHost = new Hashtable<String, Vector<DistanceVector>>();
		// ConcurrentLinkedQueue<DistanceVector[]> currentHost = new ConcurrentLinkedQueue<DistanceVector[]>();
		
		int port = 11111;
		String file = "";
		try {
			if(args.length != 2) {
				System.err.println("Usage: java DVRouting <port_number> <file_name>");
				System.exit(-1);
			}
			else {
				port = Integer.parseInt(args[0]);
				if(port <= 1023) {
					System.err.println("Port numbers ranging from 0 to 1023 are restricted for system-level services.");
					System.exit(-1);
				}
				file = args[1];				
			}
			// System.out.println("Final Port Num is " + port);
		} 
		catch(Exception e) {
			System.out.println(e);
		}
		
        new DVServerThread(port, file, neighbor_pair, neighbors, currentHost).start();
		new DVClientThread(port, file, neighbor_pair, neighbors, currentHost).start();
    }
}

