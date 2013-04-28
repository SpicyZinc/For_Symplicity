import java.util.List;
import java.util.ArrayList;


class Primes{
	public static void main(String[] args){
		System.out.println("All primes less and equal to 17: ");
		List result = allPrimes(17);
		System.out.println(result.toString());
	}
	
	public static List allPrimes(int target){
		List<Integer> primes = new ArrayList<Integer>();
		for(int i=2; i<=target; i++){
			if(isPrime(i)){
				primes.add(new Integer(i));
			}
		}
		return primes;
	}
	
	private static boolean isPrime(int num){
		boolean status = false;
		if(num == 2)
			return true;
		for(int j = 2; j <= Math.sqrt(num); j++){
			if (num % j == 0){
				status = false;
				break;
			}
			else 
				return true;
		}
		return status;
	}
}
