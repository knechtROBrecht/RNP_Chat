package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ChatClient welcher sich zu einem ChatServer verbinden kann um so einen
 * Chatraum beizutreten
 * 
 * @author rbernhof
 *
 */
public class ChatClient {
	
	private static final int SERVER_PORT = 4567;

	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);

	/**
	 * Constructor welcher die GUI eines ChatClients erstellt
	 */
	public ChatClient() {

		textField.setEditable(false);
		messageArea.setEditable(false);
		frame.getContentPane().add(textField, "North");
		frame.getContentPane().add(new JScrollPane(messageArea), "Center");
		frame.pack();

		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println(textField.getText());
				textField.setText("");
			}
		});
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
	private String getName() {
		return JOptionPane.showInputDialog(frame, "Bitte Namen wählen:", "Auswahl des Namens",
				JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Stell die Verbindung zu einem ChatServer her und wartet dann auf input
	 * vom Sockel um diesen zu verarbeiten
	 * 
	 * @throws IOException
	 */
	private void run() throws IOException {

		boolean clientRunning = true;

		String serverAddress = getServerAddress();
		Socket socket = null;
		if (serverAddress.contains(":")) {
			System.out.println("lol");
			String[] frak = serverAddress.split(":");
			socket = new Socket(frak[0], Integer.parseInt(frak[1]));
		} else {
			System.out.println("blub");
			socket = new Socket(serverAddress, SERVER_PORT);
		}
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		// true für autoflush
		out = new PrintWriter(socket.getOutputStream(), true);

		while (clientRunning) {
			String line = in.readLine();
			if (line.startsWith(".auth")) {
				out.println(getName());
			} else if (line.startsWith(".acc")) {
				textField.setEditable(true);
			} else if (line.startsWith(".quit")) {
				socket.close();
				messageArea.append("leaving chatroom. bye!");
				clientRunning = false;
			} else {
				messageArea.append(line + "\n");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		ChatClient client = new ChatClient();
		client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.frame.setVisible(true);
		client.run();
	}
}
