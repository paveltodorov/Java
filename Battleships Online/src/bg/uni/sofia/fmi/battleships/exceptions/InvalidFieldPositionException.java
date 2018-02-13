package bg.uni.sofia.fmi.battleships.exceptions;

public class InvalidFieldPositionException extends Exception {
	private static final long serialVersionUID = 1L;

	public InvalidFieldPositionException() {
		super();
	}

	public InvalidFieldPositionException(String message) {
		super(message);
	}
}
