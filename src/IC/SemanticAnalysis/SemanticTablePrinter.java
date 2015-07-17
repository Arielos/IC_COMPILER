package IC.SemanticAnalysis;

import IC.AST.ASTNode;

public class SemanticTablePrinter {
	
	private String ICFilePath;
	
	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public SemanticTablePrinter(String ICFilePath) {
		this.ICFilePath = ICFilePath;
	}
	
	public String print(ASTNode textRoot) {
		StringBuffer output = new StringBuffer();
		SymbolTable table = textRoot.enclosingScope();
		
		output.append(table);
		
		return output.toString();
	}
}
