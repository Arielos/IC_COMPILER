package IC.SemanticAnalysis;

public class GlobalSymbolTable extends SymbolTable {

	private String ICFileName;
	
	public GlobalSymbolTable(String id, String fileName) {
		super(id, null);
		ICFileName = fileName;
	}

	@Override
	public void AddUniqueTypes() {
		if (getChildList().size() > 0) {
			for (SymbolTable child : getChildList()) {
				child.AddUniqueTypes();	
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Global Symbol Table: " + ICFileName + "\n");
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
