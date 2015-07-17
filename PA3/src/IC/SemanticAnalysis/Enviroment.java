package IC.SemanticAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import IC.AST.ASTNode;
import IC.TypeTable.Type;

public class Enviroment {
	/**
	 * Maps the names of variables to types. The same variable may
	 * appear in different VarExpr objects. We use the name of the variable as a
	 * way of ensuring we have consistent mapping for each variable.
	 */
	private Map<String, Type> varToType = new HashMap<String, Type>();

	/**
	 * Updates the value of a variable.
	 * 
	 * @param v
	 *            A variable expression.
	 * @param newValue
	 *            The updated value.
	 */
	public void update(String varName, Type assosiateNode) {
		varToType.put(varName, assosiateNode);
	}

	/**
	 * Retrieves the value of the given variable. If the variable has not been
	 * initialized an exception is thrown.
	 * 
	 * @param v
	 *            A variable expression.
	 * @return The value of the given variable in this state.
	 */
	public Object get(String v) {
		System.out.println("Attempting to get item: " + v);
		if (!varToType.containsKey(v)) {
			throw new RuntimeException(
					"Attempt to access uninitialized variable: " + v);
		}

		return varToType.get(v);
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		for (Entry<String, Type> entry : varToType.entrySet()) {
			output.append(entry.getKey() + " -> " + entry.getValue() + "\n");
		}
		
		return output.toString();
	}
}
