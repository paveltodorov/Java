package bg.uni.sofia.fmi.bettleships.game.components;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bg.uni.sofia.fmi.battleships.game.components.GameRoom;
import bg.uni.sofia.fmi.battleships.game.components.GameSettings;
import bg.uni.sofia.fmi.battleships.game.components.Player;

public class GameRoomTest {
	private static GameRoom room;
	private static Player[] players = new Player[2];
	private static File[] files = new File[2];
	private static PrintWriter writers[] = new PrintWriter[2];
	private static BufferedReader readers[] = new BufferedReader[2];
	private static String roomID = "room";
	private static int boardSize = 10;

	@Before
	public void before() throws FileNotFoundException {
		room = new GameRoom(roomID);
		for (int i = 0; i < 2; i++) {
			GameRoomTest.players[i] = new Player("player" + i, i);
			GameRoomTest.files[i] = new File("file" + i);
			GameRoomTest.writers[i] = new PrintWriter(files[i].toString());
			GameRoomTest.readers[i] = new BufferedReader(new FileReader(files[i].toString()));
		}
		GameRoomTest.room = createRoom(players, writers);
	}

	public static GameRoom createRoom(Player[] players, PrintWriter[] writers) {
		GameRoom room = new GameRoom("NewRoom");
		for (int i = 0; i < 2; i++) {
			room.addPlayer(players[i], writers[i]);
		}
		players[0].addShip(5, 1, 1, 'h');
		players[0].addShip(4, 6, 0, 'v');
		players[0].addShip(4, 3, 0, 'h');
		players[0].addShip(3, 9, 5, 'h');
		players[0].addShip(3, 7, 2, 'v');
		players[0].addShip(3, 5, 2, 'h');
		players[0].addShip(2, 0, 7, 'h');
		players[0].addShip(2, 4, 7, 'h');
		players[0].addShip(2, 7, 5, 'h');
		players[0].addShip(2, 6, 9, 'v');

		players[1].addShip(5, 0, 0, 'v');
		players[1].addShip(4, 6, 0, 'v');
		players[1].addShip(4, 4, 2, 'h');
		players[1].addShip(3, 1, 7, 'v');
		players[1].addShip(3, 1, 2, 'h');
		players[1].addShip(3, 8, 2, 'h');
		players[1].addShip(2, 6, 3, 'h');
		players[1].addShip(2, 6, 6, 'v');
		players[1].addShip(2, 9, 7, 'h');
		players[1].addShip(2, 5, 8, 'v');
		return room;
	}

	@After
	public void after() throws IOException {
		for (int i = 0; i < 2; i++) {
			readers[i].close();
			writers[i].close();
			files[i].delete();
		}
	}

	@Test
	public void SendYourBoardToPlayerTest() throws IOException {
		room.SendYourBoardToPlayer(0);
		room.SendYourBoardToPlayer(1);
		for (int i = 0; i < 2; i++) {
			readers[i].readLine();
			readers[i].readLine();
		}
		assertEquals("0 |_|_|_|_|_|_|_|#|#|_|", readers[0].readLine());
		assertEquals("1 |_|#|#|#|#|#|_|_|_|_|", readers[0].readLine());
		assertEquals("2 |_|_|_|_|_|_|_|_|_|_|", readers[0].readLine());

		assertEquals("0 |#|_|_|_|_|_|_|_|_|_|", readers[1].readLine());
		assertEquals("1 |#|_|#|#|#|_|_|#|_|_|", readers[1].readLine());
	}

	@Test
	public void hitFieldTest() throws IOException {
		room.hitField(0, 0, 0);
		assertEquals(players[0].getEnemyBoard()[0][0], 'X');
		assertEquals(players[1].getBoard()[0][0], 'X');
		assertEquals("Your opponent hit 00 .", readers[1].readLine());

		room.hitField(6, 5, 1);
		assertEquals(players[1].getEnemyBoard()[6][5], 'O');
		assertEquals(players[0].getBoard()[6][5], 'O');
		assertEquals("The field is empty.", readers[1].readLine());

		room.hitField(8, 8, 1);
		assertEquals("when it's not your turn, you can not hit ", "It's not your turn.", readers[1].readLine());
	}

	@Test
	public void saveGameTest() throws IOException {
		room.saveGame(roomID);
		BufferedReader saveFile = new BufferedReader(
				new FileReader(GameSettings.savedGamesDirectory + "/room player0 player1.txt"));
		assertEquals("player0", saveFile.readLine());
		assertEquals("30", saveFile.readLine());
		assertEquals("true", saveFile.readLine());
		assertEquals("_______##_", saveFile.readLine());
		saveFile.close();
	}

	@Test
	public void loadPlayersTest() throws IOException {
		room.saveGame(roomID);
		File savedRoomFile = new File(GameSettings.savedGamesDirectory + "/room player0 player1.txt");
		GameRoom room2 = new GameRoom("room1");
		room2.loadPlayers("player1", savedRoomFile, "room");
		for (int m = 0; m < 2; m++) {
			for (int i = 0; i < boardSize; i++) {
				for (int j = 0; j < boardSize; j++) {
					assertEquals(room2.getPlayers().get(m).getBoard()[i][j], players[m].getBoard()[i][j]);
					assertEquals(room2.getPlayers().get(m).getEnemyBoard()[i][j], players[m].getEnemyBoard()[i][j]);
				}
			}
		}
	}

	@Test
	public void gameIsOverTest() throws IOException {
		assertFalse(room.gameIsOver());
		GameRoom room2 = new GameRoom("room1");
		assertFalse(room2.gameIsOver());
		room2.addPlayer(new Player("name", 0), null);
		assertFalse(room2.gameIsOver());
		room.getPlayers().get(0).setEnemyFieldsNotSinked(0);
		assertTrue(room.gameIsOver());
		assertEquals("player0 wins the game", readers[0].readLine());
		assertEquals("player0 wins the game", readers[1].readLine());
		room.getPlayers().get(0).setEnemyFieldsNotSinked(20);
		room.getPlayers().get(1).setEnemyFieldsNotSinked(0);
		assertTrue(room.gameIsOver());
		assertEquals("player1 wins the game", readers[0].readLine());
		assertEquals("player1 wins the game", readers[1].readLine());
	}

	@Test
	public void sendMessageToPlayerTest() throws IOException {
		room.sendMessageToPlayer(0, "hi");
		assertEquals("hi", readers[0].readLine());
		room.sendMessageToPlayer(1, "hola");
		assertEquals("hola", readers[1].readLine());
	}

	@Test
	public void getRoomStatusTest() {
		assertEquals("in progress", room.getStatus());
		GameRoom room1 = new GameRoom("room2");
		room1.addPlayer(new Player("player0", 0), null);
		assertEquals("pending", room1.getStatus());
	}

	@Test
	public void getRoomInfoTest() {
		assertEquals("|	NewRoom	|	player0	|	in progress	|	2/2		|", room.getGameInfo());
	}

	@Test
	public void addStatisticsTest() throws IOException {
		players[0].setEnemyFieldsNotSinked(0);
		room.addStatistic();
		File file = new File("Users/player0");
		File file1 = new File("Users/player1");
		BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
		BufferedReader reader1 = new BufferedReader(new FileReader(file1.toString()));
		assertEquals("NewRoom won", reader.readLine());
		assertEquals("NewRoom lost", reader1.readLine());
		List<String> stat = new ArrayList<>();
		stat = players[0].getStatistic();
		assertEquals("NewRoom won", stat.get(0));
		players[0].setEnemyFieldsNotSinked(30);
		file.delete();
		file1.delete();
		reader.close();
		reader1.close();
	}
	/*
	 * 0 1 2 3 4 5 6 7 8 9 ______________________ 0 |_|_|_|_|_|_|_|#|#|_| 1
	 * |_|#|#|#|#|#|_|_|_|_| 2 |_|_|_|_|_|_|_|_|_|_| 3 |#|#|#|#|_|_|_|_|_|_| 4
	 * |_|_|_|_|_|_|_|#|#|_| 5 |_|_|#|#|#|_|_|_|_|_| 6 |#|_|_|_|_|_|_|_|_|#| 7
	 * |#|_|#|_|_|#|#|_|_|#| 8 |#|_|#|_|_|_|_|_|_|_| 9 |#|_|#|_|_|#|#|#|_|_|
	 * 
	 * 0 1 2 3 4 5 6 7 8 9 ______________________ 0 |#|_|_|_|_|_|_|_|_|_| 1
	 * |#|_|#|#|#|_|_|#|_|_| 2 |#|_|_|_|_|_|_|#|_|_| 3 |#|_|_|_|_|_|_|#|_|_| 4
	 * |#|_|#|#|#|#|_|_|_|_| 5 |_|_|_|_|_|_|_|_|#|_| 6 |#|_|_|#|#|_|#|_|#|_| 7
	 * |#|_|_|_|_|_|#|_|_|_| 8 |#|_|#|#|#|_|_|_|_|_| 9 |#|_|_|_|_|_|_|#|#|_|
	 */
}
