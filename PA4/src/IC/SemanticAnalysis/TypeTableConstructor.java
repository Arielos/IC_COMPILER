package IC.SemanticAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import IC.CompilerUtils;
import IC.AST.*;
import IC.TypeTable.*;
import IC.TypeTable.MethodType.VirtualOrStatic;
import IC.TypeTable.Type;

public class TypeTableConstructor implements Visitor {

	private ASTNode root;
	private Map<String, ClassType> classTypesMap;
	
	public TypeTableConstructor(ASTNode root) {
		this.root = root;
		classTypesMap = new HashMap<String, ClassType>();
	}
	
	public void run() {
		root.accept(this);
	}

	@Override
	public Object visit(Program program) {

        List<ICClass> classes = program.getClasses();

		for (ICClass icClass : classes) {
			classTypesMap.put(icClass.getName(), new ClassType(icClass));
		}
		
		for (ICClass icClass : classes) {
			icClass.accept(this);
		}
		
		for (ICClass icClass : classes) {
			if (icClass.hasSuperClass()) {
				ClassType currentClass = TypeTable.getClassType(icClass.getName());
				ClassType superClass = TypeTable.getClassType(icClass.getSuperClassName());
				currentClass.setSuperClass(superClass);
			}
		}
		
		return null;
	}

	@Override
	public Object visit(ICClass icClass) {
		Type currentType;
		ClassType classType = new ClassType(icClass);
		TypeTable.addClassType(classType);
		
		for (Field field : icClass.getFields()) {
			currentType = (Type)field.getType().accept(this);
			
			if (currentType instanceof ArrayType) {
				TypeTable.addArrayType((ArrayType)currentType);
			}
		}
		
		for (Method method : icClass.getMethods()) {
			method.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(Field field) {
		return field.getType().accept(this);
	}

	@Override
	public Object visit(VirtualMethod method) {
		Type retType;
		List<Type> params = new ArrayList<>();
		
		Type currentType;
		for (Formal param : method.getFormals()) {
			currentType = (Type)param.accept(this);
			params.add(currentType);
			
			if (currentType instanceof ArrayType) {
				TypeTable.addArrayType((ArrayType)currentType);
			}
		}
		
		retType = (Type)method.getType().accept(this);
		
		if (retType instanceof ArrayType) {
			TypeTable.addArrayType((ArrayType)retType);
		}
		
		MethodType methodType = new MethodType(method.getName(), VirtualOrStatic.Virtual, params, retType);
		TypeTable.addMethodType(method.getName(), methodType);
		
		for (Statement stmt : method.getStatements()) {
			stmt.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		Type retType = null;
		List<Type> params = new ArrayList<Type>();
		
		Type currentType = null;
		for (Formal param : method.getFormals()) {
			currentType = (Type)param.accept(this);
			params.add(currentType);
					
			if (currentType instanceof ArrayType) {
				TypeTable.addArrayType((ArrayType)currentType);
			}
		}
		
		retType = (Type)method.getType().accept(this);
		
		if (retType instanceof ArrayType) {
			TypeTable.addArrayType((ArrayType)retType);
		}
		
		MethodType methodType = new MethodType(VirtualOrStatic.Static, params, retType);
		
		//System.out.println("method.getName(): " + method.getName());
		//System.out.println("methodType.getName(): " + methodType.getName());
		
		TypeTable.addMethodType(method.getName(), methodType);
		
		for (Statement stmt : method.getStatements()) {
			stmt.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		Type retType = null;
		List<Type> params = new ArrayList<Type>();
		
		Type currentType = null;
		for (Formal param : method.getFormals()) {
			currentType = (Type)param.accept(this);
			params.add(currentType);
			
			if (currentType instanceof ArrayType) {
				TypeTable.addArrayType((ArrayType)currentType);
			}
		}
		
		retType = (Type)method.getType().accept(this);
		
		if (retType instanceof ArrayType) {
			TypeTable.addArrayType((ArrayType)retType);
		}
		
		MethodType methodType = new MethodType(VirtualOrStatic.Static, params, retType);
		
		//System.out.println("method.getName(): " + method.getName());
		//System.out.println("methodType.getName(): " + methodType.getName());
		
		TypeTable.addMethodType(method.toString(), methodType);
		
		for (Statement stmt : method.getStatements()) {
			stmt.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		return formal.getType().accept(this);
	}

	@Override
	public Object visit(PrimitiveType type) {
		Type myType = CompilerUtils.primitiveTypeToMyType(type);
		return myType;
	}

	@Override
	public Object visit(UserType type) {
		String varTypeName = type.getName();
		return classTypesMap.get(varTypeName);
	}

	@Override
	public Object visit(Assignment assignment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		ifStatement.getOperation().accept(this);
		
		if (ifStatement.hasElse()) {
			ifStatement.getElseOperation().accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		whileStatement.getOperation().accept(this);
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		for (Statement stmt : statementsBlock.getStatements()) {
			stmt.accept(this);
		}
		
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		return localVariable.getType().accept(this);
	}

	@Override
	public Object visit(VariableLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Length length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return CompilerUtils.literalTypeToMyType(literal.getType());
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod) {
		// TODO Auto-generated method stub
		return null;
	}
}