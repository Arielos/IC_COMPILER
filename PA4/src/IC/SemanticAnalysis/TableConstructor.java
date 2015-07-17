package IC.SemanticAnalysis;

import java.util.ArrayList;
import java.util.List;

import IC.CompilerUtils;
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
import IC.TypeTable.ClassType;
import IC.TypeTable.MethodType;
import IC.TypeTable.MethodType.VirtualOrStatic;
import IC.TypeTable.Type;
import IC.TypeTable.TypeTable;

/**
 * Table Constructor visitor - travels along the AST and build the scope symbol tables
 */
public class TableConstructor implements PropagatingVisitor<SymbolTable, Object> {

	private String ICFilePath;
	private ASTNode root;
	
	private StringBuffer errors;
	private boolean didIFoundMainMethod;

	public TableConstructor(String path, ASTNode root) {
		this.ICFilePath = path.substring(path.lastIndexOf("\\") + 1);
		this.root = root;
		this.didIFoundMainMethod = false;
		errors = new StringBuffer();
	}

	public ASTNode construct() {
		ASTNode resultNode = null;
		try {
			resultNode = (ASTNode) root.accept(this, null);

            if (!didIFoundMainMethod) {
                errors.append("Main method not found, please define the main method as: static void main(string[] args)");
            }

            if (errors.length() > 0) {
                System.err.println(errors.toString());
            }

		} catch (Exception ex) {
            System.err.println(ex.getMessage());
        }

		return resultNode;
	}

	@Override
	public Object visit(Program program, SymbolTable scope) throws Exception {
		SymbolTable globalTable = new GlobalSymbolTable("Global", ICFilePath);

		globalTable.setParentSymbolTable(null);
		
		for (ICClass icClass : program.getClasses()) {
			globalTable.insert(icClass.getName(),
					(SymbolTableRow) icClass.accept(this, globalTable));
		}
		
		for (ICClass icClass : program.getClasses()) {
			if (icClass.hasSuperClass()) {
				for (ICClass icClassOther : program.getClasses()) {
					if (icClass.getSuperClassName().equals(icClassOther.getName())) {
						icClass.enclosingScope().setParentSymbolTable(icClassOther.enclosingScope());
						icClassOther.enclosingScope().addChild(icClass.enclosingScope());
						break;
					}
				}
			} else {
				globalTable.addChild(icClass.enclosingScope());
			}
		}
		
		program.setEnclosingScope(globalTable);
		
		globalTable.AddUniqueTypes();
		
		return null;
	}

	@Override
	public Object visit(ICClass icClass, SymbolTable scope) throws Exception {
		SymbolTableRow currentSymbol;

		SymbolTable classTable = new ClassSymbolTable(icClass.getName());
		ClassType classType = new ClassType(icClass);
		
		for (Field field : icClass.getFields()) {
			currentSymbol = (SymbolTableRow) field.accept(this, classTable);
			
			try {
				classTable.insert(field.getName(), currentSymbol);
			} catch (SemanticError err) {
				errors.append(err.getMessage() + " at line " + field.getLine() + "\n");
			}
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
		
		TypeTable.addClassType(classType);
		classTable.AddUniqueTypes();
		
		return new SymbolTableRow(icClass.getName(), classType, Kind.CLASS);
	}

	@Override
	public Object visit(Field field, SymbolTable scope) throws Exception {
		SymbolTable fieldTable = new VarSymbolTable(field.getName());

		Type fieldType = (Type)field.getType().accept(this, scope);

		fieldTable.setParentSymbolTable(scope);
		field.setEnclosingScope(fieldTable);

		return new SymbolTableRow(field.getName(), fieldType, Kind.FIELD);
	}

	@Override
	public Object visit(VirtualMethod method, SymbolTable scope) throws Exception {
		SymbolTableRow currentSymbol;
		SymbolTable methodTable = new MethodSymbolTable(method.getName());

		methodTable.setParentSymbolTable(scope);

		List<Type> paramTypes = new ArrayList<>();
		
		if (method.getName().equals("main")) {
			errors.append("Main method not found, please define the main method as: static void main(string[] args)");
		}
		
		for (Formal formal : method.getFormals()) {
			currentSymbol = (SymbolTableRow) formal.accept(this, methodTable);
			paramTypes.add(currentSymbol.getType());
		}
		
		if (method.getStatements().size() > 0) {
			for (Statement stmt : method.getStatements()) {
				stmt.accept(this, methodTable);
			}
		}

		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType(method.getName(),
				VirtualOrStatic.Virtual, paramTypes, methodReturnType);
		
		Type thisType = TypeTable.getClassType(scope.getId());
		methodTable.insert("$this", new SymbolTableRow("$this", thisType, Kind.THIS));
		methodTable.insert("$ret", new SymbolTableRow("$ret_type", methodReturnType, Kind.RET_VAR));
		
		method.setEnclosingScope(methodTable);

		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(StaticMethod method, SymbolTable scope) throws Exception {
		SymbolTableRow currentSymbol;
		SymbolTable methodTable = new MethodSymbolTable(method.getName());

		methodTable.setParentSymbolTable(scope);
		
		List<Type> paramTypes = new ArrayList<>();

		for (Formal formal : method.getFormals()) {
			currentSymbol = (SymbolTableRow) formal.accept(this, methodTable);
			paramTypes.add(currentSymbol.getType());
		}

		for (Statement stmt : method.getStatements()) {
			stmt.accept(this, methodTable);
		}

		if (method.getName().equals("main")) {
			Formal mainFormal; 
			if (method.getFormals().size() == 1) {
				mainFormal = method.getFormals().get(0);
				boolean isOneDimenstion = mainFormal.getType().getDimension() == 1; 
				boolean isString = mainFormal.getType().getName().equals("string");
				
				if (isOneDimenstion && isString) {
					didIFoundMainMethod = true;
				}
			}
		}
		
		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType(method.getName(),
				VirtualOrStatic.Static, paramTypes, methodReturnType);
		
		methodTable.insert("$ret", new SymbolTableRow("$ret_type", methodReturnType, Kind.RET_VAR));
		
		method.setEnclosingScope(methodTable);
		
		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(LibraryMethod method, SymbolTable scope) throws Exception {
		SymbolTableRow currentSymbol;
		SymbolTable libMethodTable = new MethodSymbolTable(method.getName());

		libMethodTable.setParentSymbolTable(scope);

		List<Type> paramTypes = new ArrayList<>();

		for (Formal formal : method.getFormals()) {
			currentSymbol = (SymbolTableRow) formal.accept(this, libMethodTable);
			paramTypes.add(currentSymbol.getType());
		}
		
		Type methodReturnType = (Type)method.getType().accept(this, scope);
		MethodType methodType = new MethodType(method.getName(),
				VirtualOrStatic.Static, paramTypes, methodReturnType);

		method.setEnclosingScope(libMethodTable);

		//TypeTable.addMethodType(method.getName(), methodType);

		return new SymbolTableRow(method.getName(), methodType, Kind.METHOD);
	}

	@Override
	public Object visit(Formal formal, SymbolTable scope) throws Exception {
		Type formalType = (Type)formal.getType().accept(this, scope);
		
		SymbolTableRow formalSymbol = new SymbolTableRow(formal.getName(),
					formalType, Kind.PARAM);
		
		scope.insert(formal.getName(), formalSymbol);

		return formalSymbol;
	}

	@Override
	public Object visit(PrimitiveType type, SymbolTable scope) throws Exception {
		return CompilerUtils.primitiveTypeToMyType(type);
	}

	@Override
	public Object visit(UserType type, SymbolTable scope) throws Exception {
		return CompilerUtils.userTypeToMyType(type);
	}

	@Override
	public Object visit(If ifStatement, SymbolTable scope) throws Exception {
		// System.out.println("------------------------- if");

		ifStatement.getOperation().accept(this, scope);
		
		if (ifStatement.hasElse()) {
			// System.out.println("------------------------- else");
			ifStatement.getElseOperation().accept(this, scope);
		}
		// System.out.println("------------------------- end if");
		return null;
	}

	@Override
	public Object visit(While whileStatement, SymbolTable scope) throws Exception {
		// System.out.println("------------------------- while");
		return whileStatement.getOperation().accept(this, scope);
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, SymbolTable scope) throws Exception {
		// System.out.println("------------------------- statemnts block");

		SymbolTable table = new StatementBlockSymbolTable("statement block in " 
				+ scope.getId());

		table.setParentSymbolTable(scope);
		
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
	public Object visit(LocalVariable localVariable, SymbolTable scope) throws Exception {
		Type varType = (Type)localVariable.getType().accept(this, scope);

		SymbolTableRow sym = new SymbolTableRow(localVariable.getName(),
				varType, Kind.VAR);
		scope.insert(sym.getId(), sym);

		return sym;
	}

	@Override
	public Object visit(Assignment assignment, SymbolTable context) throws Exception {
		//System.out.println("---- assignment");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement, SymbolTable context) throws Exception {
		//System.out.println("---- callStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement, SymbolTable context) throws Exception {
		//System.out.println("---- returnStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Break breakStatement, SymbolTable context) throws Exception {
		//System.out.println("---- breakStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement, SymbolTable context) throws Exception {
		//System.out.println("---- continueStatement");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VariableLocation location, SymbolTable context) throws Exception {
		//System.out.println("---- VariableLocation");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location, SymbolTable context) throws Exception {
		//System.out.println("---- ArrayLocation");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call, SymbolTable context) throws Exception {
		//System.out.println("---- StaticCall");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call, SymbolTable context) throws Exception {
		//System.out.println("---- VirtualCall");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression, SymbolTable context) throws Exception {
		//System.out.println("---- thisExpression");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass, SymbolTable context) throws Exception {
		//System.out.println("---- NewClass");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray, SymbolTable context) throws Exception {
		//System.out.println("---- NewArray");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length, SymbolTable context) throws Exception {
		//System.out.println("---- Length");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, SymbolTable context) throws Exception {
		//System.out.println("---- MathBinaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, SymbolTable context) throws Exception {
		//System.out.println("---- LogicalBinaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, SymbolTable context) throws Exception {
		//System.out.println("---- MathUnaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, SymbolTable context) throws Exception {
		//System.out.println("---- LogicalUnaryOp");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal, SymbolTable context) throws Exception {
		//System.out.println("---- Literal");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, SymbolTable context) throws Exception {
		//System.out.println("---- ExpressionBlock");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, SymbolTable context) throws Exception {
		//System.out.println("---- FieldOrMethod");
		// TODO Auto-generated method stub
		return null;
	}
}