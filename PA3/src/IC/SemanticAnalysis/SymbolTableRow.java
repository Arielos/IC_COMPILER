package IC.SemanticAnalysis;

import IC.TypeTable.Type;
import IC.TypeTable.UserDefType;

public class SymbolTableRow implements Comparable {
	private String id;
	private Type type;
	private Kind kind;

	public SymbolTableRow(String id, Type type, Kind kind) {
		this.id = id;
		this.type = type;
		this.kind = kind;
	}

	public String getId() {
		return this.id;
	}

	public Type getType() {
		return this.type;
	}

	public Kind getKind() {
		return this.kind;
	}

	@Override
	public String toString() {
		String text = "";
		
		switch (kind) {
		case PARAM:
			text = "Parameter: " + type + " " + id;
			break;
		case VAR:
			text = "Local variable: " + type + " " + id;
			break;
		case METHOD:
			text = type.getName() + " method: " + id + " {" + type + "}";
			break;
		case FIELD:
			text = "Field: " + type + " " + id;
			break;
		case CLASS:
			text = "Class: " + id;
			break;
		default:
			break;
		}
		
		return text;
	}

	@Override
	public int compareTo(Object other) {
		final int BEFORE = -1;
		final int EQUAL = 0;
		final int AFTER = 1;

		final SymbolTableRow theOther = (SymbolTableRow) other;

		if (this.kind.ordinal() < theOther.kind.ordinal())
			return BEFORE;
		if (this.kind.ordinal() > theOther.kind.ordinal())
			return AFTER;

		if (this.type.getClass().equals(UserDefType.class)
				&& !theOther.type.getClass().equals(UserDefType.class))
			return BEFORE;

		if (!this.type.getClass().equals(UserDefType.class)
				&& theOther.type.getClass().equals(UserDefType.class))
			return AFTER;

		if (this.kind == Kind.METHOD) {
			if (this.id.equals("Static") && !theOther.id.equals("Static"))
				return BEFORE;
			if (this.id.equals("Virtual") && !theOther.id.equals("Virtual"))
				return AFTER;
		}

		return this.id.compareTo(theOther.id);
	}
}
