package bg.uni.sofia.fmi.battleships.game.components;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GameRoom {
	private List<Player> players;
	private List<PrintWriter> writers;
	public final static int boardSize = 10;
	private String roomID;

	public GameRoom(String roomID) {
		players = new ArrayList<Player>();
		writers = new ArrayList<PrintWriter>();
		this.roomID = roomID;
	}

	public void addPlayer(Player player, PrintWriter writer) {
		players.add(player);
		writers.add(writer);
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public List<PrintWriter> getWriters() {
		return writers;
	}

	public void setWriters(List<PrintWriter> writers) {
		this.writers = writers;
	}

	public void saveGame(String roomID) throws IOException {
		Player player1 = players.get(0);
		Player player2 = players.get(1);
		File file = new File(GameSettings.savedGamesDirectory + "/" + roomID + " " + player1.getPlayerName() + " "
				+ player2.getPlayerName() + ".txt");
		PrintWriter pw = new PrintWriter(new FileWriter(file.toString()));

		char[][][] boards = new char[4][10][10];

		boards[0] = player1.getBoard();
		boards[1] = player1.getEnemyBoard();
		boards[2] = player2.getBoard();
		boards[3] = player2.getEnemyBoard();

		pw.println(player1.getPlayerName());
		pw.println(player1.getEnemyFieldsNotSinked());
		pw.println(player1.getTurn());

		for (int i = 0; i < boardSize; i++) {
			pw.println(boards[0][i]);
		}
		for (int i = 0; i < boardSize; i++) {
			pw.println(boards[1][i]);
		}

		pw.println(players.get(1).getPlayerName());
		pw.println(player2.getEnemyFieldsNotSinked());
		pw.println(player2.getTurn());
		for (int i = 0; i < boardSize; i++) {
			pw.println(boards[2][i]);
		}
		for (int i = 0; i < boardSize; i++) {
			pw.println(boards[3][i]);
		}
		pw.close();
	}

	public void loadPlayers(String player, File file, String roomID) throws IOException {
		this.roomID = roomID;
		BufferedReader br = new BufferedReader(new FileReader(file.toString()));
		Player[] players = new Player[2];
		for (int i = 0; i < 2; i++) {
			players[i] = new Player("name", i);

			String playerName = br.readLine();
			players[i].setPlayerName(playerName);

			if (playerName.equals(player)) {
				players[i].setPlayerID(0);
			} else {
				players[i].setPlayerID(1);
			}

			String enemyFieldsNotSinked = br.readLine();
			players[i].setEnemyFieldsNotSinked(Integer.parseInt(enemyFieldsNotSinked));

			String turn = br.readLine();
			if (turn.equals("true")) {
				players[i].setTurn(true);
			} else {
				players[i].setTurn(false);
			}
			char[][][] board = new char[2][GameRoom.boardSize][GameRoom.boardSize];
			for (int j = 0; j < 2; j++) {
				for (int m = 0; m < GameRoom.boardSize; m++) {
					board[j][m] = br.readLine().toCharArray();
				}
			}
			players[i].setBoard(board[0]);
			players[i].setEnemyBoard(board[1]);
			this.players.add(players[i]);
		}
		br.close();
	}

	public int getNumberOfPlayers() {
		return players.size();
	}

	public String getRoomCreatorName() {
		return players.get(0).getPlayerName();
	}

	public String getStatus() {
		int numOfPlayers = getNumberOfPlayers();
		if (numOfPlayers == 1) {
			return "pending";
		} else if (numOfPlayers == 2) {
			return "in progress";
		}
		return "";
	}

	public String getGameInfo() {
		String result = "|\t" + roomID + "\t|\t" + getRoomCreatorName() + "\t|\t" + getStatus() + "\t|\t"
				+ getNumberOfPlayers() + "/2\t\t|";
		return result;
	}

	public void sendMessageToPlayer(int i, String message) {
		PrintWriter writer = writers.get(i);
		writer.println(message);
		writer.flush();
	}

	public void shiftTurns() {
		players.forEach(Player::shiftTurn);
		if(players.get(0).getTurn() == players.get(1).getTurn())
		{
			players.get(0).setTurn(players.get(0).getTurn());
		}
		
	}

	private void sendBoardToPlayer(char[][] board, int index) {
		PrintWriter pw = writers.get(index);
		pw.println("   0 1 2 3 4 5 6 7 8 9");
		pw.println("______________________");
		for (int i = 0; i < GameRoom.boardSize; i++) {
			pw.print(i + " ");
			for (int j = 0; j < GameRoom.boardSize; j++) {
				pw.print("|" + board[i][j]);
			}
			pw.println("|");
		}
		pw.flush();
	}

	public void SendYourBoardToPlayer(int index) {
		sendBoardToPlayer(players.get(index).getBoard(), index);
	}

	public void sendBoards() {
		for (int i = 0; i < 2; i++) {
			Player player = players.get(i);
			sendMessageToPlayer(i, "Your board");
			sendBoardToPlayer(player.getBoard(), i);
			sendMessageToPlayer(i, "");
			sendMessageToPlayer(i, "Enemy board");
			sendBoardToPlayer(player.getEnemyBoard(), i);
		}
	}

	public int getOtherPlayersId(int yourId) {
		return (yourId + 1) % 2;
	}

	public void hitField(int x, int y, int playerID) {
		if (players.get(playerID).getTurn() == false) {

			sendMessageToPlayer(playerID, "It's not your turn.");
			sendMessageToPlayer(playerID, "Wait for your opponent to hit");
			return;
		}
		int opponentID = getOtherPlayersId(playerID);
		Player you = players.get(playerID);
		Player opponent = players.get(opponentID);

		if (opponent.isFree(x, y)) {
			you.getEnemyBoard()[x][y] = GameSettings.hitEmptyField;
			opponent.getBoard()[x][y] = GameSettings.hitEmptyField;
			sendMessageToPlayer(playerID, "The field is empty.");
		} else {
			you.getEnemyBoard()[x][y] = GameSettings.hitShipField;
			opponent.getBoard()[x][y] = GameSettings.hitShipField;
			you.decrementEnemyFieldsNotSinked();
			sendMessageToPlayer(playerID, "You hit something.");
		}
		sendMessageToPlayer(opponentID, "Your opponent hit " + x + y + " .");
		shiftTurns();
	}

	public void sendExitGameMessage(String playerName) {
		for (int i = 0; i < 2; i++) {
			sendMessageToPlayer(i, playerName + " left the game. The game will be saved automatically.");
		}

	}

	public boolean gameIsOver() {
		if (players == null || players.size() < 2) {
			return false;
		}
		Player player0 = players.get(0);
		Player player1 = players.get(1);
		if (player0 == null || player1 == null) {
			return false;
		}
		boolean player0Wins = player0.winsGame();
		boolean player1Wins = player1.winsGame();
		if (player0Wins) {
			sendMessageToPlayer(0, player0.getPlayerName() + " wins the game");
			sendMessageToPlayer(1, player0.getPlayerName() + " wins the game");
			return true;
		} else if (player1Wins) {
			sendMessageToPlayer(0, player1.getPlayerName() + " wins the game");
			sendMessageToPlayer(1, player1.getPlayerName() + " wins the game");
			return true;
		}
		return false;
	}

	public void addStatistic() throws FileNotFoundException {
		for (Player player : players) {
			File file = new File(player.getStatFile());
			PrintWriter writer = new PrintWriter(file.toString());
			writer.append(this.roomID + " ");
			if (player.winsGame()) {
				writer.append("won\n");
			} else {
				writer.append("lost\n");
			}
			writer.close();
		}
	}
}
