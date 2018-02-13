package bg.uni.sofia.fmi.battleships.game.components;

public class GameSettings {
	public static final int boardSize = 10;
	public static final int neighboursX[] = { -1, 0, 1, -1, 0, 1, -1, 0, 1 };
	public static final int neighboursY[] = { -1, -1, -1, 0, 0, 0, 1, 1, 1 };
	public final static char emptyField = '_';
	public final static char shipField = '#';
	public final static char hitEmptyField = 'O';
	public final static char hitShipField = 'X';
	public final static String userDirectorty = "Users";
	public final static String savedGamesDirectory= "SavedGames";
}
