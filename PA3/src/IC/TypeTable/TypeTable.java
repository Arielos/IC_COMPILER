package IC.TypeTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TypeTable {
	
	private static int id = 8;
	// Maps element types to array types
	private static final Map<String, ClassType> uniqueClassTypes = new LinkedHashMap<String, ClassType>();
	private static final Map<String, MethodType> uniqueMethodTypes = new LinkedHashMap<String, MethodType>();
	private static final Map<String, ArrayType> uniqueArrayTypes = new LinkedHashMap<String, ArrayType>();
	private static final Map<Type, Integer> uniqueTypes = new LinkedHashMap<Type, Integer>();
	
	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();
	public static Type nullType = new NullType();

	// Returns unique class type object
	public static void addClassType(ClassType classType) {
		// object doesn't exist - create and return it
		uniqueClassTypes.put(classType.getName(), classType);
		uniqueTypes.put(classType, id++);
	}
	
	public static ClassType getClassType(String className) {
		if (uniqueClassTypes.containsKey(className)) {
			// class type object already created - return it
			return uniqueClassTypes.get(className);
		}
		
		return null;
	}

	// Returns unique method type object
	public static void addMethodType(String methodName, MethodType methodType) {
			// object doesn't exist - create and return it
			MethodType mt = new MethodType(methodType.getName(),
					methodType.getParamType(), methodType.getReturnType());
			uniqueMethodTypes.put(methodName, mt);
			
			if (methodName.equals("main")) {
				uniqueTypes.put(mt, 7);	
			} else {
				uniqueTypes.put(mt, id++);
			}
	}
	
	public static MethodType getMethodType(String methodName) {
		if (uniqueMethodTypes.containsKey(methodName)) {
			// method type object already created - return it
			return uniqueMethodTypes.get(methodName);
		}
		
		return null;
	}

	// Returns unique array type object
	public static void addArrayType(String arrayName, ArrayType elemType) {
		// object doesn't exist - create and return it
		uniqueArrayTypes.put(arrayName, elemType);
		
		if (elemType.getType().subtypeOf(TypeTable.stringType)) {
			uniqueTypes.put(elemType, 6);
		} else {
			uniqueTypes.put(elemType, id++);
		}
	}
	
	public static ArrayType getArrayType(String arrayName) {
		if (uniqueArrayTypes.containsKey(arrayName)) {
			// array type object already created - return it
			return uniqueArrayTypes.get(arrayName);
		}
		
		return null;
	}

	public static String print() {
		StringBuffer output = new StringBuffer();

		output.append("    1. Primitive type: int\n");
		output.append("    2. Primitive type: boolean\n");
		output.append("    3. Primitive type: null\n");
		output.append("    4. Primitive type: string\n");
		output.append("    5. Primitive type: void\n");
		
		for (Entry<String, ClassType> entry : uniqueClassTypes.entrySet()) {
			output.append("    " + uniqueTypes.get(entry.getValue()) + ". Class: " + entry.getValue().getName() + "\n");
		}
		
		for (Entry<String, ArrayType> entry : uniqueArrayTypes.entrySet()) {
			output.append("    " + uniqueTypes.get(entry.getValue()) + ". Array type: " + entry.getValue() + "\n");
		}
		
		for (Entry<String, MethodType> entry : uniqueMethodTypes.entrySet()) {
			output.append("    " + uniqueTypes.get(entry.getValue()) + ". Method type: {" + entry.getValue() + "}\n");
		}

		return output.toString();
	}
}
