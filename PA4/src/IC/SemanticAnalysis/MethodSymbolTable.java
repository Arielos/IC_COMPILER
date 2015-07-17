package IC.SemanticAnalysis;

import java.util.Map.Entry;

import IC.TypeTable.ArrayType;
import IC.TypeTable.MethodType;
import IC.TypeTable.Type;
import IC.TypeTable.TypeTable;

public class MethodSymbolTable extends SymbolTable {

	public MethodSymbolTable(String id) {
		super(id, Kind.METHOD);
	}

    public SymbolTableRow getReturnType() {

        SymbolTableRow retType = null;
        for (Entry<String, SymbolTableRow> entry : super.entries.entrySet()) {
            if (entry.getValue().getKind() == Kind.RET_VAR) {
                retType = entry.getValue();
                break;
            }
        }

        return retType;
    }

	@Override
	public void AddUniqueTypes() {
		MethodType methodType = ((MethodType)this.getParentSymbolTable().lookup(getId()).getType());
		
		for (Type paramType : methodType.getParamType()) {
			if (paramType instanceof ArrayType) {
				TypeTable.addArrayType((ArrayType)paramType);
			}
		}
		
		if (methodType.getReturnType() instanceof ArrayType) {
			TypeTable.addArrayType((ArrayType)methodType.getReturnType());
		}
		
		TypeTable.addMethodType(getId(), methodType);
		
		Type myType;
		if (entries.size() > 0) {
			for (Entry<String, SymbolTableRow> entry : entries.entrySet()) {
				myType = entry.getValue().getType();
				
				if (myType instanceof ArrayType) {
					TypeTable.addArrayType((ArrayType)myType);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		
		output.append("Method Symbol Table: " + getId() + "\n");
		output.append(super.toString());
		
		if (getChildList().size() > 0) {
			output.append("\n");
			for (SymbolTable child : getChildList()) {
					output.append(child);
			}
		}
		
		return output.toString();
	}
}
