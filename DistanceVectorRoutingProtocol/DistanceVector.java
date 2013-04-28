import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.ConcurrentLinkedQueue;

class DistanceVector {	
	private String source;
	private String via;
	private String dest;
	private double cost;
	
	// complete constructor
	public DistanceVector(String s, String v, String d, double cost) {
		// s's neighbors
		source = s;
		via = v;
		dest = d;
		this.cost = cost;
	}

	public String getSource() {
		return source;
	}
	public String getVia() {
		return via;
	}
	public String getDest() {
		return dest;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double newCost) {
		cost = newCost;
	}
}