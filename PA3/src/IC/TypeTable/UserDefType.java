package IC.TypeTable;

public class UserDefType extends Type {

	public UserDefType(String name) {
		super(name);
	}

	@Override
	public boolean subtypeOf(Type t) {
		return true;
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
