package server;

import java.io.*;
import java.net.*;
import java.util.HashSet;

public class ChatServer {

	private static final int SERVER_PORT = 4567;

	private static HashSet<String> clients = new HashSet<String>();
	private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

	public static void main(String[] args) throws Exception {
		System.out.println("starting the Chatserver...");
		ServerSocket listener = new ServerSocket(SERVER_PORT);
		System.out.println("Chatserver started!");
		try {
			while (true) {
				new ChatWorkerThread(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}

	private static class ChatWorkerThread extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public ChatWorkerThread(Socket socket) {
			this.socket = socket;
		}

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
						if (!clients.contains(name)) {
							broadcast(name + " hat den Chatraum betreten!");
							clients.add(name);
							break;
						}
					}
				}

				out.println(".acc");
				writers.add(out);

				while (true) {
					String input = in.readLine();
					if (input == null) {
						return;
					} else if (input.equals(".quit")) {
						out.println(".quit");
						broadcast(name + " hat den Chatraum verlassen");
						break;
					} else if (input.equals(".clients")) {
						for (String client : clients) {
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
				if (out != null) {
					writers.remove(out);
				}
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		public void broadcast(String msg) {
			for (PrintWriter writer : writers) {
				writer.println(msg);
			}
		}
	}
}