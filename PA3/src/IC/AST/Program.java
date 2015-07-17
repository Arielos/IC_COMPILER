package IC.AST;

import java.util.List;

/**
 * Root AST node for an IC program.
 * 
 * @author Tovi Almozlino
 */
public class Program extends ASTNode {

	private List<ICClass> classes;

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) {
		return visitor.visit(this, context);
	}

	/**
	 * Constructs a new program node.
	 * 
	 * @param classes
	 *            List of all classes declared in the program.
	 */
	public Program(List<ICClass> classes) {
		super(0);
		this.classes = classes;
	}

	public List<ICClass> getClasses() {
		return classes;
	}

}
