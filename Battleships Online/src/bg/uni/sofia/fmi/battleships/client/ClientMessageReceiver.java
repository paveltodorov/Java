package bg.uni.sofia.fmi.battleships.client;

import java.io.BufferedReader;
import java.io.IOException;

public class ClientMessageReceiver extends Thread {
	private BufferedReader reader;

	public ClientMessageReceiver(BufferedReader reader) {
		this.reader = reader;
		this.setDaemon(true);
	}

	public void run() {
		while (true) {
			try {
				String msg = reader.readLine();
				System.out.println(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public BufferedReader getIn() {
		return reader;
	}

	public void setReader(BufferedReader reader) {
		this.reader = reader;
	}
}
