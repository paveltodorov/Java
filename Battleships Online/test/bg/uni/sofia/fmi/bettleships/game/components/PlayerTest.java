package bg.uni.sofia.fmi.bettleships.game.components;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import bg.uni.sofia.fmi.battleships.game.components.Player;

public class PlayerTest {
	private static Player player;
	private static int boardSize = 10;

	@BeforeClass
	public static void beforeClass() {
		player = new Player("player0", 0);
		player.addShip(5, 1, 1, 'h');
		player.addShip(4, 6, 0, 'v');
		player.addShip(4, 3, 0, 'h');
		player.addShip(3, 9, 5, 'h');
		player.addShip(3, 7, 2, 'v');
		player.addShip(3, 5, 2, 'h');
		player.addShip(2, 0, 7, 'h');
		player.addShip(2, 4, 7, 'h');
		player.addShip(2, 7, 5, 'h');
		player.addShip(2, 6, 9, 'v');
	}

	@Test
	public void isFreeTest() {
		assertTrue("Field 22 is empty", player.isFree(2, 2));
		assertTrue("Field 81 is empty", player.isFree(8, 1));
		assertTrue("Field 64 is empty", player.isFree(6, 4));
		assertTrue("Field 26 is empty", player.isFree(2, 6));
		assertTrue("Field 46 is empty", player.isFree(4, 6));

		assertFalse("Field 11 is not empty", player.isFree(1, 1));
		assertFalse("Field 95 is not empty", player.isFree(9, 5));
		assertFalse("Field 14 is not empty", player.isFree(1, 4));
		assertFalse("Field 60 is not empty", player.isFree(6, 0));
		assertFalse("Field 69 is not empty", player.isFree(6, 9));
	}

	@Test
	public void addShipTest() { // test that all ships in the before class are put corectlly
		char[][] board = new char[boardSize][boardSize];
		board[0] = "_______##_".toCharArray();
		board[1] = "_#####____".toCharArray();
		board[2] = "__________".toCharArray();
		board[3] = "####______".toCharArray();
		board[4] = "_______##_".toCharArray();
		board[5] = "__###_____".toCharArray();
		board[6] = "#________#".toCharArray();
		board[7] = "#_#__##__#".toCharArray();
		board[8] = "#_#_______".toCharArray();
		board[9] = "#_#__###__".toCharArray();
		for (int i = 0; i < boardSize; i++)
			for (int j = 0; j < boardSize; j++) {
				assertEquals("Asserts that all ships in the before class are put corectlly", board[i][j],
						player.getBoard()[i][j]);
			}
	}

	@Test
	public void isValidPositionTest() {
		assertTrue("Fields within board are valid", Player.isValidPosition(boardSize / 5, boardSize / 3));
		assertFalse("Fields outside board are invalid", Player.isValidPosition(boardSize, boardSize / 5));
		assertFalse("Fields outside board are invalid", Player.isValidPosition(boardSize / 7, boardSize));
	}

	@Test
	public void shiftTurnTest() {
		assertTrue("The game creator starts first", player.getTurn());
		player.shiftTurn();
		assertFalse("When the turn shifts for the first time getTurn should return false", player.getTurn());
		player.shiftTurn();
		assertTrue("When the turn shifts for the second time getTurn should return true", player.getTurn());
	}

	@Test
	public void borderWithShipTest() {
		assertTrue("Field 00 borders with ship", player.bordersWithShip(0, 0));
		assertTrue("Field 76 borders with ship", player.bordersWithShip(7, 6));
		assertTrue("Field 90 borders with ship", player.bordersWithShip(9, 0));

		assertFalse("Field 99 borders with ship", player.bordersWithShip(9, 9));
		assertFalse("Field 35 borders with ship", player.bordersWithShip(3, 5));
		assertFalse("Field 29 borders with ship", player.bordersWithShip(2, 9));
	}

	@Test
	public void canPutShipTest() {
		assertTrue("You can put a 2-field ship on 27h", player.canPutShip(2, 2, 7, 'h'));

		assertFalse("You can't put a 2-field ship on 35h", player.canPutShip(2, 3, 5, 'h'));
		assertFalse("You can't put a 2-field ship on 00v", player.canPutShip(2, 0, 0, 'v'));
		assertFalse("You can't put a 3-field ship on 61v", player.canPutShip(3, 6, 1, 'v'));
		assertFalse("You can't put a 2-field ship on -10v", player.canPutShip(2, -1, 0, 'h'));
		assertFalse("You can't put a 3-field ship on 310v", player.canPutShip(3, 6, 10, 'v'));
		assertFalse("You can't put a 4-field ship on 99h", player.canPutShip(4, 9, 9, 'h'));
		assertFalse("You can't put a 3-field ship on 510v", player.canPutShip(3, 5, 10, 'v'));
		assertFalse("You can't put a 3-field ship on -10v", player.canPutShip(3, -1, 0, 'v'));
		assertFalse("You can't put a 3-field ship on 99v", player.canPutShip(3, 9, 9, 'v'));
		assertFalse("You can't put a 3-field ship on 27v", player.canPutShip(3, 2, 7, 'v'));
		assertFalse("You can't put a 2-field ship on 27v", player.canPutShip(2, 2, 7, 'v'));
		assertFalse("You can't put a 2-field ship on 33m", player.canPutShip(2, 3, 3, 'm'));
	}

	@Test
	public void winsGameTest() {
		assertFalse("At the begining nobody has won the game.", player.winsGame());
		player.setEnemyFieldsNotSinked(0);
		assertTrue("The game is over once you sink all enemy ships", player.winsGame());
		player.setEnemyFieldsNotSinked(30);
	}
}
