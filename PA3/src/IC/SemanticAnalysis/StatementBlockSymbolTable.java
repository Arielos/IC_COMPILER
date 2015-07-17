package IC.SemanticAnalysis;

public class StatementBlockSymbolTable extends SymbolTable {

	public StatementBlockSymbolTable(String id) {
		super(id, Kind.BLOCK);
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Statement Block Symbol Table ( located");
		output.append(" in " + getParentSymbolTable().getId() + " )\n");
		
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
