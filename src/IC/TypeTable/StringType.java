package IC.TypeTable;

public class StringType extends Type {
	
	public StringType() {
		super("string");
	}

	@Override
	public boolean subtypeOf(Type t) {
		return t == this;
	}
}
