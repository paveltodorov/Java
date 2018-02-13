package bg.uni.sofia.fmi.battleships.game.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {
	private String playerName;
	private int playerID;
	private char[][] board;
	private char[][] enemyBoard;
	private int enemyFieldsNotSinked = 30;
	private boolean turn;
	private String statFile;

	public Player(String playerName, int playerID) {
		this.playerName = playerName;
		this.playerID = playerID;
		board = new char[GameSettings.boardSize][GameSettings.boardSize];
		enemyBoard = new char[GameSettings.boardSize][GameSettings.boardSize];
		statFile = GameSettings.userDirectorty + "/" + playerName;
		initializeTurns();
		initializeBoards();
	}

	public void initializeBoards() {
		for (int i = 0; i < GameSettings.boardSize; i++) {
			for (int j = 0; j < GameSettings.boardSize; j++) {
				board[i][j] = GameSettings.emptyField;
				enemyBoard[i][j] = GameSettings.emptyField;
			}
		}
	}

	public void initializeTurns() {
		if (playerID == 0) {
			turn = true;
		} else {
			turn = false;
		}
	}

	public boolean isFree(int x, int y) {
		return board[x][y] == GameSettings.emptyField;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
		statFile = GameSettings.userDirectorty + "/" + playerName;
	}

	public int getPlayerID() {
		return playerID;
	}

	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}

	public char[][] getBoard() {
		return board;
	}

	public void setBoard(char[][] board) {
		this.board = board;
	}

	public char[][] getEnemyBoard() {
		return enemyBoard;
	}

	public void setEnemyBoard(char[][] enemyBoard) {
		this.enemyBoard = enemyBoard;
	}

	public static boolean isValidPosition(int x, int y) {
		return x >= 0 && y >= 0 && x < GameSettings.boardSize && y < GameSettings.boardSize;
	}

	public boolean bordersWithShip(int x, int y) {
		for (int i = 0; i < 9; i++) {
			if (isValidPosition(x + GameSettings.neighboursX[i], y + GameSettings.neighboursY[i])
					&& !isFree(x + GameSettings.neighboursX[i], y + GameSettings.neighboursY[i])) {
				return true;
			}
		}
		return false;
	}

	public boolean canPutShip(int size, int x, int y, char orientation) {
		if (!isValidPosition(x, y)) {
			return false;
		}
		if (orientation == 'h') {
			if (!isValidPosition(x, y + size - 1)) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (bordersWithShip(x, y + i)) {
					return false;
				}
			}
		} else if (orientation == 'v') {
			if (!isValidPosition(x + size - 1, y)) {
				return false;
			}
			for (int i = 0; i < size; i++) {
				if (bordersWithShip(x + i, y)) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public void addShip(int size, int x, int y, char orientation) {
		if (orientation == 'h') {
			for (int i = 0; i < size; i++) {
				board[x][y + i] = GameSettings.shipField;
			}
		} else if (orientation == 'v') {
			for (int i = 0; i < size; i++) {
				board[x + i][y] = GameSettings.shipField;
			}
		}
	}

	public void shiftTurn() {
		turn = !turn;
	}

	public boolean getTurn() {
		return turn;
	}

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public int getEnemyFieldsNotSinked() {
		return enemyFieldsNotSinked;
	}

	public void setEnemyFieldsNotSinked(int enemyFieldsNotSinked) {
		this.enemyFieldsNotSinked = enemyFieldsNotSinked;
	}

	public void decrementEnemyFieldsNotSinked() {
		this.enemyFieldsNotSinked--;
	}

	public boolean winsGame() {
		return enemyFieldsNotSinked == 0;
	}

	public String getStatFile() {
		return statFile;
	}

	public void setStatFile(String statFile) {
		this.statFile = statFile;
	}

	public List<String> getStatistic() {
		File file = new File(statFile);
		try (BufferedReader reader = new BufferedReader(new FileReader(file.toString()))) {
			List<String> statistics = new ArrayList<>();
			statistics = reader.lines().collect(Collectors.toList());
			return statistics;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
