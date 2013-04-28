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

/***
如果一个数可以被计算多次，O(n^2)的算法还是可以的。计算每一对数的和，然后放入
hashtable。然后再对每一对数的和，在hashtable中查找是否有其负。时间复杂度O(n^
2)，空间复杂度O(n^2)。

一个hashtable保存每两个数的和。另外要一个hashmap保存每个数在array里面出现的
次数。从hashtable里面找到2对之后，在hashmap里面check这4个数字出现的次数是否
valid。


假设我们想找数列里有没有m个数的和为0

l = ceil(m / 2);
k = m - l;

首先，找到数列里每一个可能的l个数的组合，把他们的sum以及用到的数一起存到
hashtable，这个过程是O(n^l) time, O(n^l) space

重复以上过程，用k代替l，O(n^k) time, O(n^k) space

然后，遍历第一个hashtable，对里面的每一个元素
1, 查看第二个hashtable里有没有元素，它们的sum互为正负（O(1) time)
2, 如果有互为正负的数，看他们用到的数有没有重复(O(m))

对于给定的题目，m是常数，所以遍历部分的时间复杂度是O(n^l)

因为 l >= k，所以最终的时间复杂度是 O(n^l), l = ceil(m/2)

m = 2, O(n)
m = 3, O(n^2)
m = 4, O(n^2)
m = 5, O(n^3)
etc...

*/