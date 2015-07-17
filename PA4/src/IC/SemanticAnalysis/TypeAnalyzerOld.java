package IC.SemanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import IC.*;
import IC.AST.*;
import IC.TypeTable.*;
import IC.TypeTable.MethodType.VirtualOrStatic;
import IC.TypeTable.Type;

public class TypeAnalyzerOld implements
		PropagatingVisitor<Enviroment, Object> {

	private ASTNode root;
	private StringBuffer errors;

	public TypeAnalyzerOld(ASTNode root) {
		this.root = root;
	}

	public void evaluate() {
		Enviroment env = new Enviroment();
		errors = new StringBuffer();
		
		try {
			root.accept(this, env);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public Object visit(Program program, Enviroment context) throws Exception {
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(ICClass icClass, Enviroment context) throws Exception {
		ClassType classType = new ClassType(icClass);
		
		context.update(icClass.getName(), classType);
		
		for (Field field : icClass.getFields()) {
			field.accept(this, context);
		}

		for (Method method : icClass.getMethods()) {
			method.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(Field field, Enviroment context) throws Exception {
		context.update(field.getName(), (Type)field.getType().accept(this, context));
		return context.get(field.getName());
	}

	@Override
	public Object visit(VirtualMethod method, Enviroment context) throws Exception {
		
		Type methodType = (Type)method.getType().accept(this, context);
		
		context.update(method.getName(), methodType);
		
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(StaticMethod method, Enviroment context) throws Exception {

		Type methodType = (Type)method.getType().accept(this, context);
		
		System.err.println(methodType.toString());
		
		context.update(method.getName(), methodType);
		
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(LibraryMethod method, Enviroment context) throws Exception {
		
		Type methodType = (Type)method.getType().accept(this, context);
		
		context.update(method.getName(), methodType);
		
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, context);
		}
		
		return null;
	}

	@Override
	public Object visit(Formal formal, Enviroment context) throws Exception {
		Type formalType = (Type)formal.getType().accept(this, context);
		context.update(formal.getName(), formalType);
		
		return formalType;
	}

	@Override
	public Object visit(PrimitiveType type, Enviroment context) throws Exception {
		Type convertedprimitiveType = CompilerUtils.primitiveTypeToMyType(type);
		context.update(convertedprimitiveType.getName(), convertedprimitiveType);
		
		return convertedprimitiveType;
	}

	@Override
	public Object visit(UserType type, Enviroment context) throws Exception {
		Type convertedUserDefType = CompilerUtils.userTypeToMyType(type);
		context.update(convertedUserDefType.getName(), convertedUserDefType);
		
		return convertedUserDefType;
	}

	@Override
	public Object visit(Assignment assignment, Enviroment context) throws Exception {
		System.out.println(assignment.getLine() + "   -> Assignment");
		Expression rhs = assignment.getAssignment();
		Location lhs = assignment.getVariable();
		
		Type expressionValue = (Type)rhs.accept(this, context);
		Type variable = (Type)lhs.accept(this, context);
		
		System.out.println("    >>> lhs: " + variable);
		System.out.println("    >>> rhs: " + expressionValue);
		
		if (variable.subtypeOf(expressionValue)) {
			System.out.println("All Good!");
		} else {
			System.err.println("All Bad!");
			errors.append("Assignment type error at line " + assignment.getLine() + "\n");
		}
		
		System.out.println(assignment.getLine() + "   <- Assignment");
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement, Enviroment context) throws Exception {
		System.out.println(callStatement.getLine() + "    -> CallStatement");
		return callStatement.getCall().accept(this, context);
	}

	@Override
	public Object visit(Return returnStatement, Enviroment context) throws Exception {
		System.out.println(returnStatement.getLine() + "    -> ReturnStatement");
		
		Object retValue = null;
		
		if (returnStatement.hasValue()) {
			retValue = returnStatement.getValue().accept(this, context);
			System.out.println("      retValue: " + retValue.toString());
		}
		
		
		return retValue;
	}

	@Override
	public Object visit(If ifStatement, Enviroment context) throws Exception {
		System.out.println(ifStatement.getLine() + "- - - - - ifStatement - - - - -");
		if (ifStatement.getCondition().accept(this, context) != TypeTable.boolType) {
			errors.append("if statement expression must be boolean type at line " + ifStatement.getLine() + "\n");
			System.out.println("if -> BAD");
		}
		
		ifStatement.getOperation().accept(this, context);

		if (ifStatement.hasElse()) {
			System.out.println(ifStatement.getElseOperation().getLine() + "- - - - - elseStatement - - - - -");
			ifStatement.getElseOperation().accept(this, context);
		}
		
		System.out.println(ifStatement.getLine() + "e - - - - ifStatement - - - - e");
		return null;
	}

	@Override
	public Object visit(While whileStatement, Enviroment context) throws Exception {
		System.out.println(whileStatement.getLine() + "- - - - - whileStatement - - - - -");
		if (whileStatement.getCondition().accept(this, context) != null) {
			System.out.println("While -> OK");
		}
		
		whileStatement.getOperation().accept(this, context);
		System.out.println(whileStatement.getLine() + "e - - - - whileStatement - - - - e");
		return null;
	}

	@Override
	public Object visit(Break breakStatement, Enviroment context) throws Exception {
		System.out.println(breakStatement.getLine() + "    -> BreakStatement");
		return null;
	}

	@Override
	public Object visit(Continue continueStatement, Enviroment context) throws Exception {
		System.out.println(continueStatement.getLine() + "    -> ContinueStatement");
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, Enviroment context) throws Exception {
		System.out.println(statementsBlock.getLine() + "  -> StatementsBlock");
		
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable, Enviroment context) throws Exception {
		System.out.println(localVariable.getLine() + "    -> LocalVariable");
		
		Type localVariableType = (Type)localVariable.getType().accept(this, context); 
		
		if (localVariable.hasInitValue()) {
			Type initValueType = (Type)localVariable.getInitValue().accept(this, context);
		
			if (localVariableType.subtypeOf(initValueType)) {
				System.err.println(localVariable.getLine() + "    ---->> " + localVariableType.toString());
				System.err.println(localVariable.getLine() + "    ---->> " + initValueType.toString());
			}
		}
		
		context.update(localVariable.getName(), localVariableType);
		
		return localVariableType;
	}

	@Override
	public Object visit(VariableLocation location, Enviroment context) throws Exception {
		System.out.println(location.getLine() + "    -> VariableLocation");
		
		Type localVariableType = context.get(location.getName());
		
		if (location.getLocation() != null) {
			return location.getLocation().accept(this, context);
		}
		
		if (localVariableType != null) {
			return localVariableType;
		}
		
		return null;
	}

	@Override
	public Object visit(ArrayLocation location, Enviroment context) throws Exception {
		System.out.println(location.getLine() + "    -> ArrayLocation");
		Type temp;
		
		ArrayType arrayType = (ArrayType)location.getArray().accept(this, context);
		Type indexType = (Type)location.getIndex().accept(this, context);
		
		if (arrayType.getDimention() > 1) {
			temp = TypeTable.getArrayType(arrayType.toString()).getType();	
		} else {
			temp = arrayType.getType();
		}
		
		if (indexType.subtypeOf(TypeTable.intType)) {
			System.out.println("E :- e0:" + arrayType + "  E :- e1:" + indexType + 
					" => E :- e0[e1]:" + temp);
			System.out.println(location.getLine() + "    <- ArrayLocation");
			return temp;
		} else {
			throw new SemanticError("Index type must be an Integer, found " + indexType);
			//System.err.println("Type missmatch!");
			//System.out.println(location.getLine() + "    <- ArrayLocation");
		}
	}

	@Override
	public Object visit(StaticCall call, Enviroment context) throws Exception {
		System.out.println(call.getLine() + "    -> StaticCall");
		/*
		List<Type> argumentsTypeList = new ArrayList<>();
		
		for (Expression expr : call.getArguments()) {
			argumentsTypeList.add((Type) expr.accept(this, context));
		}
		
		for (Type type : argumentsTypeList) {
			System.out.println("type: " + type);
		}
		
		System.err.println(context.get(call.getName()).getName());
		
		ClassType classType = TypeTable.getClassType(call.getClassName());
		MethodType methodType = TypeTable.getMethodType(call.getName());
		
		System.out.println("call.getName(): " + call.getName());
		System.out.println("argumentsTypeList: " + argumentsTypeList);
		System.out.println("methodType: " + methodType);
		System.out.println("methodType.getReturnType(): " + methodType.getReturnType());
		
		MethodType methodToTest = new MethodType(call.getName(), VirtualOrStatic.Static, argumentsTypeList, methodType.getReturnType());
		
		if (classType.getClassAST().enclosingScope().lookup(call.getName()) != null) {
			if (methodType.subtypeOf(methodToTest)) {
				System.out.println("All good!");
				return methodType.getReturnType();
			}
		}
		
		throw new SemanticError("Non static method used in line " + call.getLine());
		*/
		//System.err.println("Not Good!");
		return null;
	}

	@Override
	public Object visit(VirtualCall call, Enviroment context) throws Exception {
		System.out.println(call.getLine() + "    -> VirtualCall");
		List<Type> argumentsTypeList = new ArrayList<Type>();
		
		for (Expression expr : call.getArguments()) {
			argumentsTypeList.add((Type) expr.accept(this, context));
		}
		
		MethodType methodType = TypeTable.getMethodType(call.getName());
		
		MethodType methodToTest = new MethodType(call.getName(), VirtualOrStatic.Static, argumentsTypeList, methodType.getReturnType());
		
		if (call.isExternal()) {
			ClassType classType = TypeTable.getClassType(((Type)call.getLocation().accept(this, context)).getName());	
			if (classType.getClassAST().enclosingScope().lookup(call.getName()) == null) {
				//throw new SemanticError("Call Exception");
			}
		}
		
		if (methodType.subtypeOf(methodToTest)) {
			System.out.println("All good!");
			return methodType.getReturnType();
		}
		
		throw new SemanticError("No virtual function matched to " + methodType + " in line " + call.getLine());
	}

	@Override
	public Object visit(This thisExpression, Enviroment context) throws Exception {
		System.out.println(thisExpression.getLine() + "    -> thisExpression");
		return null;
	}

	@Override
	public Object visit(NewClass newClass, Enviroment context) throws Exception {
		System.out.println(newClass.getLine() + "    -> newClass ---- " + newClass.getName());
		return TypeTable.getClassType(newClass.getName());
	}

	@Override
	public Object visit(NewArray newArray, Enviroment context) throws Exception {
		System.out.println(newArray.getLine() + "    -> newArray");
		
		Type arrayType = null;
		Type sizeType = (Type)newArray.getSize().accept(this, context);
		
		if (sizeType.subtypeOf(TypeTable.intType)) {
			arrayType = (Type)newArray.getType().accept(this, context);
			context.update(arrayType.getName(), arrayType);
			
			System.out.println("E :- e:" + sizeType + " => E :- T[e]:" + arrayType);
		}
		
		return arrayType;
	}

	@Override
	public Object visit(Length length, Enviroment context) throws Exception {
		System.out.println(length.getLine() + "    -> length");
		Type array = (Type)length.getArray().accept(this, context);
		
		if (context.get(array.getName()) == null) {
			return null;
		}
		
		return TypeTable.intType;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, Enviroment context) throws Exception {
		System.out.println(binaryOp.getLine() + "- - - - MathBinaryOp - - - -");
		Type result = null;
		Type lhsVal = (Type)binaryOp.getFirstOperand().accept(this, context);
		Type rhsVal = (Type)binaryOp.getSecondOperand().accept(this, context);
		BinaryOps op = binaryOp.getOperator();
		
		System.out.println("lhsVal: " + lhsVal);
		System.out.println("rhsVal: " + rhsVal);
		System.out.println("Op: " + op);
		
		if (lhsVal.subtypeOf(rhsVal)) {
			if (lhsVal.subtypeOf(TypeTable.stringType) && op == BinaryOps.PLUS) {
				result = TypeTable.stringType;
				System.out.println("E :- e0:" + lhsVal + "   E :- e1:" + rhsVal + "  op:" + binaryOp.getOperator() + " => E :- e0 op e1:" + result);
				System.out.println("TRUE");
			} else if (lhsVal.subtypeOf(TypeTable.intType)) {
				result = TypeTable.intType;
				System.out.println("E :- e0:" + lhsVal + "   E :- e1:" + rhsVal + "  op:" + binaryOp.getOperator() + " => E :- e0 op e1:" + result);
				System.out.println("TRUE");
			} else {
				throw new SemanticError("Math Binary Op only valid on String or Integer type in line " + binaryOp.getLine());
				//System.out.println("FALSE");
			}
		} else {
			throw new SemanticError("Math Binary Op types not matched in line " + binaryOp.getLine());
			//System.out.println("FALSE");
		}
		
		System.out.println(binaryOp.getLine() + "e - - - MathBinaryOp - - - e");
		return result;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, Enviroment context) throws Exception {
		System.out.println(binaryOp.getLine() + "- - - - LogicalBinaryOp - - - -");
		Type result = null;
		Type lhsVal = (Type)binaryOp.getFirstOperand().accept(this, context);
		Type rhsVal = (Type)binaryOp.getSecondOperand().accept(this, context);
		BinaryOps op = binaryOp.getOperator();
		
		System.out.println("lhsVal: " + lhsVal);
		System.out.println("rhsVal: " + rhsVal);
		System.out.println("Op: " + op);
		
		if (lhsVal.subtypeOf(rhsVal) || rhsVal.subtypeOf(lhsVal)) {
			if (op == BinaryOps.EQUAL || op == BinaryOps.NEQUAL) {
				System.out.println("TRUE");
				result = TypeTable.boolType;
			} else {
				if (lhsVal.subtypeOf(TypeTable.intType)
						&& (op == BinaryOps.GT
							|| op == BinaryOps.GTE
							|| op == BinaryOps.LT
							|| op == BinaryOps.LTE)) {
					System.out.println("TRUE");
					result = TypeTable.boolType; 
				} else if (lhsVal.subtypeOf(TypeTable.boolType)
						&& (op == BinaryOps.LAND || op == BinaryOps.LOR)) {
					System.out.println("TRUE");
					result = TypeTable.boolType;
				} else {
					throw new SemanticError("Math Binary Op only valid on Boolean or Integer type in line " + binaryOp.getLine());
					//System.out.println("FALSE");
				}
			}
		} else {
			throw new SemanticError("Logical Binary Op types not mathced in line " + binaryOp.getLine());
			//System.out.println("FALSE");
		}
		System.out.println(binaryOp.getLine() + "e - - - LogicalBinaryOp - - - e");
		return result;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, Enviroment context) throws Exception {
		System.out.println(unaryOp.getLine() + "- - - - MathUnaryOp - - - -");
		UnaryOps op = unaryOp.getOperator();
		Expression expr = unaryOp.getOperand();
		
		Type exprType = (Type)expr.accept(this, context);
		
		if (op != UnaryOps.UMINUS) {
			throw new RuntimeException("Encountered unexpected operator " + op);
		}
		
		if (exprType.subtypeOf(TypeTable.intType)) {
			System.out.println("E :- e:" + exprType + " => E :- -e:" + exprType);
			System.out.println("e - - - MathUnaryOp - - - e");
			return unaryOp.getOperand().accept(this, context);	
		}
		
		System.out.println(unaryOp.getLine() + "e - - - MathUnaryOp - - - e");
		throw new SemanticError("Math Unary Op only valid on Integer type in line " + unaryOp.getLine());
		//return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, Enviroment context) throws Exception {
		System.out.println(unaryOp.getLine() + "- - - - LogicalUnaryOp - - - -");
		UnaryOps op = unaryOp.getOperator();
		Expression expr = unaryOp.getOperand();
		
		Type exprType = (Type) expr.accept(this, context);
		
		if (op != UnaryOps.LNEG) {
			throw new RuntimeException("Encountered unexpected operator " + op);
		}

		if (exprType.subtypeOf(TypeTable.boolType)) {
			System.out.println("E :- e:" + exprType + " => E :- !e:" + exprType);
			System.out.println("e - - - LogicalUnaryOp - - - e");
			return unaryOp.getOperand().accept(this, context);	
		}
		
		System.out.println(unaryOp.getLine() + "e - - - LogicalUnaryOp - - - e");
		throw new SemanticError("Logical Unary Op only valid on Boolean type in line " + unaryOp.getLine());
		//return null;
	}

	@Override
	public Object visit(Literal literal, Enviroment context) throws Exception {
		return CompilerUtils.literalTypeToMyType(literal.getType());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, Enviroment context) throws Exception {
		Object expr = expressionBlock.getExpression().accept(this, context);
		return expr;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, Enviroment context) throws Exception {
		return null;
	}

}
