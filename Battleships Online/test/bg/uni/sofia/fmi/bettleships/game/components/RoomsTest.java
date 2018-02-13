package bg.uni.sofia.fmi.bettleships.game.components;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import bg.uni.sofia.fmi.battleships.exceptions.GameNotFoundException;
import bg.uni.sofia.fmi.battleships.game.components.GameRoom;
import bg.uni.sofia.fmi.battleships.game.components.Player;
import bg.uni.sofia.fmi.battleships.game.components.Rooms;

public class RoomsTest {
	private static Rooms rooms;
	private static Player[] players = new Player[2];
	private static File[] files = new File[2];
	private static PrintWriter writers[] = new PrintWriter[2];

	@Before
	public void before() throws FileNotFoundException {
		rooms = new Rooms();
		GameRoom[] r = new GameRoom[3];
		Player[] p = new Player[3];
		for (int i = 0; i < 3; i++) {
			r[i] = new GameRoom("room" + i);
			rooms.getRooms().put("room" + i, r[i]);
			p[i] = new Player("player" + i, 0);
			r[i].addPlayer(p[i], null);
		}
		r[0].addPlayer(new Player("player0", 1), null);

		for (int i = 0; i < 2; i++) {
			players[i] = new Player("player" + i, i);
			files[i] = new File("file" + i);
			writers[i] = new PrintWriter(files[i].toString());
		}
		GameRoom room = GameRoomTest.createRoom(players, writers);
		rooms.getRooms().put("NewRoom", room);
	}

	@After
	public void after() {
		for (int i = 0; i < 2; i++) {
			files[i].delete();
			writers[i].close();
		}
	}

	@Test
	public void getAllFreeRoomsTest() {
		List<String> freeRooms = new ArrayList<>();
		freeRooms = rooms.getAllFreeRooms();
		assertTrue("room2 is free", freeRooms.contains("room2"));
		assertTrue("room1 is free", freeRooms.contains("room1"));
		assertFalse("room0 is not free", freeRooms.contains("room0"));
	}

	@Test
	public void loadGameTest() throws IOException, GameNotFoundException {
		rooms.getRooms().get("NewRoom").saveGame("NewRoom");
		rooms.exitGame("NewRoom");
		rooms.loadGame("NewRoom", "player1", writers[1]);
		assertTrue(rooms.getRooms().containsKey("NewRoom"));
		GameRoom room = rooms.getRooms().get("NewRoom");
		Player player0 = room.getPlayers().get(0);

		assertNotNull("Player1's writer was initialized", rooms.getRooms().get("NewRoom").getWriters().get(0));
		assertEquals("player0", player0.getPlayerName());
		assertEquals(30, player0.getEnemyFieldsNotSinked());
		assertTrue(player0.getTurn());
		assertEquals(true, player0.getTurn());
		for (int i = 0; i < 10; i++) {
			assertEquals("_______##_".charAt(i), player0.getBoard()[0][i]);
		}

		rooms.loadGame("NewRoom", "player0", writers[0]);
		assertNotNull("Player0's writer was initialized", rooms.getRooms().get("NewRoom").getWriters().get(1));
		rooms.deleteSavedGame("NewRoom", "player0");
	}

	@Test
	public void getAllRoomsInfoTest()
	{
		List<String> roomsInfo = new ArrayList<String>();
		roomsInfo = rooms.getAllRoomsInfo();
		assertTrue("roomsInfo contains NewRoom",roomsInfo.contains("|	NewRoom	|	player0	|	in progress	|	2/2		|"));
		assertTrue("roomsInfo contains room2",roomsInfo.contains("|	room2	|	player2	|	pending	|	1/2		|"));
		assertTrue("roomsInfo contains room1",roomsInfo.contains("|	room1	|	player1	|	pending	|	1/2		|"));
	}
	
	@Test
	public void getAllSavedGamesOfPlayer() throws IOException, GameNotFoundException
	{
		rooms.getRooms().get("NewRoom").saveGame("NewRoom");
		List<String> roomsInfo = new ArrayList<String>();
		roomsInfo = rooms.getAllSavedGamesOfPlayer("player0");
		assertTrue("roomsInfo contains NewRoom",roomsInfo.contains("NewRoom"));
		
		rooms.deleteSavedGame("NewRoom", "player0");
	}
	
	@Test(expected = GameNotFoundException.class)
	public void loadNonExistingGameTest() throws IOException, GameNotFoundException {
		rooms.loadGame("Not valid room", "player1", null);
	}

	@Test(expected = GameNotFoundException.class)
	public void deleteNonExistingGameTest() throws IOException, GameNotFoundException {
		rooms.deleteSavedGame("Not valid room", "player1");
	}

}
