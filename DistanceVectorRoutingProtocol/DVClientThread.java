import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentLinkedQueue;

class DVClientThread extends Thread {
	public final double INFINITY = 99999.0;
	private long TEN_SECONDS = 10000;
	private int BUFFER_SIZE = 32768;
	private int port;
	private String file;
	// DatagramSocket on client
	private DatagramSocket clientSocket = null;
	private boolean updating = true;	
	private int count = 0; // while() times
	private int neighborsCnt = 0;
	Hashtable<String, Double> neighbor_pair = null;		
	// Hashtable<String, Integer> neighbors = null;
	ArrayList<String> neighbors = null;
	// LinkedList<DistanceVector[]> currentHost = null;
	Hashtable<String, Vector<DistanceVector>> currentHost = null;
 
    public DVClientThread(int port, String file, Hashtable<String, Double> neighbor_pair, ArrayList<String> neighbors, Hashtable<String, Vector<DistanceVector>> currentHost) throws IOException {
        super("DVClientThread");
		this.port = port;
		this.file = file;	
		clientSocket = new DatagramSocket();
		
		this.neighbor_pair = neighbor_pair;
		this.neighbors = neighbors;
		this.currentHost = currentHost;
    }
 
    public void run() { 
        while(true) {
			try {
				String hostName = InetAddress.getLocalHost().getHostName();
				count++;
				System.out.println("\nAt Host " + hostName + ", ## sequence number " + count + "\n");
			}
			catch(UnknownHostException uhe) {
				System.out.println(uhe);
			}
			// as client, send out routing info. at interval			
			try {							   
				// get a datagram socket
				// clientSocket = new DatagramSocket();
				DatagramPacket clientPacket = null;
				// send update
				byte[] bufSent = new byte[BUFFER_SIZE];
				String dString = "";
				// read newly received information
				if(count == 1) {
					readHost(file);
					// LinkedList<DistanceVector[]> dvList = currentHost;					
					// dString = DVToRT(dvList);
					dString = DVToRT(currentHost);
					for(int n=0; n<neighbors.size(); n++) {
					// for(String neighbor : neighbors.keySet()) {
						// System.out.println("Sent to " + neighbor + ":\n" + dString);
						bufSent = dString.getBytes();
						// InetAddress address = InetAddress.getByName(neighbor);
						InetAddress address = InetAddress.getByName(neighbors.get(n));
						clientPacket = new DatagramPacket(bufSent, bufSent.length, address, port);
						clientSocket.send(clientPacket);
					}
					// System.out.println("Sent to " + neighbors.keySet().size() + " neighbors" + ":\n" + dString);
					String simpleString = simplePrint(dString);
					System.out.println("Sent to " + neighbors.size() + " neighbors" + ":\n" + simpleString);
				}
				else if(count > 1) {
					boolean isChanged = false;
					LinkedList<String> newPair = changeDetect(file);
					for(int i=0; i<newPair.size(); i++) {
						String[] pairCost = newPair.get(i).split(" ");
						if(neighbor_pair.get(pairCost[0]) != Double.parseDouble(pairCost[1])) {
							isChanged = true;
							// System.out.println("who - who " + pairCost[0]);
							// System.out.println(neighbor_pair.get(pairCost[0]));
							// System.out.println(Double.parseDouble(pairCost[1]));
							// System.out.println("Cost is changed " + isChanged);
							// double tmp = neighbor_pair.remove(pairCost[0]);	
							neighbor_pair.remove(pairCost[0]);							
							// System.out.println("What cost has been removed " + tmp);
							neighbor_pair.put(pairCost[0], Double.parseDouble(pairCost[1]));
							// System.exit(0);							
						}
					}
					
					if(isChanged) {
						// currentHost = new LinkedList<DistanceVector[]>();
						// need to empty theset two holders first when read again
						/// key ****** part, no need to empty hashtable
						/// because put will drop the previous object on the position 
						// currentHost = new Hashtable<String, Vector<DistanceVector>>();
						neighbors = new ArrayList<String>();
						// neighbor_pair = new Hashtable<String, Double>();
						readHost(file);
						// LinkedList<DistanceVector[]> dvList = currentHost;
						// dString = DVToRT(dvList);
						dString = DVToRT(currentHost);
						for(int n=0; n<neighbors.size(); n++) {
						// for(String neighbor : neighbors.keySet()) {
							// System.out.println("Sent to " + neighbor + ":\n" + dString);
							bufSent = dString.getBytes();
							// InetAddress address = InetAddress.getByName(neighbor);
							InetAddress address = InetAddress.getByName(neighbors.get(n));							
							clientPacket = new DatagramPacket(bufSent, bufSent.length, address, port);
							clientSocket.send(clientPacket);
						}
						// System.out.println("Sent to " + neighbors.keySet().size() + " neighbors" + ":\n" + dString);
						String simpleString = simplePrint(dString);
						System.out.println("Sent to " + neighbors.size() + " neighbors" + ":\n" + simpleString);
						//
						isChanged = false;
					}
					else {
						// LinkedList<DistanceVector[]> dvList = currentHost;					
						// dString = DVToRT(dvList);
						dString = DVToRT(currentHost);						
						for(int n=0; n<neighbors.size(); n++) {
						// for(String neighbor : neighbors.keySet()) {
							// System.out.println("Sent to " + neighbor + ":\n" + dString);
							bufSent = dString.getBytes();
							// InetAddress address = InetAddress.getByName(neighbor);
							InetAddress address = InetAddress.getByName(neighbors.get(n));							
							clientPacket = new DatagramPacket(bufSent, bufSent.length, address, port);
							clientSocket.send(clientPacket);
						}
						// System.out.println("Sent to " + neighbors.keySet().size() + " neighbors" + ":\n" + dString);
						String simpleString = simplePrint(dString);
						System.out.println("Sent to " + neighbors.size() + " neighbors" + ":\n" + simpleString);
					}
				}
				
				// get response
				// reuse packet by "new" for receiving
				/*
				clientPacket = new DatagramPacket(bufSent, bufSent.length);
				clientSocket.receive(clientPacket);
				String received = new String(clientPacket.getData(), 0, clientPacket.getLength());
				// System.out.println("Received from " + clientPacket.getAddress().getHostName() + ":\n" + received);
				*/
				// sleep for a while
				try {
					Thread.sleep(TEN_SECONDS);
				} 
				catch(InterruptedException e) { 
					System.out.println(e);
				}
			}
			catch(IOException e) {
				e.printStackTrace();
				System.out.println(e);
			}			
        }
		// clientSocket.close();
    }
	
	// read current host, meanwhile populate currentHost list
	private void readHost(String file) {		
        FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		String res = "";
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			String aLine;
			int count = 0;
			while((aLine = br.readLine()) != null) {				
				String[] hostFile = file.split("\\.dat");
				String[] words = aLine.split(" ");
				if(words.length == 2) {					
					// neighbors.put(words[0], count);
					neighbors.add(words[0]);
					neighbor_pair.put(hostFile[0] + "-" + words[0], Double.parseDouble(words[1]));
				}
				count++;
			}
			// otherwise use iterator
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			while((aLine = br.readLine()) != null) {
				String[] hostFile = file.split("\\.dat");
				String[] words = aLine.split(" ");
				// DistanceVector[] dvLine = new DistanceVector[neighbors.keySet().size()];
				// DistanceVector[] dvLine = new DistanceVector[neighbors.size()];
				Vector<DistanceVector> dvLine = new Vector<DistanceVector>();
				int i = 0;
				if(words.length == 2) {				
					Iterator<String> itr = neighbors.iterator();
					while (itr.hasNext()) {
						String element = itr.next();
						if(element.equals(words[0])) {
							// dvLine[i] = new DistanceVector(hostFile[0], element, words[0], Double.parseDouble(words[1]));
							dvLine.add(new DistanceVector(hostFile[0], element, words[0], Double.parseDouble(words[1])));
						}
						else {
							// dvLine[i] = new DistanceVector(hostFile[0], element, words[0], INFINITY);
							dvLine.add(new DistanceVector(hostFile[0], element, words[0], INFINITY));
						}
						// i++;						
					}
/*					
					for(String key : neighbors.keySet()) {
						if(key.equals(words[0])) {
							dvLine[i] = new DistanceVector(hostFile[0], key, words[0], Double.parseDouble(words[1]));
						}
						else {
							dvLine[i] = new DistanceVector(hostFile[0], key, words[0], INFINITY);
						}
						i++;
					}
*/
					// currentHost.add(dvLine);
					currentHost.put(hostFile[0] + "-" + words[0], dvLine);
				}
				else if(words.length == 1) {
					neighborsCnt = Integer.parseInt(words[0]);
					// System.out.println("how many " + neighborsCnt);
				}
			}
		}
		catch(FileNotFoundException ex) {
			 System.err.println("Could not find text file.");
		}
		catch(IOException e) {
			System.err.println(e);
		}
		finally {
            try {
                br.close();
				isr.close();
                fis.close();
            } 
			catch(IOException ex) {
				System.out.println(ex);
            }
        }		
        // return res;
    }	
	
	// read host file to check cost if changes
	private LinkedList<String> changeDetect(String file) {		
        FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		LinkedList<String> res = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			res = new LinkedList<String>();
			String aLine;
			while((aLine = br.readLine()) != null) {				
				String[] hostFile = file.split("\\.dat");
				String[] words = aLine.split(" ");
				if(words.length == 2) {					
					res.add(hostFile[0] + "-" + words[0] + " " + Double.parseDouble(words[1]));
				}
			}
		}
		catch(FileNotFoundException ex) {
			 System.err.println("Could not find text file.");
		}
		catch(IOException e) {
			System.err.println(e);
		}
		finally {
            try {
                br.close();
				isr.close();
                fis.close();
            } 
			catch(IOException ex) {
				System.out.println(ex);
            }
        }
		
        return res;
    }	

	// find min then convert RT
	// DVToRoutingTable to send	
	private String DVToRT(Hashtable<String, Vector<DistanceVector>> dvList) {
		Double min = Double.MAX_VALUE;
		String group1 = "shortest path from node ";
		String group3 = " to node ";
		String group5 = ": the next hop is node ";
		String group7 = " and the cost is ";
		
		String res = "";
		
		for(Vector<DistanceVector> v : dvList.values()) {
			String source = "";
			String via = "";
			String dest = "";
			double[] costs = new double[v.size()];			
			// populate costs double array and find min index 
			for(int j=0; j<v.size(); j++) {
				costs[j] = v.get(j).getCost();				
			}
			source = v.get(findMinIndex(costs)).getSource();
			dest = v.get(findMinIndex(costs)).getDest();
			via = v.get(findMinIndex(costs)).getVia();
			min = v.get(findMinIndex(costs)).getCost();
			
			res += group1 + source + group3 + dest + group5 + via + group7 + min + "\n";
		}
		
		return res;			
	}
	
	// for simple output purpose
	private String simplePrint(String input) {
		String res = "";
		String hyphen = "-";
		String space = " ";
		
		String[] lines = input.split("\\n");
		for(int i=0; i<lines.length; i++) {
			String[] array = lines[i].split(" ");
			String[] start = array[4].split("\\.");
			String[] dest = array[7].split("\\.");
			String[] via = array[13].split("\\.");
			
			// res += + start[0] + hyphen + via[0] + hyphen + dest[0] + space + array[18] + "\n";
			res += "shortest path: " + start[0] + " to " + dest[0] + " via " + via[0] + space + array[18] + "\n";			

		}

		return res;			
	}
	
	private int findMinIndex(double[] d) {
		double min = Double.MAX_VALUE;
		for(int i=0; i<d.length; i++) {
			if(min > d[i]) {
				min = d[i];
			}
		}
		
		for(int i=0; i<d.length; i++) {
			if(min == d[i]) {
				return i;
			}
		}
		return -1;
	}
}