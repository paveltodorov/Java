package bg.uni.sofia.fmi.battleships.game.components;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bg.uni.sofia.fmi.battleships.exceptions.GameNotFoundException;

public class Rooms {
	private Hashtable<String, GameRoom> rooms = new Hashtable<>();

	public Rooms() {
		setRooms(new Hashtable<>());
	}

	public Hashtable<String, GameRoom> getRooms() {
		return rooms;
	}

	public void setRooms(Hashtable<String, GameRoom> rooms) {
		this.rooms = rooms;
	}

	public List<String> getAllFreeRooms() {
		ArrayList<String> roomArray = new ArrayList<String>();
		Set<Map.Entry<String, GameRoom>> roomHashTable = new HashSet<>();
		roomHashTable = rooms.entrySet();
		for (Iterator<Map.Entry<String, GameRoom>> it = roomHashTable.iterator(); it.hasNext();) {
			Map.Entry<String, GameRoom> room = it.next();
			if (room.getValue().getNumberOfPlayers() == 1) {
				roomArray.add(room.getKey());
			}
		}
		return roomArray;
	}

	public List<String> getAllRoomsInfo() {
		ArrayList<String> roomInfoArray = new ArrayList<String>();
		Set<Map.Entry<String, GameRoom>> roomHashTable = new HashSet<>();
		roomHashTable = rooms.entrySet();
		for (Iterator<Map.Entry<String, GameRoom>> it = roomHashTable.iterator(); it.hasNext();) {
			Map.Entry<String, GameRoom> room = it.next();
			roomInfoArray.add(room.getValue().getGameInfo());
		}
		return roomInfoArray;
	}

	public void exitGame(String gameID) {
		rooms.remove(gameID);
	}

	public File[] getAllSavedGames() {
		File dirName = new File(GameSettings.savedGamesDirectory);
		File[] listOfFiles = dirName.listFiles();
		return listOfFiles;
	}

	public List<String> getAllSavedGamesOfPlayer(String playerName) {
		List<String> gamesOfPlayer = new ArrayList<>();
		File[] listOfFiles = getAllSavedGames();
		for (File file : listOfFiles) {
			String[] nameComponents = file.getName().toString().split(" ");
			// System.out.printlm(nameComponents[1] + " vdv" +nameComponents[2]);
			if (playerName.equals(nameComponents[1]) || nameComponents[2].startsWith(playerName)) {
				gamesOfPlayer.add(file.getName().toString().split(" ")[0]);
			}
		}
		return gamesOfPlayer;
	}

	public void deleteSavedGame(String gameID, String playerName) throws GameNotFoundException {
		File[] listOfSavedGames = getAllSavedGames();
		for (File game : listOfSavedGames) {
			String fileName = game.getName();
			String[] nameComponents = fileName.split(" ");
			if (nameComponents[0].equals(gameID)) {
				if (nameComponents[1].equals(playerName) || nameComponents[2].equals(playerName)) {
					game.delete();
					return;
				}
			}
		}
		throw new GameNotFoundException();
	}

	public void loadGame(String roomID, String name, PrintWriter writer) throws IOException, GameNotFoundException {
		if (!rooms.containsKey(roomID)) {
			File roomFile = null;
			File[] listOfSavedGames = getAllSavedGames();
			for (File game : listOfSavedGames) {
				String fileName = game.getName();
				String[] nameComponents = fileName.split(" ");
				if (nameComponents[0].equals(roomID)) {
					roomFile = game;
				}
			}
			if (roomFile == null) {
				throw new GameNotFoundException();
			}
			GameRoom room = new GameRoom(roomID);
			rooms.put(roomID, room);
			room.loadPlayers(name, roomFile, roomID);
			room.getWriters().add(writer);
			//room.getPlayers().get(1).setPlayerID(1);

		} else {
			GameRoom curRoom = rooms.get(roomID);
			if (curRoom == null) {
				throw new GameNotFoundException();
			}
			curRoom.getWriters().add(writer);
		}
	}
}
