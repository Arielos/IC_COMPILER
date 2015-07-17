package IC.TypeTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TypeTable {
	
	private static int id = 8;
	
	// Maps element types to array types
	private static final Map<String, ClassType> uniqueClassTypes = new LinkedHashMap<String, ClassType>();
	private static final Map<String, MethodType> uniqueMethodTypes = new LinkedHashMap<String, MethodType>();
	private static final Map<String, ArrayType> uniqueArrayTypes = new LinkedHashMap<String, ArrayType>();
	
	public static Type boolType = new BoolType();
	public static Type intType = new IntType();
	public static Type stringType = new StringType();
	public static Type voidType = new VoidType();
	public static Type nullType = new NullType();
	
	// Adds unique class type object
	public static void addClassType(ClassType classType) {
		// object doesn't exist - create it
		if (!uniqueClassTypes.containsKey(classType.getName())) {
			classType.setId(id++);
			uniqueClassTypes.put(classType.getName(), classType);
		}
	}
	
	// Returns unique class type object
	public static ClassType getClassType(String className) {
		//System.out.println("Attempting to get class: " + className);
		if (uniqueClassTypes.containsKey(className)) {
			// class type object already created - return it
			return uniqueClassTypes.get(className);
		}
		
		return null;
	}

	// Adds unique method type object
	public static void addMethodType(String methodName, MethodType methodType) {
		// object doesn't exist - create it
		if (!uniqueMethodTypes.containsKey(methodType.toString())) {
			if (methodName.equals("main")) {
				methodType.setId(7);
			} else {
				methodType.setId(id++);
			}
			
			uniqueMethodTypes.put(methodType.toString(), methodType);
		}
	}
	
	// Returns unique method type object
	public static MethodType getMethodType(String methodName) {
		//System.out.println("Attempting to get method: " + methodName);
		if (uniqueMethodTypes.containsKey(methodName)) {
			// method type object already created - return it
			return uniqueMethodTypes.get(methodName);
		}
		
		return null;
	}

	// Adds unique array type object
	public static void addArrayType(ArrayType elemType) {
		// object doesn't exist - create it
		if (!uniqueArrayTypes.containsKey(elemType.toString())) {
			ArrayType arrayType = null;
			
			for (int i = 1; i < elemType.getDimention(); i++) {
				arrayType = new ArrayType(elemType.getType(), i);
				arrayType.setId(id++);
				uniqueArrayTypes.put(arrayType.toString(), arrayType);
			}
			
			if (elemType.getType().subtypeOf(TypeTable.stringType)) {
				elemType.setId(6);
			} else {
				elemType.setId(id++);
			}
			
			uniqueArrayTypes.put(elemType.toString(), elemType);
		}
	}
	
	// Returns unique array type object
	public static ArrayType getArrayType(String arrayName) {
		//System.out.println("Attempting to get array: " + arrayName);
		if (uniqueArrayTypes.containsKey(arrayName)) {
			// array type object already created - return it
			return uniqueArrayTypes.get(arrayName);
		}
		
		return null;
	}
	
	public static String print(String path) {
		StringBuffer output = new StringBuffer();
		
		String fileName = path.substring(path.lastIndexOf("\\") + 1);
		
		output.append("Type Table: " + fileName + "\n");
		output.append("    1. Primitive type: int\n");
		output.append("    2. Primitive type: boolean\n");
		output.append("    3. Primitive type: null\n");
		output.append("    4. Primitive type: string\n");
		output.append("    5. Primitive type: void\n");
		
		LinkedHashMap<String, ArrayType> tempArrayMap = sortMap(uniqueArrayTypes);
		LinkedHashMap<String, MethodType> tempMethodMap = sortMap(uniqueMethodTypes);
		
		for (Entry<String, ClassType> entry : uniqueClassTypes.entrySet()) {
			output.append("    " + entry.getValue().getId() + ". Class: " + entry.getValue().getName());
			
			if (entry.getValue().getClassAST().hasSuperClass()) {
				Type superClassType = uniqueClassTypes.get(entry.getValue().getClassAST().getSuperClassName());
				
				output.append(", Superclass ID: " + superClassType.getId());
			}
			
			 output.append("\n");
		}
		
		for (Entry<String, ArrayType> entry : tempArrayMap.entrySet()) {
			output.append("    " + entry.getValue().getId() + ". Array type: " + entry.getValue() + "\n");
		}
		
		for (Entry<String, MethodType> entry : tempMethodMap.entrySet()) {
			output.append("    " + entry.getValue().getId() + ". Method type: {" + entry.getValue() + "}\n");
		}

		return output.toString();
	}
	
	private static <T> LinkedHashMap<String, T> sortMap(Map<String, T> uniquearraytypes2) {
		List<Map.Entry<String, T>> entries =
				  new ArrayList<Map.Entry<String, T>>(uniquearraytypes2.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, T>>() {
			@Override
			public int compare(Map.Entry<String, T> a, Map.Entry<String, T> b){
				if (((Type)a.getValue()).getId() >= ((Type)b.getValue()).getId()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		
		LinkedHashMap<String, T> sortedMap = new LinkedHashMap<String, T>();
		for (Map.Entry<String, T> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		
		return sortedMap;
	}
}
