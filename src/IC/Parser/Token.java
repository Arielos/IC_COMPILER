package IC.Parser;

public class Token extends java_cup.runtime.Symbol {

	private String name;
	private int lineNumber;
	private int columnNumber;

	public Token(int id, Object value, int line, int column) {
		this(id, null, value, line, column);
	}

	public Token(int id, String name, Object value, int line, int column) {
		super(id, line + 1, column + 1, value);

		this.name = name;
		this.lineNumber = line;
		this.columnNumber = column;
	}

	public int getId() {
		return sym;
	}

	public String getName() {
		return this.name;
	}

	public Object getValue() {
		return value;
	}

	public int getLine() {
		return this.lineNumber;
	}

	public int getColumn() {
		return this.columnNumber;
	}

	@Override
	public String toString() {
		String val = "";

		if (name != null) {
			val = value + "\t" + name;
		} else {
			val = value + "\t" + value;
		}

		return val + "\t" + lineNumber + ":" + columnNumber;
	}
}