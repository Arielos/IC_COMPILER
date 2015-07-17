package IC.Parser;

public class LexicalError extends Exception {
	private static final long serialVersionUID = 1L;

	public LexicalError(String message) {
		super(message);
	}

	public LexicalError(String message, int line) {
		this(message + " in line " + (line + 1));
	}

	public LexicalError(String message, int line, int column) {
		this(message + " in line " + (line + 1) + ", column " + column);
	}
}