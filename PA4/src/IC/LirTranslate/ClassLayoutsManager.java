package IC.LirTranslate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import IC.AST.ICClass;

public class ClassLayoutsManager {
	
	private Map<String, ClassLayout> result;
	
	public ClassLayoutsManager() {
		result = new LinkedHashMap<>();
	}
	
	public void build(List<ICClass> classesFromVisitor) {
		
		int currentClassIndex = 0;
		ICClass currentClass;
		ClassLayout layout, parentLayout;
		
		while (currentClassIndex < classesFromVisitor.size()) {
			currentClass = classesFromVisitor.get(currentClassIndex);
			if (!currentClass.hasSuperClass()) {
				layout = new ClassLayout(currentClass.getName());
				layout.tableToLayout(currentClass);
                result.put(currentClass.getName(), layout);
				classesFromVisitor.remove(currentClassIndex);
			} else {
				currentClassIndex += 1;
			}
		}
		
		currentClassIndex = 0;
		while (classesFromVisitor.size() > 0) {
			currentClass = classesFromVisitor.get(currentClassIndex);
			
			parentLayout = result.get(currentClass.getSuperClassName());
			
			if (parentLayout != null) {
				layout = new ClassLayout(currentClass.getName());
				layout.tableToLayout(currentClass, parentLayout);
				result.put(currentClass.getName(), layout);
				classesFromVisitor.remove(currentClassIndex);
			} else {
				currentClassIndex += 1 % classesFromVisitor.size() - 1; 
			}
		}
	}
	
	public ClassLayout getLayout(String className) {
        if (result.containsKey(className)) {
            return result.get(className);
        } else {
            return null;
        }
	}
	
	public String printPointers() {
		StringBuffer output = new StringBuffer();

		for (ClassLayout layout : result.values()) {
			if (!layout.getName().equals("Library")) {
				output.append("_DV_" + layout.getName() + ": [" + layout.print() + "]\n");
			}
		}
		
		return output.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		result.values().forEach(output::append);
		
		return output.toString();
	}
}
