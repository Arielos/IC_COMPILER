package IC.SemanticAnalysis;

import IC.TypeTable.MethodType;
import IC.TypeTable.Type;

public class SymbolTableRow {
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
			String virtualOrStatic = ((MethodType)type).isStatic() ? "Static" : "Virtual";
			text = virtualOrStatic + " method: " + id + " {" + type + "}";
			break;
		case FIELD:
			text = "Field: " + type + " " + id;
			break;
		case CLASS:
			text = "Class: " + id;
			break;
		default:
			text = id;
			break;
		}
		
		return text;
	}
}
