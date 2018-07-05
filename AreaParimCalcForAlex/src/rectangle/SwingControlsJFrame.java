package rectangle;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class SwingControlsJFrame {

	private JFrame frmAreaAndPerimeter;
	private JTextField textFieldWidth;
	private JTextField textFieldLength;
	private JTextField textFieldArea;
	private JTextField textFieldPerimiter;
	private JButton btnClearValues;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingControlsJFrame window = new SwingControlsJFrame();
					window.frmAreaAndPerimeter.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public SwingControlsJFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAreaAndPerimeter = new JFrame();
		frmAreaAndPerimeter.setTitle("Area and Perimeter Calculator");
		frmAreaAndPerimeter.setBounds(100, 100, 395, 262);
		frmAreaAndPerimeter.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAreaAndPerimeter.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("Enter Width");
		lblNewLabel.setBounds(20, 30, 83, 14);
		frmAreaAndPerimeter.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Enter Length");
		lblNewLabel_1.setBounds(21, 55, 82, 14);
		frmAreaAndPerimeter.getContentPane().add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Area");
		lblNewLabel_2.setBounds(20, 151, 83, 14);
		frmAreaAndPerimeter.getContentPane().add(lblNewLabel_2);

		JLabel lblNewLabel_3 = new JLabel("Parimiter");
		lblNewLabel_3.setBounds(20, 179, 65, 14);
		frmAreaAndPerimeter.getContentPane().add(lblNewLabel_3);

		textFieldWidth = new JTextField();
		textFieldWidth.setText("");
		textFieldWidth.setBounds(102, 27, 124, 20);
		frmAreaAndPerimeter.getContentPane().add(textFieldWidth);
		textFieldWidth.setColumns(10);

		textFieldLength = new JTextField();
		textFieldLength.setText("");
		textFieldLength.setBounds(102, 52, 124, 20);
		frmAreaAndPerimeter.getContentPane().add(textFieldLength);
		textFieldLength.setColumns(10);

		textFieldArea = new JTextField();
		textFieldArea.setText("0.0");
		textFieldArea.setBounds(102, 148, 124, 20);
		frmAreaAndPerimeter.getContentPane().add(textFieldArea);
		textFieldArea.setColumns(10);

		textFieldPerimiter = new JTextField();
		textFieldPerimiter.setText("0.0");
		textFieldPerimiter.setBounds(102, 176, 124, 20);
		frmAreaAndPerimeter.getContentPane().add(textFieldPerimiter);
		textFieldPerimiter.setColumns(10);

		JButton btnCalculate = new JButton("Calculate");
		btnCalculate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("unused")
				double area, perm, len, wid;
				len = Double.parseDouble(textFieldLength.getText());
				wid = Double.parseDouble(textFieldWidth.getText());
				area = len * wid;
				perm = 2 * (len + wid);
				textFieldArea.setText(String.format("%.2f", area));
				textFieldPerimiter.setText(String.format("%.2f", perm));

			}
		});
		btnCalculate.setBounds(20, 97, 89, 23);
		frmAreaAndPerimeter.getContentPane().add(btnCalculate);

		btnClearValues = new JButton("Clear Values");
		btnClearValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldArea.setText(String.format("%.2f", 0.0));
				textFieldPerimiter.setText(String.format("%.2f", 0.0));
				textFieldLength.setText("");
				textFieldWidth.setText("");
			}
		});
		btnClearValues.setBounds(119, 97, 107, 23);
		frmAreaAndPerimeter.getContentPane().add(btnClearValues);

	}


}
