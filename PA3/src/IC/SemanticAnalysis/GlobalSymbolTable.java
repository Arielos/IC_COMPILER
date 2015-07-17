package IC.SemanticAnalysis;

public class GlobalSymbolTable extends SymbolTable {

	public GlobalSymbolTable(String id) {
		super(id, null);
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Global Symbol Table: " + getId() + "\n");
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
