package IC.AST;

/**
 * User-defined data type AST node.
 * 
 * @author Tovi Almozlino
 */
public class UserType extends Type {

	private String name;

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) throws Exception {
		return visitor.visit(this, context);
	}

	/**
	 * Constructs a new user-defined data type node.
	 * 
	 * @param line
	 *            Line number of type declaration.
	 * @param name
	 *            Name of data type.
	 */
	public UserType(int line, String name) {
		super(line);
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

}
