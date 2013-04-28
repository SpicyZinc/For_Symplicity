/**
GUI Mortgage Calculator
Author: Liang Xin

*/
=========
Implementation:

Mortgage.java: is an direct implementation of mortgage calculation, 
input four parameters price, down payment, interest rate, term in years in the command line and enter, 
the code will output result.

MortgageCalculator.java: is a simple GUI implementation of mortgage calculation.
In the User Interface, input price, down payment, interest rate, term into four text fields,
then click on "Calculate" button, the result will show in the three areas.
Click on "Clear" button, it can do the next calculation.

=========
Two things to note: 

1. after finishing typing one number in a text field, you have to press "enter" to register this event.
Because of limited time, I did not use KeyListener to implement a more robust version of GUI. 

2. In the PDF file of Assignment#1 given to me, I guess there is a typo (".83" not ".03") there:

Total Amount Paid should be				$409,163.83
Total Interest Paid should be           $209,163.03

These two numbers should have an exact differenc of $200,000, according to the example here. 

I might be wrong, but my code output 
$409,163.83
$209,163.83
Their difference is exactly $200,000.


=========
Files submitted:
1. Mortgage.java
2. MortgageCalculator.java
3. ReadMe.txt

=========
To compile:
	1. javac Mortgage.java	
	2. javac MortgageCalculator.java
	
=========	
To run:
	1. Usage: java Mortgage <price> <down payment> <interest rate> <term in years>
	2. Usage: java MortgageCalculator
	
=========

