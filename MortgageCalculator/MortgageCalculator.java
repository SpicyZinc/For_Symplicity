import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MortgageCalculator extends JFrame {

	private JPanel sourcePanel;
	private JPanel functionPanel;
	private JPanel resultPanel;
	
	private GridLayout sourceGridLayout;
	private GridLayout functionGridLayout;
	private GridLayout resultGridLayout;

	private JLabel pLabel;
	private JLabel dpLabel;
	private JLabel rLabel;
	private JLabel tLabel;
	
	private JLabel mLabel;
	private JLabel taLabel;
	private JLabel tiLabel;
	
	private JTextField pTextField;
	private JTextField dpTextField;
	private JTextField rTextField;
	private JTextField tTextField;	

	private JButton calculateButton;
	private JButton clearButton;
	
	private JTextArea mTextArea;	
	private JTextArea taTextArea;	
	private JTextArea tiTextArea;
	
	private double p;
	private double downP;
	private double r;
	private int t;
	
	public static void main(String[] args) {
		MortgageCalculator test = new MortgageCalculator();
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.setSize(550, 320);
		test.setVisible(true);
	}
	
	// constructor
	//
	public MortgageCalculator() {
		super("Mortgage Calculator");		
		
		sourcePanel = new JPanel();
		functionPanel = new JPanel();
		resultPanel = new JPanel();
		
		sourceGridLayout = new GridLayout(4, 2, 5, 5);				
		sourcePanel.setLayout(sourceGridLayout);
		
		functionGridLayout = new GridLayout(4, 2);				
		functionPanel.setLayout(functionGridLayout);
		
		resultGridLayout = new GridLayout(3, 2, 10, 10);				
		resultPanel.setLayout(resultGridLayout);		
		
		// <price> <down payment> <interest rate> <term in years>
		pLabel = new JLabel("Price ($)");	
		pLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		dpLabel = new JLabel("Down Payment Rate (%)");
		dpLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		rLabel = new JLabel("Interest Rate (%)");
		rLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		tLabel = new JLabel("Term (years)");
		tLabel.setFont(new Font("Arial", Font.PLAIN, 12));

		ClickHandler handler = new ClickHandler();
		
		pTextField = new JTextField();
		pTextField.setFont(new Font("Arial", Font.PLAIN, 12));
		pTextField.setEditable(true);
		pTextField.addActionListener(handler);
		
		dpTextField = new JTextField();
		dpTextField.setFont(new Font("Arial", Font.PLAIN, 12));
		dpTextField.setEditable(true);
		dpTextField.addActionListener(handler);
		
		rTextField = new JTextField();
		rTextField.setFont(new Font("Arial", Font.PLAIN, 12));
		rTextField.setEditable(true);
		rTextField.addActionListener(handler);
		
		tTextField = new JTextField();
		tTextField.setFont(new Font("Arial", Font.PLAIN, 12));
		tTextField.setEditable(true);
		tTextField.addActionListener(handler);
		
		sourcePanel.add(pLabel);
		sourcePanel.add(pTextField);
		sourcePanel.add(dpLabel);
		sourcePanel.add(dpTextField);
		sourcePanel.add(rLabel);
		sourcePanel.add(rTextField);
		sourcePanel.add(tLabel);
		sourcePanel.add(tTextField);

		calculateButton = new JButton("Calculate");
		calculateButton.addActionListener(handler);
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(handler);	

		functionPanel.add(calculateButton);
		functionPanel.add(clearButton);
		
		mLabel = new JLabel("The monthly mortgage payment ($)");
		mLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		taLabel = new JLabel("Total Amount Paid ($)");
		taLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		
		tiLabel = new JLabel("Total Interest Paid ($)");
		tiLabel.setFont(new Font("Arial", Font.PLAIN, 12));
				
		mTextArea = new JTextArea();
		mTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
		mTextArea.setEditable(false);
		
		taTextArea = new JTextArea();
		taTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
		taTextArea.setEditable(false);
		
		tiTextArea = new JTextArea();
		tiTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
		tiTextArea.setEditable(false);		
						
		resultPanel.add(mLabel);        resultPanel.add(mTextArea);
		resultPanel.add(taLabel);       resultPanel.add(taTextArea);
		resultPanel.add(tiLabel);		resultPanel.add(tiTextArea);
		
		add(sourcePanel, BorderLayout.WEST);
		add(functionPanel, BorderLayout.EAST);
		add(resultPanel, BorderLayout.SOUTH);
	}
	
	// handler
	//
	private class ClickHandler implements ActionListener {	
		public void actionPerformed(ActionEvent event) {		
			if (event.getSource() == pTextField) {
				String text = String.format("%s", event.getActionCommand()); 
				System.out.println("price == " + text);
				p = Double.parseDouble(text);	
			}
			if (event.getSource() == dpTextField) {
				String text = event.getActionCommand(); 
				System.out.println("downP == " + text);
				downP = Double.parseDouble(text);	
			}
			if (event.getSource() == rTextField) {
				String text = event.getActionCommand(); 
				System.out.println("rate == " + text);
				r = Double.parseDouble(text);	
			}
			if (event.getSource() == tTextField) {
				String text = event.getActionCommand(); 
				System.out.println("term == " + text);
				t = Integer.parseInt(text);	
			}
			
			if (event.getSource() == calculateButton) {
				double[] ret = new double[3];	
				
				double financed = p - p * downP;
				int n = t * 12;
				double i = r / 12; 
				
				double m = (financed * i) / (1 - (1 / Math.pow((1 + i), n)));
				
				ret[0] = m;
				ret[1] = m * n + p * downP;
				ret[2] = ret[1] - p;
				
				mTextArea.append(Double.toString(ret[0]));
				taTextArea.append(Double.toString(ret[1]));
				tiTextArea.append(Double.toString(ret[2]));
				
				System.out.printf("The monthly mortgage payment should be %.2f\n", ret[0]);
				System.out.printf("Total Amount Paid should be %.2f\n", ret[1]);
				System.out.printf("Total Interest Paid should be %.2f\n", ret[2]);
				
				// return ret;
			}			
				
			if (event.getSource() == clearButton) {
				pTextField.setText("");
				dpTextField.setText("");
				rTextField.setText("");
				tTextField.setText("");
				
				mTextArea.setText("");
				taTextArea.setText("");
				tiTextArea.setText("");			
				
				p = 0.0;
				downP = 0.0;
				r = 0.0;
				t = 0;		

				System.out.println("clear");
			}			
		}	
	}
}