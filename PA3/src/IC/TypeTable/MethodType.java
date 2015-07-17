package IC.TypeTable;

public class MethodType extends Type {

	private Type[] paramType;
	private Type returnType;

	public MethodType(Type[] paramType, Type returnType) {
		this(null, paramType, returnType);
	}

	public MethodType(String name, Type[] paramType, Type returnType) {
		super(name);
		this.paramType = paramType;
		this.returnType = returnType;
	}

	public Type[] getParamType() {
		return this.paramType;
	}

	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		if (paramType.length > 0) {

			output.append(paramType[0]);

			if (paramType.length > 1) {
				for (int i = 1; i < paramType.length; i++) {
					output.append(", " + paramType[i].toString());
				}
			}
		}
		output.append(" -> ");

		if (returnType != null) {
			output.append(returnType.toString());
		} else {
			output.append("void");
		}

		return output.toString();
	}

	@Override
	public boolean subtypeOf(Type t) {
		boolean test = false;
		
		if (t instanceof MethodType) {
			MethodType mType = (MethodType)t;
			if (mType.getName().equals(getName()) && mType.returnType.subtypeOf(returnType)) {
				test = true;
			}
			
			if (test) {
				for (int i = 0; i < Math.min(mType.paramType.length, paramType.length); i++) {
					if (!paramType[i].subtypeOf(mType.paramType[i])) {
						test = false;
					}
				}
			}
		}
		
		
		return test;
	}
}
