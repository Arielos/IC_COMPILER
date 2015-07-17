package IC.SemanticAnalysis;

public class MethodSymbolTable extends SymbolTable {

	public MethodSymbolTable(String id) {
		super(id, Kind.METHOD);
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Method Symbol Table: " + getId() + "\n");
		output.append(super.toString());
		
		if (getChildList().size() > 0) {
			output.append("\n");
			for (SymbolTable child : getChildList()) {
					output.append(child);
			}
		}
		
		return output.toString();
	}
}
