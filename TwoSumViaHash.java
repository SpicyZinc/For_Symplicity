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
���һ�������Ա������Σ�O(n^2)���㷨���ǿ��Եġ�����ÿһ�����ĺͣ�Ȼ�����
hashtable��Ȼ���ٶ�ÿһ�����ĺͣ���hashtable�в����Ƿ����为��ʱ�临�Ӷ�O(n^
2)���ռ临�Ӷ�O(n^2)��

һ��hashtable����ÿ�������ĺ͡�����Ҫһ��hashmap����ÿ������array������ֵ�
��������hashtable�����ҵ�2��֮����hashmap����check��4�����ֳ��ֵĴ����Ƿ�
valid��


��������������������û��m�����ĺ�Ϊ0

l = ceil(m / 2);
k = m - l;

���ȣ��ҵ�������ÿһ�����ܵ�l��������ϣ������ǵ�sum�Լ��õ�����һ��浽
hashtable�����������O(n^l) time, O(n^l) space

�ظ����Ϲ��̣���k����l��O(n^k) time, O(n^k) space

Ȼ�󣬱�����һ��hashtable���������ÿһ��Ԫ��
1, �鿴�ڶ���hashtable����û��Ԫ�أ����ǵ�sum��Ϊ������O(1) time)
2, ����л�Ϊ�����������������õ�������û���ظ�(O(m))

���ڸ�������Ŀ��m�ǳ��������Ա������ֵ�ʱ�临�Ӷ���O(n^l)

��Ϊ l >= k���������յ�ʱ�临�Ӷ��� O(n^l), l = ceil(m/2)

m = 2, O(n)
m = 3, O(n^2)
m = 4, O(n^2)
m = 5, O(n^3)
etc...

*/