package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChatClient {

	BufferedReader in;
	PrintWriter out;
	JFrame frame = new JFrame("Chatter");
	JTextField textField = new JTextField(40);
	JTextArea messageArea = new JTextArea(8, 40);

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

	private String getServerAddress() {
		return JOptionPane.showInputDialog(frame, "IP Adresse des Servers:", "RNP Aufgabe2 Chatprogramm",
				JOptionPane.QUESTION_MESSAGE);
	}

	private String getName() {
		return JOptionPane.showInputDialog(frame, "Bitte Namen wählen:", "Auswahl des Namens",
				JOptionPane.PLAIN_MESSAGE);
	}

	private void run() throws IOException {
		
		boolean clientRunning = true;

		String serverAddress = getServerAddress();
		Socket socket = null;
		if(serverAddress.contains(":")){
			String[] frak = serverAddress.split(":");
			socket = new Socket(frak[0], Integer.parseInt(frak[1]));
		}else{
			socket = new Socket(serverAddress, 4567);
		}
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		//true für autoflush
		out = new PrintWriter(socket.getOutputStream(), true);

		while (clientRunning) {
			String line = in.readLine();
			if (line.startsWith(".auth")) {
				out.println(getName());
			} else if (line.startsWith(".acc")) {
				textField.setEditable(true);
			}else if(line.startsWith(".quit")){
				socket.close();
				messageArea.append("leaving chatroom. bye!");
				clientRunning = false;
			}else {
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
