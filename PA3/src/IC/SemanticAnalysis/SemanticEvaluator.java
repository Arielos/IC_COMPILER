package IC.SemanticAnalysis;

import IC.*;
import IC.AST.*;
import IC.TypeTable.*;
import IC.TypeTable.Type;

public class SemanticEvaluator implements
		PropagatingVisitor<Enviroment, Object> {

	private ASTNode root;

	public SemanticEvaluator(ASTNode root) {
		this.root = root;
	}

	public void evaluate() {
		Enviroment env = new Enviroment();
		root.accept(this, env);
	}
	
	@Override
	public Object visit(Program program, Enviroment context) {
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(ICClass icClass, Enviroment context) {
		ClassType classType = new ClassType(icClass);
		TypeTable.addClassType(classType);
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
	public Object visit(Field field, Enviroment context) {
		context.update(field.getName(), (Type)field.getType().accept(this, context));
		return context.get(field.getName());
	}

	@Override
	public Object visit(VirtualMethod method, Enviroment context) {

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
	public Object visit(StaticMethod method, Enviroment context) {

		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(LibraryMethod method, Enviroment context) {
		/*
		for (Formal formal : method.getFormals()) {
			formal.accept(this, context);
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, context);
		}
		*/
		return null;
	}

	@Override
	public Object visit(Formal formal, Enviroment context) {
		context.update(formal.getName(), (Type)formal.getType().accept(this, context));
		return null;
	}

	@Override
	public Object visit(PrimitiveType type, Enviroment context) {
		return CompilerUtils.primitiveTypeToMyType(type);
	}

	@Override
	public Object visit(UserType type, Enviroment context) {
		return CompilerUtils.userTypeToMyType(type);
	}

	@Override
	public Object visit(Assignment assignment, Enviroment context) {
		System.out.println("   -> Assignment");
		Expression rhs = assignment.getAssignment();
		Location lhs = assignment.getVariable();
		
		Object expressionValue = rhs.accept(this, context);
		Object variable = lhs.accept(this, context);
		
		System.out.println(">>> lhs: " + variable);
		System.out.println(">>> rhs: " + expressionValue);
		
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement, Enviroment context) {
		System.out.println("    -> CallStatement");
		return callStatement.getCall().accept(this, context);
	}

	@Override
	public Object visit(Return returnStatement, Enviroment context) {
		System.out.println("    -> ReturnStatement");
		return returnStatement.getValue();
	}

	@Override
	public Object visit(If ifStatement, Enviroment context) {
		System.out.println("- - - - - ifStatement - - - - -");
		if (ifStatement.getCondition().accept(this, context) != null) {
			System.out.println("if -> OK");
		}

		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this, context);
		}
		System.out.println("e - - - - ifStatement - - - - e");
		return null;
	}

	@Override
	public Object visit(While whileStatement, Enviroment context) {
		System.out.println("- - - - - whileStatement - - - - -");
		if (whileStatement.getCondition().accept(this, context) != null) {
			System.out.println("While -> OK");
		}
		
		whileStatement.getOperation().accept(this, context);
		System.out.println("e - - - - whileStatement - - - - e");
		return null;
	}

	@Override
	public Object visit(Break breakStatement, Enviroment context) {
		System.out.println("    -> BreakStatement");
		return null;
	}

	@Override
	public Object visit(Continue continueStatement, Enviroment context) {
		System.out.println("    -> ContinueStatement");
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, Enviroment context) {
		System.out.println("  -> StatementsBlock");
		
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable, Enviroment context) {
		System.out.println("    -> LocalVariable");
		context.update(localVariable.getName(),
				(Type)localVariable.getType().accept(this, context));
		
		return localVariable.getType().accept(this, context);
	}

	@Override
	public Object visit(VariableLocation location, Enviroment context) {
		System.out.println("    -> VariableLocation");
		
		if (location.getLocation() != null) {
			return location.getLocation().accept(this, context);
		}
		
		return context.get(location.getName());
	}

	@Override
	public Object visit(ArrayLocation location, Enviroment context) {
		System.out.println("    -> ArrayLocation");
		
		Type arrayType = (Type)location.getArray().accept(this, context);
		Type indexType = (Type)location.getIndex().accept(this, context);
		
		if (indexType.subtypeOf(TypeTable.intType)) {
			System.out.println("E :- e0:" + arrayType + "  E :- e1:" + indexType + 
					" => E :- e0[e1]:" + arrayType.getName());
			return arrayType;
		}
		
		return null;
	}

	@Override
	public Object visit(StaticCall call, Enviroment context) {
		System.out.println("    -> StaticCall");
		
		for (Expression expr : call.getArguments()) {
			expr.accept(this, context);
		}
		
		return context.get(call.getName());
	}

	@Override
	public Object visit(VirtualCall call, Enviroment context) {
		System.out.println("    -> VirtualCall");
		return context.get(call.getName());
	}

	@Override
	public Object visit(This thisExpression, Enviroment context) {
		System.out.println("    -> thisExpression");
		return null;
	}

	@Override
	public Object visit(NewClass newClass, Enviroment context) {
		System.out.println("    -> newClass ---- " + newClass.getName());
		return context.get(newClass.getName());
	}

	@Override
	public Object visit(NewArray newArray, Enviroment context) {
		System.out.println("    -> newArray");
		
		Type arrayType = (Type)newArray.getType().accept(this, context);
		Type sizeType = (Type)newArray.getSize().accept(this, context);
		
		if (sizeType.subtypeOf(TypeTable.intType)) {
			System.out.println("E :- e:" + sizeType + " => E :- T[e]:" + arrayType);
			return arrayType;
		}
		
		return null;
	}

	@Override
	public Object visit(Length length, Enviroment context) {
		System.out.println("    -> length");
		Type array = (Type)length.getArray().accept(this, context);
		
		TypeTable.getArrayType(array.getName());
		
		context.get(array.getName());
		
		return TypeTable.intType;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, Enviroment context) {
		System.out.println("- - - - MathBinaryOp - - - -");
		IC.TypeTable.Type result = null;
		IC.TypeTable.Type lhsVal = (IC.TypeTable.Type)binaryOp.getFirstOperand().accept(this, context);
		IC.TypeTable.Type rhsVal = (IC.TypeTable.Type)binaryOp.getSecondOperand().accept(this, context);
		
		System.out.println("lhsVal: " + lhsVal);
		System.out.println("rhsVal: " + rhsVal);
		
		if (lhsVal.subtypeOf(rhsVal)) {
			if (lhsVal.subtypeOf(TypeTable.stringType) && binaryOp.getOperator() == BinaryOps.PLUS) {
				result = TypeTable.stringType;
				System.out.println("E :- e0:" + lhsVal + "   E :- e1:" + rhsVal + "  op:" + binaryOp.getOperator() + " => E :- e0 op e1:" + result);
				System.out.println("TRUE");
			} else if (lhsVal.subtypeOf(TypeTable.intType)) {
				result = TypeTable.intType;
				System.out.println("E :- e0:" + lhsVal + "   E :- e1:" + rhsVal + "  op:" + binaryOp.getOperator() + " => E :- e0 op e1:" + result);
				System.out.println("TRUE");
			} else {
				System.out.println("FALSE");
			}
		} else {
			System.out.println("FALSE");
		}
		
		
	
		System.out.println("e - - - MathBinaryOp - - - e");
		return result;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, Enviroment context) {
		System.out.println("- - - - LogicalBinaryOp - - - -");
		IC.TypeTable.Type result = null;
		IC.TypeTable.Type lhsVal = (IC.TypeTable.Type)binaryOp.getFirstOperand().accept(this, context);
		IC.TypeTable.Type rhsVal = (IC.TypeTable.Type)binaryOp.getSecondOperand().accept(this, context);
		BinaryOps op = binaryOp.getOperator();
		
		System.out.println("lhsVal: " + lhsVal + "  " + lhsVal.getClass());
		System.out.println("rhsVal: " + rhsVal + "  " + rhsVal.getClass());
		System.out.println("Op: " + binaryOp.getOperator());
		
		if (lhsVal.subtypeOf(rhsVal) || rhsVal.subtypeOf(lhsVal)) {
			if (binaryOp.getOperator() == BinaryOps.EQUAL
					|| binaryOp.getOperator() == BinaryOps.NEQUAL) {
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
					System.out.println("FALSE");
				}
			}
		} else {
			System.out.println("FALSE");
		}
		System.out.println("e - - - LogicalBinaryOp - - - e");
		return result;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, Enviroment context) {
		System.out.println("- - - - MathUnaryOp - - - -");
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
		
		System.out.println("e - - - MathUnaryOp - - - e");
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, Enviroment context) {
		System.out.println("- - - - LogicalUnaryOp - - - -");
		UnaryOps op = unaryOp.getOperator();
		Expression expr = unaryOp.getOperand();
		
		Type exprType = (Type) expr.accept(this, context);
		
		if (op != UnaryOps.UMINUS) {
			throw new RuntimeException("Encountered unexpected operator " + op);
		}

		if (exprType.subtypeOf(TypeTable.boolType)) {
			System.out.println("E :- e:" + exprType + " => E :- !e:" + exprType);
			System.out.println("e - - - LogicalUnaryOp - - - e");
			return unaryOp.getOperand().accept(this, context);	
		}
		
		System.out.println("e - - - LogicalUnaryOp - - - e");
		return null;
	}

	@Override
	public Object visit(Literal literal, Enviroment context) {
		return CompilerUtils.literalTypeToMyType(literal.getType());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, Enviroment context) {
		Object expr = expressionBlock.getExpression().accept(this, context);
		return expr;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, Enviroment context) {
		return null;
	}

}
