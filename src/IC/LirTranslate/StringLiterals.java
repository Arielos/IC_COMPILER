package IC.LirTranslate;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * StringLiterals class to manage the string literals in the code
 */
public class StringLiterals {
	
	private final String name;
	private int stringId;
	private Map<String, String> stringLiterals;
	
	public StringLiterals() {
		this.stringId = 1;
		this.name = "str";
		this.stringLiterals = new LinkedHashMap<>();
	}
	
	public String addStringlIteral(String value) {
		String currentId = name + stringId;
		
		if (!stringLiterals.containsKey(value)) {
			stringLiterals.put(value, currentId);
			stringId += 1;
		} else {
			currentId = stringLiterals.get(value);
		}
		
		return currentId;
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		for (Entry<String, String> entry : stringLiterals.entrySet()) {
            output.append(entry.getValue() + ": \"" + entry.getKey() + "\"\n");
		}
		
		return output.toString();
	}
}
