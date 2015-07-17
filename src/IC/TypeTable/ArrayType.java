package IC.TypeTable;

public class ArrayType extends Type {
	
	private Type elemType;
	private int dimention;

	public ArrayType(Type elemType) {
		this(elemType.toString(), elemType, 0);
	}

	public ArrayType(Type elemType, int dimention) {
		this(elemType.toString(), elemType, dimention);
	}

	public ArrayType(String name, Type elemType, int dimention) {
		super(name);
		this.elemType = elemType;
		this.dimention = dimention;
	}
	
	public Type getType() {
		return this.elemType;
	}
	
	public void setType(Type type) {
		this.elemType = type;
	}
	
	public int getDimention() {
		return this.dimention;
	}
	
	public void setDimention(int dimention) {
		this.dimention = dimention;
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
