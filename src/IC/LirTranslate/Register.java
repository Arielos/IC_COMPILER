package IC.LirTranslate;

public class Register {

	private int number;
	private Object value;
	
	public Register(int number, Object value) {
		super();
		this.number = number;
		this.value = value;
	}

	public int getNumber() {
		return number;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public boolean isAvailable() {
		return this.value == null;
	}

	@Override
	public String toString() {
		return "R" + number;
	}
	
	
}
