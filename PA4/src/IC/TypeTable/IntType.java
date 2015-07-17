package IC.TypeTable;

public class IntType extends Type {

	public IntType() {
		super("int");
	}

	@Override
	public boolean subtypeOf(Type t) {
		return t == this;
	}
}
