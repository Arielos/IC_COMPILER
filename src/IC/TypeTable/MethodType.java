package IC.TypeTable;

import java.util.List;

public class MethodType extends Type {
	
	private VirtualOrStatic virtualOrStatic;
	private List<Type> paramType;
	private Type returnType;

	public MethodType(VirtualOrStatic virtualOrStatic, List<Type> paramType, Type returnType) {
		this(null, virtualOrStatic, paramType, returnType);
	}

	public MethodType(String name, VirtualOrStatic virtualOrStatic, List<Type> paramType, Type returnType) {
		this.virtualOrStatic = virtualOrStatic;
		this.paramType = paramType;
		this.returnType = returnType;
		super.setName(toString());
	}
	
	public boolean isStatic() {
		return virtualOrStatic == VirtualOrStatic.Static;
	}
	
	public boolean isVirtual() {
		return virtualOrStatic == VirtualOrStatic.Virtual;
	}
	
	public List<Type> getParamType() {
		return this.paramType;
	}

	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		if (paramType.size() > 0) {
			output.append(paramType.get(0));

			if (paramType.size() > 1) {
				for (int i = 1; i < paramType.size(); i++) {
					output.append(", " + paramType.get(i));
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
				if (mType.paramType.size() == paramType.size()) {
					for (int i = 0; i < paramType.size(); i++) {
						if (!paramType.get(i).subtypeOf(mType.paramType.get(i))) {
							test = false;
							break;
						}
					}
				} else {
					test = false;
				}
			}
		}
		
		return test;
	}
	
	public enum VirtualOrStatic {
		Virtual, Static
	}
}
