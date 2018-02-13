package bg.uni.sofia.fmi.battleships.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import bg.uni.sofia.fmi.battleships.exceptions.GameNotFoundException;
import bg.uni.sofia.fmi.battleships.exceptions.InvalidFieldPositionException;
import bg.uni.sofia.fmi.battleships.exceptions.RoomNotFoundException;
import bg.uni.sofia.fmi.battleships.game.components.GameRoom;
import bg.uni.sofia.fmi.battleships.game.components.Player;
import bg.uni.sofia.fmi.battleships.game.components.Rooms;

public class ServerThread extends Thread {
	private String name;
	private String roomID;
	private int playerID;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private static Hashtable<String, GameRoom> roomHashTable = new Hashtable<String, GameRoom>();
	private static Rooms rooms;
	private GameRoom room;

	public ServerThread(Socket socket, Rooms rooms) throws IOException {
		this.socket = socket;
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintWriter(socket.getOutputStream(), true);
		ServerThread.rooms = rooms;
		ServerThread.roomHashTable = rooms.getRooms();
	}

	public void sendMessageToClient(String msg) {
		writer.println(msg);
		writer.flush();
	}

	public void createRoom(String roomName) {
		Player player = new Player(name, 0);
		GameRoom room = new GameRoom(roomName);
		this.room = room;
		room.addPlayer(player, this.writer);
		roomHashTable.put(roomName, room);
		sendMessageToClient("You added a game named " + roomName);
		this.roomID = roomName;
		this.playerID = 0;
	}

	public void joinRoom(String roomName) throws RoomNotFoundException {
		Player player = new Player(name, 1);
		GameRoom room = roomHashTable.get(roomName);
		if (room == null) {
			throw new RoomNotFoundException();
		}
		room.addPlayer(player, this.writer);
		sendMessageToClient("You joined a game named " + roomName);
		this.roomID = roomName;
		this.playerID = 1;
		this.room = room;
	}

	public void joinRandomGame() throws RoomNotFoundException {
		List<String> freeRooms = new ArrayList<>();
		synchronized (rooms) {
			freeRooms = rooms.getAllFreeRooms();
		}
		if (freeRooms.get(0) != null) {
			joinRoom(freeRooms.get(0));
		} else {
			sendMessageToClient("There are n o free rooms at the moment.");
		}
	}

	public void checkField(String field) throws InvalidFieldPositionException {
		int x = Character.getNumericValue(field.charAt(0));
		int y = Character.getNumericValue(field.charAt(1));
		if (Player.isValidPosition(x, y)) {
			room.hitField(x, y, this.playerID);
			room.sendBoards();
		} else {
			throw new InvalidFieldPositionException();
		}
	}

	public int getOtherPlayersId(int yourId) {
		return (yourId + 1) % 2;
	}

	public void saveGame() throws IOException {
		room.saveGame(roomID);
	}

	public void loadGame(String roomID) throws IOException {
		try {
			synchronized (rooms) {
				rooms.loadGame(roomID, name, writer);
			}

			this.roomID = roomID;
			room = roomHashTable.get(roomID);
			sendMessageToClient("Game Loaded");
		} catch (GameNotFoundException e) {
			e.printStackTrace();
			sendMessageToClient("There is no such game.");
		}
	}

	public void getAllRoomHashTable() {
		String header = "|\tNAME\t|\tCREATOR\t|\tSTATUS\t|\tPLAYERS\t|";
		sendMessageToClient(header);
		sendMessageToClient("------------------------------------------------------------------------------");
		List<String> roomInfoArray = new ArrayList<String>();
		synchronized (rooms) {
			roomInfoArray = rooms.getAllRoomsInfo();
		}
		roomInfoArray.forEach(a -> writer.println(a));
		writer.flush();
	}

	public void setPlayerName() throws IOException {
		String name = reader.readLine();
		this.name = name;
		sendMessageToClient("Hello " + this.name);
	}

	public void setShipPositions() throws IOException {
		setShipPosition(5);
		setShipPosition(4);
		setShipPosition(4);
		for (int i = 0; i < 3; i++) {
			setShipPosition(3);
		}
		for (int i = 0; i < 4; i++) {
			setShipPosition(2);
		}

	}

	public void setShipPosition(int shipSize) throws IOException {
		sendMessageToClient("insert the coordinates of the shis with size " + shipSize);
		char[] coordinates = new char[3];
		coordinates = reader.readLine().toCharArray();
		try {
			int x = Character.getNumericValue(coordinates[0]);
			int y = Character.getNumericValue(coordinates[1]);
			char orientation = coordinates[2];

			Player player = room.getPlayers().get(this.playerID);
			while (!player.canPutShip(shipSize, x, y, orientation)) {
				sendMessageToClient("You can't put ship on that field.");
				sendMessageToClient("insert the coordinates of the shis with size " + shipSize);
				coordinates = reader.readLine().toCharArray();
				x = Character.getNumericValue(coordinates[0]);
				y = Character.getNumericValue(coordinates[1]);
				orientation = coordinates[2];
			}
			player.addShip(shipSize, x, y, orientation);
			room.SendYourBoardToPlayer(this.playerID);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}

	public void deleteSavedGame(String gameName) {
		try {
			synchronized (rooms) {
				rooms.deleteSavedGame(gameName, name);
			}
			sendMessageToClient("You deleted the game " + gameName + " successfully.");
		} catch (GameNotFoundException e) {
			sendMessageToClient("You don't have any saved game with this name. "
					+ "Note that you can only delete games that you participated in.");
			e.printStackTrace();
		}
	}

	public void exitGame() throws IOException {
		room.sendExitGameMessage(this.name);
		room.saveGame(this.roomID);
		synchronized (rooms) {
			rooms.exitGame(this.roomID);
		}
	}

	public void getYourSavedGames() {
		List<String> listOfYourGames = new ArrayList<>();
		listOfYourGames = rooms.getAllSavedGamesOfPlayer(this.name);
		if (listOfYourGames.size() == 0) {
			writer.println("You don't have saved games.");
		} else {
			writer.println("Your games are:");
			listOfYourGames.forEach(a -> writer.println(a));
		}
		writer.flush();
	}

	public void printAllCommands() {
		writer.println("create-game <GameName> - creates a new room ");
		writer.println("join-game <GameName> - join room ");
		writer.println("join-random-game - join random room if there is any avaible");
		writer.println("set-ships <pos pos direction> - puts your ships in the field. Example: 11h 88v 55h");
		writer.println("hit <pos pos> - use this command to hit an opponent's field");
		writer.println("save-game - saves the game");
		writer.println("load-game <GameName> - loads a game");
		writer.println("list-rooms - prints a list of all available rooms");
		writer.println("get-stat - print player statistics");
		writer.println("exit-game - you exit the current game. The game is automatically saved before you leave it.");
		writer.println("saved-games - see all saved games");
		writer.println("delete-game <GameName>- deletes GameName");
		writer.flush();
	}

	public void getStatistics() {
		List<String> statistics = new ArrayList<>();
		statistics = new Player(this.name,0).getStatistic();
		statistics.forEach(a -> sendMessageToClient(a));
	}

	public void run() {
		try {
			setPlayerName();
			while (true) {
				if (room != null) {
					if (room.gameIsOver()) {
						room.addStatistic();
					}
				}
				String input = reader.readLine();
				if (input.equals("create-game")) {
					sendMessageToClient("Enter room name");
					String roomName = reader.readLine();
					createRoom(roomName);
				} else if (input.equals("set-ships")) {
					setShipPositions();
					sendMessageToClient("Ships are set");
				} else if (input.equals("join-game")) {
					sendMessageToClient("Enter room name");
					String roomName = reader.readLine();
					try {
						joinRoom(roomName);
					} catch (RoomNotFoundException e) {
						e.printStackTrace();
					}
				} else if (input.equals("join-random-game")) {
					try {
						joinRandomGame();
					} catch (RoomNotFoundException e) {
						e.printStackTrace();
					}
				} else if (input.equals("save-game")) {
					saveGame();
					sendMessageToClient("Game Saved");
				} else if (input.equals("load-game")) {
					String gameName = reader.readLine();
					loadGame(gameName);
				} else if (input.equals("list-rooms")) {
					getAllRoomHashTable();
				} else if (input.equals("hit")) {
					String field = reader.readLine();
					//System.out.println(field);
					try {
						checkField(field);
					} catch (InvalidFieldPositionException e) {
						sendMessageToClient("The position you have entered is invalid.");
						e.printStackTrace();
					} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
						sendMessageToClient("The position you have entered is invalid.");
						e.printStackTrace();
					}
				} else if (input.equals("exit-game")) {
					exitGame();
				} else if (input.equals("saved-games")) {
					getYourSavedGames();
				} else if (input.equals("delete-game")) {
					String savedGame = reader.readLine();
					deleteSavedGame(savedGame);
				} else if (input.equals("get-stat")) {
					getStatistics();

				} else {
					sendMessageToClient("Invalid Command");
					printAllCommands();
				}
			}
		} catch (IOException e) {
			System.out.println(e);
		} finally {
			try {
				socket.close();
				writer.close();
				reader.close();
			} catch (IOException e) {
			}
		}
	}
}
