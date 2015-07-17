package IC.SemanticAnalysis;

public class VarSymbolTable extends SymbolTable {

	public VarSymbolTable(String id) {
		super(id, Kind.VAR);
	}

	@Override
	public String toString() {
		return "";
	}

}
