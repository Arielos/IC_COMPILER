package IC.TypeTable;

import IC.AST.ICClass;

public class ClassType extends Type {
	
	private ICClass classAST;
	private ClassType superClass;
	

	public ClassType(ICClass classAST) {
		this(classAST.getName(), classAST);
	}

	public ClassType(String name, ICClass classAST) {
		super(name);
		this.classAST = classAST;
	}

	public ICClass getClassAST() {
		return this.classAST;
	}
	
	public void setSuperClass(ClassType superClass) {
		this.superClass = superClass;
	}
	
	public ClassType getSuperClass() {
		return this.superClass;
	}
	
	public boolean hasSuperClass() {
		return superClass != null;
	}

	@Override
	public boolean subtypeOf(Type t) {
		if (!(t instanceof ClassType)) {
			return false;
		}
		
	    if (this == t) {
	    	return true;
	    }
	    
	    if (superClass == null) {
	    	return false;
	    }
	    
	    ClassType c = superClass;
	    while (c != null) {
            if (c == t) {
                return true;
            }
            
            c = c.getSuperClass();  
	    }
	    
	    if (((ClassType)t).hasSuperClass()) {
            return subtypeOf(((ClassType)t).getSuperClass());
	    }
	    
	    return false;
	}
}
