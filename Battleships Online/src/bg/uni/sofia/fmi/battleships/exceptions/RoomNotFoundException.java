package bg.uni.sofia.fmi.battleships.exceptions;

public class RoomNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public RoomNotFoundException() {
		super();
	}

	public RoomNotFoundException(String message) {
		super(message);
	}
}
