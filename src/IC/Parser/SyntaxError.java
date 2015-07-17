package IC.Parser;

import java_cup.runtime.Symbol;

public class SyntaxError extends Exception {
	private static final long serialVersionUID = 1L;

    /**
     * The line number of the error
     */
	private int lineNumber = -1;

    /**
     * SyntaxError error constructor
     * @param message the message of the error
     */
	public SyntaxError(String message) {
		super(message);
	}

    /**
     * SyntaxError error constructor
     * @param line the line of the error
     * @param message the message of the error
     */
	public SyntaxError(int line, String message) {
		this(message);
		this.lineNumber = line;
	}

    /**
     * SyntaxError error constructor
     * @param line the line of the error
     * @param sym the symbol of the error
     */
	public SyntaxError(int line, Symbol sym) {
		this(line, Integer.toString(sym.sym));
	}

    /**
     * Line number getter method
     * @return the line number
     */
    public int getLineNumber() {
        return this.lineNumber;
    }
}
