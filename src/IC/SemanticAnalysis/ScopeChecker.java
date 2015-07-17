package IC.SemanticAnalysis;

import java.util.Map.Entry;

import IC.AST.ASTNode;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
import IC.AST.Expression;
import IC.AST.ExpressionBlock;
import IC.AST.Field;
import IC.AST.FieldOrMethod;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.Length;
import IC.AST.LibraryMethod;
import IC.AST.Literal;
import IC.AST.LocalVariable;
import IC.AST.LogicalBinaryOp;
import IC.AST.LogicalUnaryOp;
import IC.AST.MathBinaryOp;
import IC.AST.MathUnaryOp;
import IC.AST.Method;
import IC.AST.NewArray;
import IC.AST.NewClass;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.PropagatingVisitor;
import IC.AST.Return;
import IC.AST.Statement;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.While;
import IC.TypeTable.ClassType;
import IC.TypeTable.MethodType;
import IC.TypeTable.TypeTable;

public class ScopeChecker implements PropagatingVisitor<SymbolTable, Object>,
		Tester {

	private StringBuffer errors;
	private int loopCounter;
	private ASTNode root;

	public ScopeChecker(ASTNode root) {
		this.root = root;
		errors = new StringBuffer();
	}

	@Override
	public boolean isAllGood() {
		return errors.length() == 0;
	}

	@Override
	public void test() throws Exception {
		loopCounter = 0;
		root.accept(this, null);
	}

    public String getErrors() {
        return errors.toString();
    }

	@Override
	public Object visit(ArrayLocation location, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return location.getArray().accept(this, context);
	}

	@Override
	public Object visit(Assignment assignment, SymbolTable context)
			throws Exception {
		assignment.getVariable().accept(this, context);
		assignment.getAssignment().accept(this, context);
		return null;
	}

	@Override
	public Object visit(Break breakStatement, SymbolTable context)
			throws Exception {
		if (loopCounter == 0) {
			errors.append("SemanticError: 'break' must be inside a loop at line "
					+ breakStatement.getLine() + "\n");
		}

		return null;
	}

	@Override
	public Object visit(CallStatement callStatement, SymbolTable context)
			throws Exception {
		callStatement.getCall().accept(this, context);
		return null;
	}

	@Override
	public Object visit(Continue continueStatement, SymbolTable context)
			throws Exception {
		if (loopCounter == 0) {
			errors.append("SemanticError: 'continue' must be inside a loop at line "
					+ continueStatement.getLine() + "\n");
		}

		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, SymbolTable context)
			throws Exception {
		expressionBlock.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Object visit(Field field, SymbolTable context) throws Exception {
		int counter = 0;

		for (Entry<String, SymbolTableRow> entry : context.getIterator()) {
			if (entry.getKey().equals(field.getName())) {
				counter++;
			}
		}

		if (counter > 1) {
			errors.append("Error");
		}

		return null;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Formal formal, SymbolTable context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ICClass icClass, SymbolTable context) throws Exception {
		SymbolTable myTable = icClass.enclosingScope();
		SymbolTable parentTable = myTable.getParentSymbolTable();
		SymbolTable myParentParentTable;

		if (myTable.getId().equals(parentTable.getId())) {
			errors.append("SemanticError: cyclic inheritance detected! at line "
					+ icClass.getLine() + "\n");
		} else {
			while (parentTable.getParentSymbolTable() != null) {
				myParentParentTable = parentTable.getParentSymbolTable();

				if (myTable.getId().equals(parentTable.getId())) {
					errors.append("SemanticError: cyclic inheritance detected! at line "
							+ icClass.getLine() + "\n");
					break;
				} else {
					if (parentTable.getId().equals(myParentParentTable.getId())) {
						errors.append("SemanticError: cyclic inheritance detected! at line "
								+ icClass.getLine() + "\n");
						break;
					} else {
						parentTable = parentTable.getParentSymbolTable();
					}
				}
			}
		}

		for (Field field : icClass.getFields()) {
			field.accept(this, myTable);
		}

		for (Method method : icClass.getMethods()) {
			method.accept(this, myTable);
		}

		return null;
	}

	@Override
	public Object visit(If ifStatement, SymbolTable context) throws Exception {
		ifStatement.getCondition().accept(this, context);
		ifStatement.getOperation().accept(this, context);

		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(Length length, SymbolTable context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LibraryMethod method, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal, SymbolTable context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable, SymbolTable context)
			throws Exception {
		// System.out.println("----- LocalVariable: " +
		// localVariable.getName());

		if (localVariable.hasInitValue()) {
			localVariable.getInitValue().accept(this, context);
		}

		if (context.lookup(localVariable.getName()) == null) {
			errors.append("SemanticError: undefined local variable '"
					+ localVariable.getName() + "' at line "
					+ localVariable.getLine() + "\n");
		}

		return localVariable.getName();
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, SymbolTable context)
			throws Exception {

		binaryOp.getFirstOperand().accept(this, context);
		binaryOp.getSecondOperand().accept(this, context);

		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, SymbolTable context)
			throws Exception {
		unaryOp.getOperand().accept(this, context);
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, SymbolTable context)
			throws Exception {
		binaryOp.getFirstOperand().accept(this, context);
		binaryOp.getSecondOperand().accept(this, context);

		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, SymbolTable context)
			throws Exception {
		unaryOp.getOperand().accept(this, context);
		return null;
	}

	@Override
	public Object visit(NewArray newArray, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(PrimitiveType type, SymbolTable context)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Program program, SymbolTable context) throws Exception {
		for (ICClass icClass : program.getClasses()) {
			icClass.accept(this, program.enclosingScope());
		}

		return null;
	}

	@Override
	public Object visit(Return returnStatement, SymbolTable context)
			throws Exception {
		if (returnStatement.hasValue()) {
			returnStatement.getValue().accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, SymbolTable context)
			throws Exception {
		// System.out.println("----- StatementsBlock");
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this, statementsBlock.enclosingScope());
		}

		return null;
	}

	@Override
	public Object visit(StaticCall call, SymbolTable context) throws Exception {
		for (Expression expr : call.getArguments()) {
			expr.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(StaticMethod method, SymbolTable context)
			throws Exception {
		SymbolTable myScope = method.enclosingScope();

		for (Statement stmts : method.getStatements()) {
			stmts.accept(this, myScope);
		}

		return null;
	}

	@Override
	public Object visit(This thisExpression, SymbolTable context)
			throws Exception {
		SymbolTable thisScope = context;

		while (thisScope.getKind() == Kind.BLOCK) {
			thisScope = thisScope.getParentSymbolTable();
		}

		if (((MethodType) thisScope.getType()).isStatic()) {
			errors.append("SemanticError: 'this' must be only in virtual methods at line "
					+ thisExpression.getLine() + "\n");
		}

		return "$this";
	}

	@Override
	public Object visit(UserType type, SymbolTable context) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location, SymbolTable context)
			throws Exception {
		//System.out.println("----- VariableLocation");
		if (location.isExternal()) {
			String external = (String) location.getLocation().accept(this, context);

			ClassType classType = TypeTable.getClassType(context.lookup(external).getType().getName());
			
			if (classType.getClassAST().enclosingScope().lookup(location.getName()) == null) {
				errors.append("SemanticError: undefined variable '"
						+ location.getName() + "' at line " + location.getLine()
						+ "\n");
			}
		} else {
            if (context.lookup(location.getName()) == null) {
				errors.append("SemanticError: undefined variable '"
						+ location.getName() + "' at line " + location.getLine()
						+ "\n");
			}
		}

		return location.getName();
	}

	@Override
	public Object visit(VirtualCall call, SymbolTable context) throws Exception {
		if (call.isExternal()) {
			call.getLocation().accept(this, context);
		} else {
			if (context.lookup(call.getName()) == null) {
				errors.append("SemanticError: undefined method '"
						+ call.getName() + "' at line " + call.getLine() + "\n");
			}
		}

		for (Expression expr : call.getArguments()) {
			expr.accept(this, context);
		}

		return null;
	}

	@Override
	public Object visit(VirtualMethod method, SymbolTable context)
			throws Exception {
		SymbolTable myScope = method.enclosingScope();

		for (Statement stmts : method.getStatements()) {
			stmts.accept(this, myScope);
		}

		return null;
	}

	@Override
	public Object visit(While whileStatement, SymbolTable context)
			throws Exception {
		whileStatement.getCondition().accept(this, context);

		loopCounter += 1;
		whileStatement.getOperation().accept(this, context);
		loopCounter -= 1;

		return null;
	}
}