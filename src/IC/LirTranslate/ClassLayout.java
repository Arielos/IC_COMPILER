package IC.LirTranslate;

import IC.AST.Field;
import IC.AST.ICClass;
import IC.AST.Method;
import IC.SemanticAnalysis.SymbolTable;
import IC.TypeTable.MethodType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ClassLayout {
	private Map<Method, Integer> methodToOffset;
	private Map<Field, Integer> fieldToOffset;
	private Map<String, Method> staticMethods;
	
	private String className;
	private String superClassName;
	private int methodOffset = 0;
	private int fieldOffset = 1;
	
	public ClassLayout(String className) {
		this.className = className;
		this.superClassName = "";
		
		methodToOffset = new LinkedHashMap<>();
		fieldToOffset = new LinkedHashMap<>();
		staticMethods = new LinkedHashMap<>();
	}
	
	public void tableToLayout(ICClass icClass) {
		SymbolTable myTable = icClass.enclosingScope();

		icClass.getFields().forEach(this::insertField);
		
		for (Method method : icClass.getMethods()) {
			if (((MethodType)myTable.look(method.getName()).getType()).isVirtual()) {
				insertMethod(method);
			} else {
				staticMethods.put(method.getName(), method);
			}
		}
	}
	
	public void tableToLayout(ICClass icClass, ClassLayout superIcClassLayout) {
		
		superClassName = superIcClassLayout.className;
		SymbolTable myTable = icClass.enclosingScope();
		
		for (Entry<Field, Integer> field : superIcClassLayout.fieldToOffset.entrySet()) {
			insertField(field.getKey());
		}
		
		for (Entry<Method, Integer> method : superIcClassLayout.methodToOffset.entrySet()) {
			insertMethod(method.getKey());
		}
		
		methodOffset = methodToOffset.size();
		fieldOffset = fieldToOffset.size() + 1;

		icClass.getFields().forEach(this::insertField);
		
		for (Method method : icClass.getMethods()) {
			if (((MethodType)myTable.look(method.getName()).getType()).isVirtual()) {
				insertMethod(method);
			} else {
				staticMethods.put(method.getName(), method);
			}
		}
	}
	
	private void insertField(Field field) {
		fieldToOffset.put(field, fieldOffset++);
	}
	
	private void insertMethod(Method method) {
		int offset = methodOffset++;
		for (Entry<Method, Integer> entry : methodToOffset.entrySet()) {
			if (entry.getKey().getName().equals(method.getName())) {
				offset = entry.getValue();
				methodToOffset.remove(entry.getKey());
				break;
			}
		}
		
		methodToOffset.put(method, offset);
	}
	
	public int getSize() {
		return fieldOffset * 4;
	}
	
	public String getName() {
		return className;
	}
	
	public Integer getFieldIndex(String name) {
		Integer index = null;
		for (Entry<Field, Integer> entry : fieldToOffset.entrySet()) {
			if (entry.getKey().getName().equals(name)) {
				index = entry.getValue();
				break;
			}
		}
		
		return index;
	}
	
	public Integer getMethodIndex(String name) {
		Integer index = null;
		for (Entry<Method, Integer> entry : methodToOffset.entrySet()) {
			if (entry.getKey().getName().equals(name)) {
				index = entry.getValue();
				break;
			}
		}
		
		return index;
	}
	
	public Method getStaticMethod(String name) {
		return staticMethods.get(name);
	}
	
	public Method getMethodSymbol(String name) {
		Method row = null;
		for (Entry<Method, Integer> entry : methodToOffset.entrySet()) {
			if (entry.getKey().getName().equals(name)) {
				row = entry.getKey();
				break;
			}
		}
		
		return row;
	}
	
	public String print() {
		StringBuffer output = new StringBuffer();
		
		int i = methodToOffset.size();
        Method currentMethod;

		Iterator<Method> methodsSet = methodToOffset.keySet().iterator(); 
		
		while (methodsSet.hasNext() && i > 1) {
            currentMethod = methodsSet.next();
			output.append("_" + currentMethod.enclosingScope().getParentSymbolTable().getId() + "_" + currentMethod.getName() + ", ");
			i--;
		}
		
		if (methodsSet.hasNext()) {
            currentMethod = methodsSet.next();
            output.append("_" + currentMethod.enclosingScope().getParentSymbolTable().getId() + "_" + currentMethod.getName());
		}
		
		return output.toString();
	}
	
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		output.append("Class Name: " + className);
		if (!superClassName.equals("")) {
			output.append(" : " + superClassName);
		}

		output.append("\n");

		output.append("  > Fields:\n");
		for (Entry<Field, Integer> entry : fieldToOffset.entrySet()) {
			output.append("    - " + entry.getValue() + ". " + entry.getKey().getName() + "\n");
		}

		output.append("  > Method:\n");
		for (Entry<Method, Integer> entry : methodToOffset.entrySet()) {
			output.append("    - " + entry.getValue() + ". " + entry.getKey().getName() + "\n");
		}

		return output.toString();
	}
}