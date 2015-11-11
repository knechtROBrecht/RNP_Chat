package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 * hatServer welcher Clientverbindungen annimmt und so einen Chatraum darstellt
 * 
 * @author rbernhof
 *
 */
public class ChatServer {

	private static final int SERVER_PORT = 4567;

	/**
	 * HashMap mit Namen aller verbundenen Clients als Key und den OutputStreams
	 * (PrintWriter) als Value
	 */
	private static HashMap<String, PrintWriter> clients = new HashMap<String, PrintWriter>();

	/**
	 * startet den ChatServer
	 * 
	 * @param port
	 * @throws IOException
	 */
	private static void run(int port) throws IOException {
		System.out.println("starting the Chatserver...");
		ServerSocket listener = new ServerSocket(SERVER_PORT);
		System.out.println("Chatserver started!");
		URL whatismyip = new URL("http://checkip.amazonaws.com");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				whatismyip.openStream()));
		String ip = in.readLine();
		System.out.println("Server-IP: " + ip);
		try {
			while (true) {
				new ChatWorkerThread(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	public static void main(String[] args) throws Exception {
		run(SERVER_PORT);
	}

	/**
	 * Ist eine Worker-Klasse die zum Client eine Socketverbindung hat und
	 * empfangene Nachrichten an alle Teilnehmer des ChatRaums weiterleitet
	 * 
	 * @author rbernhof
	 *
	 */
	private static class ChatWorkerThread extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		/**
		 * Constructor
		 * 
		 * @param socket
		 */
		public ChatWorkerThread(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Stellt zuerst die Verbindung zum Client her und authentifiziert den
		 * Namen. Wartet danach auf input vom client und broadcastet den weiter
		 * an alle Teilnehmer des Chatraums
		 * 
		 */
		public void run() {
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);

				while (true) {
					out.println(".auth");
					name = in.readLine();
					if (name == null) {
						return;
					}
					synchronized (clients) {
						if (!clients.keySet().contains(name)) {
							broadcast(name + " hat den Chatraum betreten!");
							out.println(".acc");
							clients.put(name, out);
							break;
						}
					}
				}

				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					} else if (input.equals(".quit")) {
						out.println(".quit");
						broadcast(name + " hat den Chatraum verlassen");
						break;
					} else if (input.equals(".clients")) {
						for (String client : clients.keySet()) {
							out.println("user: " + client);
						}
					} else {
						broadcast(name + ": " + input);
					}

				}
			} catch (IOException e) {
				broadcast("Verbindungsabbruch mit " + name);
				broadcast(name + " hat den Chatraum verlassen!");
			} finally {
				if (name != null) {
					clients.remove(name);
				}
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * Sendet die übergebene Message and alle Teilnehmer des Chatraumes
		 * 
		 * @param msg
		 */
		public void broadcast(String msg) {
			for (PrintWriter writer : clients.values()) {
				writer.println(msg);
			}
		}
	}
}