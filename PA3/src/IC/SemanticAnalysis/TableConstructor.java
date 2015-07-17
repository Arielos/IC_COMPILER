package IC.SemanticAnalysis;

import IC.CompilerUtils;
import IC.DataTypes;
import IC.AST.ASTNode;
import IC.AST.ArrayLocation;
import IC.AST.Assignment;
import IC.AST.Break;
import IC.AST.CallStatement;
import IC.AST.Continue;
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
import IC.Parser.SyntaxError;
import IC.TypeTable.ArrayType;
import IC.TypeTable.BoolType;
import IC.TypeTable.ClassType;
import IC.TypeTable.IntType;
import IC.TypeTable.MethodType;
import IC.TypeTable.StringType;
import IC.TypeTable.Type;
import IC.TypeTable.TypeTable;
import IC.TypeTable.UserDefType;

/**
 * Table Constructor visitor - travels along the AST and build the scope symbol
 * tables
 */
public class TableConstructor implements
		PropagatingVisitor<SymbolTable, Object> {

	private String ICFilePath;
	private ASTNode root;

	public TableConstructor(String path, ASTNode root) {
		this.ICFilePath = path.substring(path.lastIndexOf("\\") + 1);
		this.root = root;
	}

	public ASTNode construct() {
		return (ASTNode) root.accept(this, null);
	}

	@Override
	public Object visit(Program program, SymbolTable scope) {
		SymbolTable globalTable = new GlobalSymbolTable("Global");

		globalTable.setParentSymbolTable(null);

		for (ICClass icClass : program.getClasses()) {
			globalTable.insert(icClass.getName(),
					(SymbolTableRow) icClass.accept(this, globalTable));

			if (icClass.hasSuperClass()) {
				for (ICClass icClassOther : program.getClasses()) {
					if (icClass.getSuperClassName().equals(
							icClassOther.getName())) {
						icClass.enclosingScope().setParentSymbolTable(
								icClassOther.enclosingScope());
						icClassOther.enclosingScope().addChild(
								icClass.enclosingScope());
						break;
					}
				}
			} else {
				globalTable.addChild(icClass.enclosingScope());
			}
		}

		program.setEnclosingScope(globalTable);
		
		return null;
	}

	@Override
	public Object visit(ICClass icClass, SymbolTable scope) {
		SymbolTableRow currentSymbol = null;

		SymbolTable classTable = new ClassSymbolTable(icClass.getName());
		TypeTable.addClassType(new ClassType(icClass));
		
		for (Field field : icClass.getFields()) {
			currentSymbol = (SymbolTableRow) field.accept(this, classTable);
			classTable.insert(field.getName(), currentSymbol);
		}

		for (Method method : icClass.getMethods()) {
			currentSymbol = (SymbolTableRow) method.accept(this, classTable);
			classTable.insert(method.getName(), currentSymbol);
			classTable.addChild(method.enclosingScope());
		}

		if (!icClass.hasSuperClass()) {
			classTable.setParentSymbolTable(scope);
		}

		icClass.setEnclosingScope(classTable);
		
		return new SymbolTableRow(icClass.getName(),
				TypeTable.getClassType(classTable.getId()),
				Kind.CLASS);
	}

	@Override
	public Object visit(Field field, SymbolTable scope) {
		SymbolTable fieldTable = new VarSymbolTable(field.getName());

		Type fieldType = (Type)field.getType().accept(this, scope);

		fieldTable.setParentSymbolTable(scope);
		field.setEnclosingScope(fieldTable);

		return new SymbolTableRow(field.getName(), fieldType, Kind.FIELD);
	}

	@Override
	public Object visit(VirtualMethod method, SymbolTable scope) {
		SymbolTableRow currentSymbol = null;
		SymbolTable methodTable = new MethodSymbolTable(method.getName());

		methodTable.setParentSymbolTable(scope);

		int i = 0;
		Type[] paramTypes;
		paramTypes = new Type[method.getFormals().size()];

		if (method.getName().equals("main")) {
			System.err.println("Main must be static");
			//throw new SyntaxError("Main error");
		}
		
		if (method.getFormals().size() > 0) {
			for (Formal formal : method.getFormals()) {
				currentSymbol = (SymbolTableRow) formal.accept(this,
						methodTable);
				paramTypes[i++] = currentSymbol.getType();
			}
		}

		if (method.getStatements().size() > 0) {
			for (Statement stmt : method.getStatements()) {
				stmt.accept(this, methodTable);
			}
		}

		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType("Virtual", paramTypes,
				methodReturnType);
		
		Type thisType = TypeTable.getClassType(scope.getId());
		methodTable.insert("$this", new SymbolTableRow("$this", thisType, Kind.THIS));
		methodTable.insert("$ret", new SymbolTableRow("$ret_type", methodReturnType, Kind.RET_VAR));
		
		method.setEnclosingScope(methodTable);

		TypeTable.addMethodType(method.getName(), methodType);

		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(StaticMethod method, SymbolTable scope) {
		SymbolTableRow currentSymbol = null;
		SymbolTable methodTable = new MethodSymbolTable(method.getName());

		methodTable.setParentSymbolTable(scope);
		
		int i = 0;
		Type[] paramTypes;
		paramTypes = new Type[method.getFormals().size()];

		for (Formal formal : method.getFormals()) {
			currentSymbol = (SymbolTableRow) formal.accept(this, methodTable);
			paramTypes[i++] = currentSymbol.getType();
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, methodTable);
		}

		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType("Static", paramTypes,
				methodReturnType);
		
		methodTable.insert("$ret", new SymbolTableRow("$ret_type", methodReturnType, Kind.RET_VAR));
		
		method.setEnclosingScope(methodTable);

		TypeTable.addMethodType(method.getName(), methodType);

		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(LibraryMethod method, SymbolTable scope) {
		// System.out.println("------------------------- library method \n");

		SymbolTableRow currentSymbol = null;
		SymbolTable libMethodTable = new MethodSymbolTable(method.getName());

		libMethodTable.setParentSymbolTable(scope);

		int i = 0;
		Type[] paramTypes;
		paramTypes = new Type[method.getFormals().size()];

		for (Formal formal : method.getFormals()) {
			currentSymbol = (SymbolTableRow) formal
					.accept(this, libMethodTable);
			paramTypes[i++] = currentSymbol.getType();
		}
		
		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType("Static", paramTypes,
				methodReturnType);

		method.setEnclosingScope(libMethodTable);

		TypeTable.addMethodType(method.getName(), methodType);

		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(Formal formal, SymbolTable scope) {
		Type formalType = (Type)formal.getType().accept(this, scope);
		
		SymbolTableRow formalSymbol = new SymbolTableRow(formal.getName(),
					formalType, Kind.PARAM);
		
		scope.insert(formal.getName(), formalSymbol);

		return formalSymbol;
	}

	@Override
	public Object visit(PrimitiveType type, SymbolTable scope) {
		return CompilerUtils.primitiveTypeToMyType(type);
	}

	@Override
	public Object visit(UserType type, SymbolTable scope) {
		return CompilerUtils.userTypeToMyType(type);
	}

	@Override
	public Object visit(If ifStatement, SymbolTable scope) {
		// System.out.println("------------------------- if");

		/*SymbolTableRow returnedSymbol = (SymbolTableRow)*/ ifStatement
				.getOperation().accept(this, scope);

		//if (returnedSymbol != null) {
			//scope.insert(returnedSymbol.getId(), returnedSymbol);
		//}

		if (ifStatement.hasElse()) {
			// System.out.println("------------------------- else");
			/*returnedSymbol = (SymbolTableRow)*/ ifStatement.getElseOperation()
					.accept(this, scope);

			//if (returnedSymbol != null) {
				//scope.insert(returnedSymbol.getId(), returnedSymbol);
			//}
		}
		// System.out.println("------------------------- end if");
		return null;
	}

	@Override
	public Object visit(While whileStatement, SymbolTable scope) {
		// System.out.println("------------------------- while");
		return whileStatement.getOperation().accept(this, scope);
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, SymbolTable scope) {
		// System.out.println("------------------------- statemnts block");

		SymbolTable table = new StatementBlockSymbolTable("statement block in "
				+ scope.getId());

		table.setParentSymbolTable(scope);

		System.out.println("stmt: " + table.getId());
		
		for (Statement stmt : statementsBlock.getStatements()) {
			if (stmt.accept(this, table) != null) {
				if (stmt.enclosingScope() != null) {
					table.addChild(stmt.enclosingScope());
				}
			}
		}

		scope.addChild(table);
		statementsBlock.setEnclosingScope(table);

		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable, SymbolTable scope) {
		Type varType = (Type)localVariable.getType().accept(this, scope);

		SymbolTableRow sym = new SymbolTableRow(localVariable.getName(),
				varType, Kind.VAR);
		scope.insert(sym.getId(), sym);

		return sym;
	}

	@Override
	public Object visit(Assignment assignment, SymbolTable context) {
		System.out.println("---- assignment");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement, SymbolTable context) {
		System.out.println("---- callStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement, SymbolTable context) {
		System.out.println("---- returnStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement, SymbolTable context) {
		System.out.println("---- breakStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement, SymbolTable context) {
		System.out.println("---- continueStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location, SymbolTable context) {
		System.out.println("---- VariableLocation");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location, SymbolTable context) {
		System.out.println("---- ArrayLocation");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call, SymbolTable context) {
		System.out.println("---- StaticCall");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call, SymbolTable context) {
		System.out.println("---- VirtualCall");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression, SymbolTable context) {
		System.out.println("---- thisExpression");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass, SymbolTable context) {
		System.out.println("---- NewClass");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray, SymbolTable context) {
		System.out.println("---- NewArray");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length, SymbolTable context) {
		System.out.println("---- Length");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, SymbolTable context) {
		System.out.println("---- MathBinaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, SymbolTable context) {
		System.out.println("---- LogicalBinaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, SymbolTable context) {
		System.out.println("---- MathUnaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, SymbolTable context) {
		System.out.println("---- LogicalUnaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal, SymbolTable context) {
		System.out.println("---- Literal");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, SymbolTable context) {
		System.out.println("---- ExpressionBlock");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, SymbolTable context) {
		System.out.println("---- FieldOrMethod");
		// TODO Auto-generated method stub
		return null;
	}
}