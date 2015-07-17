package IC.TypeTable;

public class NullType extends Type {

	public NullType() {
		super("null");
	}
	
	@Override
	public boolean subtypeOf(Type t) {
		
		if (t instanceof StringType) {
            return true;
	    }
	    
		if (t instanceof NullType) {
            return true;
	    }
	    
	    if (t instanceof ArrayType) {
            return true;    
	    }
	    
	    if (t instanceof ClassType) {
            return true;
	    }
	    
	    return false;
	}
}
