package IC.Parser;

import java_cup.runtime.Symbol;

public class SyntaxError extends Exception {
	private static final long serialVersionUID = 1L;

	private int lineNumber = -1;

	public SyntaxError(String message) {
		super(message);
	}

	public SyntaxError(int line, String message) {
		super(message);
		this.lineNumber = line;
	}

	public SyntaxError(int line, Symbol sym) {
		this(line, Integer.toString(sym.sym));
	}
}
