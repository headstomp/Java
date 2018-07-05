package guessthenumber;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class GuessTheNumber {

	private JFrame frmGuessingGame;
	private JTextField textFieldGuess;
	private JLabel lblYouHave;
	private JLabel lblWarning;

	int count = 0;
	int guess = 0;
	boolean hasWon = false;
	int randomNumber = (int) (Math.random() * 100) + 1;
	ArrayList<Integer> prevGuesses = new ArrayList<>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuessTheNumber window = new GuessTheNumber();
					window.frmGuessingGame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GuessTheNumber() {
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGuessingGame = new JFrame();
		frmGuessingGame.setTitle("Guessing Game");
		frmGuessingGame.setBounds(100, 100, 509, 280);
		frmGuessingGame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGuessingGame.getContentPane().setLayout(null);

		JLabel lblTryToGuess = new JLabel("Try to guess the number, it's between 1 and 100");
		lblTryToGuess.setFont(new Font("Tahoma", Font.BOLD, 18));
		lblTryToGuess.setBounds(25, 11, 504, 38);
		frmGuessingGame.getContentPane().add(lblTryToGuess);

		lblYouHave = new JLabel("You have 10  guesses left.");
		lblYouHave.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblYouHave.setBounds(25, 56, 417, 14);
		frmGuessingGame.getContentPane().add(lblYouHave);

		lblWarning = new JLabel("");
		lblWarning.setForeground(Color.RED);
		lblWarning.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblWarning.setBounds(25, 138, 417, 14);
		frmGuessingGame.getContentPane().add(lblWarning);

		JLabel lblEnterANumber = new JLabel("Enter a Number");
		lblEnterANumber.setBounds(25, 110, 107, 14);
		frmGuessingGame.getContentPane().add(lblEnterANumber);

		textFieldGuess = new JTextField();
		textFieldGuess.setBounds(123, 107, 76, 20);
		frmGuessingGame.getContentPane().add(textFieldGuess);
		textFieldGuess.setColumns(10);

		JLabel lblGuessNumber = new JLabel("Guess Number");
		lblGuessNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblGuessNumber.setBounds(10, 163, 107, 14);
		frmGuessingGame.getContentPane().add(lblGuessNumber);

		JLabel lblTotalGuesses = new JLabel("");
		lblTotalGuesses.setBounds(40, 311, 443, 14);
		frmGuessingGame.getContentPane().add(lblTotalGuesses);

		JLabel lblPreviousGuesses = new JLabel("Previous Guesses");
		lblPreviousGuesses.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPreviousGuesses.setBounds(10, 188, 107, 14);
		frmGuessingGame.getContentPane().add(lblPreviousGuesses);

		JLabel lbShowGuesses = new JLabel("");
		lbShowGuesses.setBounds(127, 188, 317, 14);
		frmGuessingGame.getContentPane().add(lbShowGuesses);

		JButton btnSubmitGuess = new JButton("Submit Guess");
		btnSubmitGuess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					guess = Integer.parseInt(textFieldGuess.getText());
					prevGuesses.add(guess);
					lbShowGuesses.setText(String.format(prevGuesses.toString()));
					count++;
					int guessesRemaining = 10 - count;
					lblYouHave.setText(String.format("You have " + guessesRemaining + " guesses left"));
					lblGuessNumber.setText(String.format("Guess Number " + count));
					textFieldGuess.setText(String.format(""));

					if (guess < 1 || guess > 100) {
						lblWarning.setText(String.format(guess + " Is not between 1 and 100. - NO HINT FOR YOU!"));
						count--;
						int remaining = 10 - count;
						lblYouHave.setText(String.format("You have " + remaining + " guesses left"));
					} else {
						if (guess > randomNumber) {
							lblWarning.setText(String.format("The number is smaller than " + guess + " Try again"));
						} else if (guess < randomNumber) {
							lblWarning.setText(String.format("The number is larger than " + guess + " Try again"));
						} else {
							lblWarning.setText(
									String.format("You got it in " + count + " tries, the number is " + guess));
							btnSubmitGuess.setVisible(false);
						}

						if (count == 10) {
							lblWarning.setText(String.format(
									"Sorry that is " + count + " guesses, You lose. The number was " + randomNumber));
							btnSubmitGuess.setVisible(false);
						}
					}
				} catch (NumberFormatException e) {
					lblWarning.setText(String.format("You must enter an integer"));
					textFieldGuess.setText(String.format(""));
				}

			}
		});
		btnSubmitGuess.setBounds(209, 106, 118, 23);
		frmGuessingGame.getContentPane().add(btnSubmitGuess);

		JButton btnReset = new JButton("Reset");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				count = 0;
				guess = 0;
				lblWarning.setText(String.format(" "));
				lblGuessNumber.setText(String.format("Guess Number 1 "));
				lblYouHave.setText(String.format("You have 10 guesses left"));
				textFieldGuess.setText(String.format(""));
				lbShowGuesses.setText(String.format(""));
				btnSubmitGuess.setVisible(true);
				prevGuesses.clear();
				randomNumber = (int) (Math.random() * 100) + 1;
			}
		});
		btnReset.setBounds(336, 106, 89, 23);
		frmGuessingGame.getContentPane().add(btnReset);



	}




}
