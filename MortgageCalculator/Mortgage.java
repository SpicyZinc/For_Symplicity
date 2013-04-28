public class Mortgage {
	public static void main(String[] args) {
		double p = 0.0;
		double downP = 0.0;
		double r = 0.0;
		int t = 0;
		
		if (args.length < 4) {
			System.err.println("Usage: java Mortgage <price> <down payment> <interest rate> <term in years>");
			System.exit(0);
		}
		else {
			p = Double.parseDouble(args[0]);
			downP = Double.parseDouble(args[1]);;
			r = Double.parseDouble(args[2]);;
			t = Integer.parseInt(args[3]);;
		}
		new Mortgage(p, downP, r, t);
	}
	// constructor
	// 
	public Mortgage(double p, double downP, double r, int t) {
		double[] result = calculate(p, downP, r, t);
		
		System.out.printf("The monthly mortgage payment should be %.2f\n", result[0]);
		System.out.printf("Total Amount Paid should be %.2f\n", result[1]);
		System.out.printf("Total Interest Paid should be %.2f\n", result[2]);

	}
	// price, down payment, interest rate, and term.

	// int[0] = monthly payment
	// int[1] = totalAmount;
	// int[2] = totalInterest;
	
	public double[] calculate(double p, double downP, double r, int t) {
		double[] ret = new double[3];
		
		double financed = p - p * downP;
		int n = t * 12;
		double i = r / 12; 
		
		double m = (financed * i) / (1 - (1 / Math.pow((1 + i), n)));
		
		ret[0] = m;
		ret[1] = m * n + p * downP;
		ret[2] = ret[1] - p;
		
		return ret;
	}
}