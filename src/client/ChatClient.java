package client;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;

public class ChatClient {
	
	private static final int SERVER_PORT = 4567;

	private JFrame frame;
	private BufferedReader in;
	private PrintWriter out;
	private JTextField textField;
	private JTextArea messageArea, clientList;
	private JScrollPane scrollPane, scrollPane2;
	private JPanel contentPane;
	private JButton quit, send;
	private Socket socket;
	private boolean clientRunning = false;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient client = new ChatClient();
					client.frame.setVisible(true);
					client.run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ChatClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 500, 350);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		textField = new JTextField(40);
		textField.setEditable(false);
		frame.getContentPane().add(textField, BorderLayout.SOUTH);
		
		messageArea = new JTextArea(8, 40);
		scrollPane = new JScrollPane(messageArea);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		contentPane = new JPanel(new BorderLayout(0, 0));
		quit = new JButton("Quit");
		quit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(".quit");
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				messageArea.append("leaving chatroom. bye!");
				clientRunning = false;
			}
		});
		contentPane.add(quit, BorderLayout.NORTH);
		
		send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(".message " + textField.getText());
				textField.setText("");
			}
		});
		contentPane.add(send, BorderLayout.SOUTH);
		
		clientList = new JTextArea();
		scrollPane2 = new JScrollPane(clientList);
		contentPane.add(scrollPane2, BorderLayout.CENTER);
		
		frame.getContentPane().add(contentPane, BorderLayout.EAST);
		
		frame.pack();
	}
	
	
	/**
	 * Stell die Verbindung zu einem ChatServer her und wartet dann auf input
	 * vom Sockel um diesen zu verarbeiten
	 * 
	 * @throws IOException
	 */
	private void run() throws IOException {

		clientRunning = true;

		String serverAddress = getServerAddress();
		socket = null;
		if (serverAddress.contains(":")) {
			String[] frak = serverAddress.split(":");
			socket = new Socket(frak[0], Integer.parseInt(frak[1]));
		} else {
			socket = new Socket(serverAddress, SERVER_PORT);
		}
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// true für autoflush
		out = new PrintWriter(socket.getOutputStream(), true);

		while (clientRunning) {
			String line = in.readLine();
			
			if(line == null){
//				System.out.println(line);
				continue;
			}
			System.out.println(line);
			if (line.startsWith(".auth")) {
				out.println(chooseName());
			} else if (line.startsWith(".authfail")) {
				messageArea.append("Authfail, Name already used, try a different one");
			} else if (line.startsWith(".ack")) {
				textField.setEditable(true);
				out.println("clients");
			} else if (line.startsWith(".new")) {
				out.println("clients");
			} else if (line.startsWith(".clients")) {
				renewClientList(line.substring(9, line.length()));
			} else if (line.startsWith(".message")) {
				messageArea.append(line.substring(9, line.length()) + "\n");
			}
		}
	}

	/**
	 * Erstellt ein InputDialog und fragt den Benutzer nach der IP Adresse des
	 * Servers
	 * 
	 * @return die Server-Adresse die eingegeben wurde als String
	 */
	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame, "IP Adresse des Servers:", "RNP Aufgabe2 Chatprogramm",
				JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Erstellt ein InputDialog und fragt den Benutzer nach dem gewünschtem
	 * Chatnamen
	 * 
	 * @return den gewählten Chatamen der eingegeben wurde als String
	 */
	private String chooseName() {
		return JOptionPane.showInputDialog(frame, "Bitte Namen wählen:", "Auswahl des Namens",
				JOptionPane.PLAIN_MESSAGE);
	}
	
	private void renewClientList(String substring) {
		clientList.setText("");
		String[] clients = substring.split("; ");
		System.out.println(clients);
	}

}
