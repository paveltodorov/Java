package bg.uni.sofia.fmi.battleships.server;

import java.net.ServerSocket;

import bg.uni.sofia.fmi.battleships.game.components.Rooms;

public class BattleshipsServer {
	private static final int PORT = 80;
	public static Rooms rooms;

	public static void main(String[] args) throws Exception {
		System.out.println("The battleship server is running.");
		Rooms rooms = new Rooms();
		ServerSocket listener = new ServerSocket(PORT);
		try {
			while (true) {
				new ServerThread(listener.accept(), rooms).start();
			}
		} finally {
			listener.close();
		}
	}
}