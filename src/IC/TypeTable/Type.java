package IC.TypeTable;

public abstract class Type {

	private int id;
	private String name;
	
	public Type() {
		this(null);
	}

	public Type(String name) {
		this.name = name;
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public abstract boolean subtypeOf(Type t);
	
	@Override
	public String toString() {
		return this.name;
	}
}
