package bg.uni.sofia.fmi.battleships.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	private final int PORT = 80;

	public Client() throws UnknownHostException, IOException {
		socket = new Socket("localhost", PORT);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	private void run() throws IOException {

		Scanner sc = new Scanner(System.in);
		ClientMessageReceiver receiver = new ClientMessageReceiver(in);
		receiver.start();

		String userName = sc.next();
		out.println(userName);
		out.flush();

		try {
			while (true) {
				String msg = sc.next();
				out.println(msg);
				out.flush();
			}
		} finally {
			sc.close();
		}

	}

	public void close() throws IOException {
		socket.close();
		in.close();
		out.close();
	}

	public static void main(String[] args) throws Exception {
		Client client = new Client();
		client.run();
		client.close();
	}
}
/*
 * 
 * joe create-game game12 set-ships 11h 60v 30h 72v 52h 95h 75h 47h 69v 07h
 * 
 * eli join-game game12 set-ships 00v 60v 42h 17v 12h 82h 63h 66v 97h 58v
 * 
 */