package IC.TypeTable;

import IC.AST.ICClass;

public class ClassType extends Type {

	private ICClass classAST;

	public ClassType(ICClass classAST) {
		this(classAST.getName(), classAST);
	}

	public ClassType(String name, ICClass classAST) {
		super(name);
		this.classAST = classAST;
	}

	public ICClass getClassAST() {
		return classAST;
	}

	@Override
	public String toString() {
		return "ClassType";
	}

	@Override
	public boolean subtypeOf(Type t) {
		return t.getClass().isInstance(this);
	}
}
