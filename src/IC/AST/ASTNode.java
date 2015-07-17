package IC.AST;

import IC.SemanticAnalysis.SymbolTable;

/**
 * Abstract AST node base class.
 * 
 * @author Tovi Almozlino
 */
public abstract class ASTNode {

	private int line;

	private SymbolTable enclosingScope;

	/**
	 * Double dispatch method, to allow a visitor to visit a specific subclass.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @return A value propagated by the visitor.
	 */
	public abstract Object accept(Visitor visitor);

	public abstract <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) throws Exception;

	/**
	 * Constructs an AST node corresponding to a line number in the original
	 * code. Used by subclasses.
	 * 
	 * @param line
	 *            The line number.
	 */
	protected ASTNode(int line) {
		this.line = line;
	}

	public int getLine() {
		return line;
	}

	public void setEnclosingScope(SymbolTable table) {
		this.enclosingScope = table;
	}

	public SymbolTable enclosingScope() {
		return this.enclosingScope;
	}

}
