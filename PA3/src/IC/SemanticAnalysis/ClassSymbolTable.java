package IC.SemanticAnalysis;

public class ClassSymbolTable extends SymbolTable {

	public ClassSymbolTable(String id) {
		super(id, Kind.CLASS);
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Class Symbol Table: " + getId() + "\n");
		output.append(super.toString());
		
		if (getChildList().size() > 0) {
			output.append("\n");
			for (SymbolTable child : getChildList()) {
				output.append(child);	// line break between methods
			}
		}
		
		return output.toString();
	}

}
