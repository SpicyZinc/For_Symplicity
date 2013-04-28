import java.util.Arrays;
import java.util.Hashtable;
import java.io.*;

class Stopwatch { 

    private final long start;

    public Stopwatch() {
        start = System.currentTimeMillis();
    } 

    // return time (in seconds) since this object was created
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    } 
}

public class TwoSumViaHash{

	public static int twoSumViaHash(int[] a){
		Hashtable<Integer, Integer> aHashtable = new Hashtable<Integer, Integer>();
		for (int i = 0; i < a.length; i++){
			aHashtable.put(a[i], new Integer(i)); 
		}
		
		int count = 0;
		for (int i = 0; i < a.length; i++){
			if(aHashtable.get(0-a[i]) == null) continue;
			else{
				int j = aHashtable.get(0-a[i]);
				if(j < i){ ///to confine j<i is the key point OR   j > i is OK
					System.out.println(a[i] + " " + a[j]);
					count++;
				}
			}
		}
		return count;
	}		

    public static void main(String[] args) throws IOException { 
	
		int[] a = new int[8000];
		int count = 0;
		
		FileReader in = new FileReader(args[0]);
		BufferedReader integerFile = new BufferedReader(in);
		String aLine;
		
		while((aLine = integerFile.readLine()) != null){						
			//System.out.println(aLine);
			int aNumber = Integer.parseInt(aLine);
			a[count] = aNumber;
			count++;
		}
		
        Stopwatch timer = new Stopwatch();
        int cnt = twoSumViaHash(a);
        System.out.printf("Elapsed Time = %f s\n", timer.elapsedTime());
        System.out.println(cnt);
	} 
}
