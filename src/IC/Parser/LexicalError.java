package IC.Parser;

public class LexicalError extends Exception {
	private static final long serialVersionUID = 1L;

    /**
     * LexicalError error constructor
     * @param message the message of the error
     */
	public LexicalError(String message) {
		super(message);
	}

    /**
     * LexicalError error constructor
     * @param message the message of the error
     * @param line the line of the error
     */
	public LexicalError(String message, int line) {
		this(message + " in line " + (line + 1));
	}

    /**
     * LexicalError error constructor
     * @param message the message of the error
     * @param line the line of the error
     * @param column the column of the error
     */
	public LexicalError(String message, int line, int column) {
		this(message + " in line " + (line + 1) + ", column " + column);
	}
}