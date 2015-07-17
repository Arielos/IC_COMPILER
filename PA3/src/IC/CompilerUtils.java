package IC;

import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.TypeTable.ArrayType;
import IC.TypeTable.Type;
import IC.TypeTable.TypeTable;
import IC.TypeTable.UserDefType;

public class CompilerUtils {

	private static Type extractType(DataTypes dataType) {

		Type type = null;

		switch (dataType) {
		case INT:
			type = TypeTable.intType;
			break;
		case STRING:
			type = TypeTable.stringType;
			break;
		case BOOLEAN:
			type = TypeTable.boolType;
			break;
		case VOID:
			type = TypeTable.voidType;
			break;
		default:
			break;
		}
		
		return type;
	}

	public static Type primitiveTypeToMyType(PrimitiveType pType) {
		String arrayName = pType.getName();
		Type arrayType = extractType(pType.getType());
		Type currentArrayType = arrayType;

		if (pType.getDimension() > 0) {
			for (int i = 1; i <= pType.getDimension(); i++) {
				currentArrayType = new ArrayType(arrayName, arrayType, i);
				TypeTable.addArrayType(arrayName, (ArrayType)currentArrayType);
			}
			
			currentArrayType = new ArrayType(arrayName, arrayType, pType.getDimension());
		}

		return currentArrayType;
	}

	public static Type userTypeToMyType(UserType uType) {
		String typeName = uType.getName();
		Type arrayType = new UserDefType(typeName);
		Type currentArrayType = arrayType;

		if (uType.getDimension() > 0) {
			for (int i = 1; i <= uType.getDimension(); i++) {
				currentArrayType = new ArrayType(typeName, arrayType, i);
				TypeTable.addArrayType(typeName + i, (ArrayType)currentArrayType);
			}
			
			currentArrayType = new ArrayType(typeName, arrayType, uType.getDimension());
		}

		return currentArrayType;
	}
	
	public static Type literalTypeToMyType(LiteralTypes lType) {
		Type type = null;
		
		switch (lType) {
		case INTEGER:
			type = TypeTable.intType;
			break;
		case STRING:
			type = TypeTable.stringType;
			break;
		case TRUE:
			type = TypeTable.boolType;
			break;
		case FALSE:
			type = TypeTable.boolType;
			break;
		case NULL:
			type = TypeTable.nullType;
			break;
		}
		
		return type;
		
	}
}
