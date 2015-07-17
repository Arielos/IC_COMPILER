package IC.TypeTable;

public class ArrayType extends Type {

	private Type elemType;
	private int dimention;

	public ArrayType(Type elemType) {
		this(null, elemType, 0);
	}

	public ArrayType(String name, Type elemType) {
		this(name, elemType, 0);
	}

	public ArrayType(String name, Type elemType, int dimention) {
		super(name);
		this.elemType = elemType;
		this.dimention = dimention;
	}

	public Type getType() {
		return this.elemType;
	}

	@Override
	public String toString() {

		StringBuffer output = new StringBuffer();

		output.append(elemType);

		for (int i = 0; i < dimention; i++) {
			output.append("[]");
		}
		return output.toString();
	}

	@Override
	public boolean subtypeOf(Type t) {
		
		if (t instanceof ArrayType) {
			ArrayType aType = (ArrayType)t;
			
			if ((aType.dimention == this.dimention)
				&& (aType.elemType.subtypeOf(this.elemType))) {
				return true;
			}
		}
		
		return false;
	}
}
