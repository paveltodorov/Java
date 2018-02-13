package bg.uni.sofia.fmi.battleships.exceptions;

public class GameNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public GameNotFoundException() {
		super();
	}

	public GameNotFoundException(String message) {
		super(message);
	}
}
