package IC;

import IC.AST.PrimitiveType;
import IC.AST.UserType;
import IC.TypeTable.ArrayType;
import IC.TypeTable.Type;
import IC.TypeTable.TypeTable;

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

    /**
     * Convert AST type to Semantic type
     * @param pType the PrimitiveType node
     * @return new object as Semantic Type
     */
	public static Type primitiveTypeToMyType(PrimitiveType pType) {
		String arrayName = pType.getName();
		Type primitiveType = extractType(pType.getType());
		Type convertedType;
		
		if (pType.getDimension() > 0) {
			for (int i = 1; i <= pType.getDimension(); i++) {
				convertedType = new ArrayType(arrayName, primitiveType, i);
			}
			
			convertedType = new ArrayType(arrayName, primitiveType, pType.getDimension());
		} else {
			convertedType = primitiveType;
		}

		return convertedType;
	}

    /**
     * Convert AST type to Semantic type
     * @param uType the UserType node
     * @return new object as Semantic Type
     */
	public static Type userTypeToMyType(UserType uType) {
		String typeName = uType.getName();
		Type userDefType = TypeTable.getClassType(typeName);
		Type convertedType;
		
		if (uType.getDimension() > 0) {
			for (int i = 1; i <= uType.getDimension(); i++) {
				convertedType = new ArrayType(typeName, userDefType, i);
			}
			
			convertedType = new ArrayType(typeName, userDefType, uType.getDimension());
		} else {
			convertedType = userDefType;
		}

		return convertedType;
	}

    /**
     * Convert AST type to Semantic type
     * @param lType the LiteralTypes node
     * @return new object as Semantic Type
     */
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