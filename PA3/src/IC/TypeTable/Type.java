package IC.TypeTable;

public abstract class Type {

	private String name;
	private int dimentions;

	public Type() {
		this(null);
	}

	public Type(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public boolean subtypeOf(Type t) {
		return this == t;
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		output.append(this.name);

		for (int i = 0; i < this.dimentions; i++) {
			output.append("[]");
		}

		return output.toString();
	}
}
