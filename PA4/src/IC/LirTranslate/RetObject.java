package IC.LirTranslate;

import IC.TypeTable.Type;

import java.util.Objects;

/**
 * RetObject class to pass data between the visitors
 */
public class RetObject {

	private StringBuffer lirString;
    private String result;
    private String context;
	private Type type;
    private WhoType whoType;
	
	public RetObject() {
		lirString = new StringBuffer();
	}

    /**
     * Add lir instruction
     * @param lir the lir string to add
     */
	public void addLir(String lir) {
		this.lirString.append(lir);
	}

    /**
     * Get the lir instructions
     * @return the instructions
     */
	public String getLir() {
		return lirString.toString();
	}

    /**
     * Sets the context of the returned object
     * @param context the context of the object, variable name, class name, array name, etc
     */
	public void setContext(String context) {
		this.context = context;
	}

    public void setContext(Object context) {
        setContext(context.toString());
    }

    /**
     * Get the context of the object
     * @return the context
     */
	public String getContext() {
		return context;
	}

    /**
     * Sets the returned type of the object
     * @param type the type of the object as in <code>TypeTable</code> types package
     */
	public void setType(Type type) {
		this.type = type;
	}

    /**
     * Get the type of the object
     * @return the type of the object as in <code>TypeTable</code> types package
     */
	public Type getType() {
		return this.type;
	}

    /**
     * Set who is the returned type result
     * @param type the <code>WhoType</code> enum value
     */
    public void setWhoType(WhoType type) {
        this.whoType = type;
    }

    /**
     * Get the returned type result
     * @return the <code>WhoType</code> object
     */
    public WhoType getWhoType() {
        return this.whoType;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;
    }
	
	public boolean isLiteral() {
		return this.whoType == WhoType.LITERAL;
	}

    public boolean isArray() { return this.whoType == WhoType.ARRAY; }

    public boolean isField() { return this.whoType == WhoType.FIELD; }

    public boolean isVar() { return this.whoType == WhoType.VAR; }

    public boolean isMemory() { return this.whoType == WhoType.MEMORY; }
	
	@Override
	public String toString() {
		if (isLiteral() || isMemory()) {
			return context.toString();
		} else {
			return lirString.toString();
		}
	}

    public enum WhoType {
        VAR, ARRAY, FIELD, LITERAL, VIRTUAL_METHOD, STATIC_METHOD, CLASS, MEMORY
    }
}
