package IC.AST;

import IC.LiteralTypes;
import IC.SemanticAnalysis.SymbolTable;

/**
 * Pretty printing visitor - travels along the AST and prints info about each
 * node, in an easy-to-comprehend format.
 * 
 * @author Tovi Almozlino
 */
public class PrettyPrinter implements PropagatingVisitor<SymbolTable, Object> {

	private int depth = 0; // depth of indentation

	private String ICFilePath;

	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public PrettyPrinter(String ICFilePath) {
		this.ICFilePath = ICFilePath;
	}

	public void print(ASTNode root) {
		System.out.print(root.accept(this, null) + "\n");
	}

	private void indent(StringBuffer output, ASTNode node) {
		output.append("\n");
		for (int i = 0; i < depth; ++i)
			output.append(" ");
		if (node != null)
			output.append(node.getLine() + ": ");
	}

	private void indent(StringBuffer output) {
		indent(output, null);
	}

	@Override
	public Object visit(Program program, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output);
		output.append("Abstract Syntax Tree: " + ICFilePath + "\n");
		for (ICClass icClass : program.getClasses())
			output.append(icClass.accept(this, program.enclosingScope()));
		return output.toString();
	}

	@Override
	public Object visit(ICClass icClass, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, icClass);
		output.append("Declaration of class: " + icClass.getName());
		if (icClass.hasSuperClass())
			output.append(", subclass of " + icClass.enclosingScope().getParentSymbolTable().getId());
		output.append(", Type: " + icClass.getName());
		output.append(", Symbol table: " + icClass.enclosingScope().getParentSymbolTable().getId());
		//output.append(", Symbol table: " + scope.getId());
		depth += 2;
		for (Field field : icClass.getFields())
			output.append(field.accept(this, icClass.enclosingScope()));
		for (Method method : icClass.getMethods())
			output.append(method.accept(this, icClass.enclosingScope()));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(PrimitiveType type, SymbolTable scope) {
		StringBuffer output = new StringBuffer();
		
		output.append(type.getName());
		if (type.getDimension() > 0) {
			for (int i = 0; i < type.getDimension(); i++) {
				output.append("[]");
			}
		}
		
		return output.toString();
	}

	@Override
	public Object visit(UserType type, SymbolTable scope) {
		StringBuffer output = new StringBuffer();
		
		output.append(type.getName());
		if (type.getDimension() > 0) {
			for (int i = 0; i < type.getDimension(); i++) {
				output.append("[]");
			}
		}
		
		return output.toString();
	}

	@Override
	public Object visit(Field field, SymbolTable scope) {
		StringBuffer output = new StringBuffer();
		
		indent(output, field);
		output.append("Declaration of field: " + field.getName());
		output.append(", Type: " + field.getType().getName());
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		return output.toString();
	}

	@Override
	public Object visit(LibraryMethod method, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of library method: " + method.getName());
		depth += 2;
		output.append(method.getType().accept(this, method.enclosingScope()));
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this, method.enclosingScope()));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(Formal formal, SymbolTable scope) {
		StringBuffer output = new StringBuffer();
		
		if (scope != null) {
			indent(output, formal);
			output.append("Parameter: " + formal.getName());
			++depth;
			output.append(", Type: " + formal.getType().accept(this, scope));
			output.append(", Symbol table: " + scope.getId());
			--depth;
		} else {
			output.append(formal.getType().accept(this, scope));
		}
		
		return output.toString();
	}

	@Override
	public Object visit(VirtualMethod method, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of virtual method: " + method.getName());
		output.append(", Type: {");
		
		if (method.getFormals().size() > 0) {
			output.append(method.getFormals().get(0).getType().getName());
			for (int i = 1; i < method.getFormals().size(); i++) {
				output.append(", " + method.getFormals().get(i).getType().getName());
			}
		}
		
		output.append(" -> " + method.getType().getName() + "}");
		output.append(", Symbol table: " + scope.getId());
		
		depth += 2;
		//indent(output, method);
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this, method.enclosingScope()));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this, method.enclosingScope()));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(StaticMethod method, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, method);
		output.append("Declaration of static method: " + method.getName());
		output.append(", Type: {");
		
		if (method.getFormals().size() > 0) {
			output.append(method.getFormals().get(0).getType().getName());
			for (int i = 1; i < method.getFormals().size(); i++) {
				output.append(", " + method.getFormals().get(i).getType().getName());
			}
		}
		
		output.append(" -> " + method.getType().getName() + "}");
		output.append(", Symbol table: " + scope.getId());
		
		depth += 2;
		//indent(output, method);
		for (Formal formal : method.getFormals())
			output.append(formal.accept(this, method.enclosingScope()));
		for (Statement statement : method.getStatements())
			output.append(statement.accept(this, method.enclosingScope()));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(Assignment assignment, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, assignment);
		output.append("Assignment statement");
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		output.append(assignment.getVariable().accept(this, scope));
		output.append(assignment.getAssignment().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(CallStatement callStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, callStatement);
		output.append("Method call statement");
		++depth;
		output.append(callStatement.getCall().accept(this, scope));
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(Return returnStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, returnStatement);
		output.append("Return statement");
		if (returnStatement.hasValue())
			output.append(", with return value");
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		if (returnStatement.hasValue()) {
			++depth;
			output.append(returnStatement.getValue().accept(this, scope));
			--depth;
		}
		
		return output.toString();
	}

	@Override
	public Object visit(If ifStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, ifStatement);
		output.append("If statement");
		if (ifStatement.hasElse())
			output.append(", with Else operation");
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		depth += 2;
		output.append(ifStatement.getCondition().accept(this, scope));
		output.append(ifStatement.getOperation().accept(this, scope));
		if (ifStatement.hasElse())
			output.append(ifStatement.getElseOperation().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(While whileStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, whileStatement);
		output.append("While statement");
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		output.append(whileStatement.getCondition().accept(this, scope));
		output.append(whileStatement.getOperation().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(Break breakStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, breakStatement);
		output.append("Break statement");
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		return output.toString();
	}

	@Override
	public Object visit(Continue continueStatement, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, continueStatement);
		output.append("Continue statement");
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		return output.toString();
	}

	@Override
	public Object visit(StatementsBlock statementsBlock, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, statementsBlock);
		output.append("Block of statements");
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		for (Statement statement : statementsBlock.getStatements())
			output.append(statement.accept(this, statementsBlock.enclosingScope()));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(LocalVariable localVariable, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, localVariable);
		output.append("Declaration of local variable: "
				+ localVariable.getName());
		if (localVariable.hasInitValue()) {
			output.append(", with initial value");
			++depth;
		}
		++depth;
		output.append(", Type: " + localVariable.getType().accept(this, scope));
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		if (localVariable.hasInitValue()) {
			output.append(localVariable.getInitValue().accept(this, scope));
			--depth;
		}
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(VariableLocation location, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to variable: " + location.getName());
		if (location.isExternal())
			output.append(", in external scope");
		if (scope != null) {
			output.append(", Type: " + scope.lookup(location.getName()).getType());
			output.append(", Symbol table: " + scope.getId());
		}
		if (location.isExternal()) {
			++depth;
			output.append(location.getLocation().accept(this, scope));
			--depth;
		}
		
		return output.toString();
	}

	@Override
	public Object visit(ArrayLocation location, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, location);
		output.append("Reference to array");
		
		// TODO: fix later
		if (scope != null) {
			if (location.getArray() instanceof VariableLocation) {
				output.append(", Type: " + scope.lookup(((VariableLocation)location.getArray()).getName()).getType());	
			}
			
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		output.append(location.getArray().accept(this, scope));
		output.append(location.getIndex().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(StaticCall call, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to static method: " + call.getName()
				+ ", in class " + call.getClassName());
		depth += 2;
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(VirtualCall call, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, call);
		output.append("Call to virtual method: " + call.getName());
		if (call.isExternal())
			output.append(", in external scope");
		depth += 2;
		if (call.isExternal())
			output.append(call.getLocation().accept(this, scope));
		for (Expression argument : call.getArguments())
			output.append(argument.accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(This thisExpression, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, thisExpression);
		output.append("Reference to 'this' instance");
		return output.toString();
	}

	@Override
	public Object visit(NewClass newClass, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, newClass);
		output.append("Instantiation of class: " + newClass.getName());
		
		if (scope != null) {
			output.append(", Type: " + scope.lookup(newClass.getName()).getType().getName());
			output.append(", Symbol table: " + scope.getId());
		}
		
		return output.toString();
	}

	@Override
	public Object visit(NewArray newArray, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, newArray);
		output.append("Array allocation");
		depth += 2;
		output.append(", Type: " + newArray.getType().accept(this, scope));
		
		if (scope != null) {
			output.append(", Symbol table: " + scope.getId());
		}
		
		output.append(newArray.getSize().accept(this, scope));
		
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(Length length, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, length);
		output.append("Reference to array length");
		++depth;
		output.append(length.getArray().accept(this, scope));
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(MathBinaryOp binaryOp, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Mathematical binary operation: "
				+ binaryOp.getOperator().getDescription());
		
		// TODO: fix later
		if (scope != null) {
			output.append(", Type: boolean");
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this, scope));
		output.append(binaryOp.getSecondOperand().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, binaryOp);
		output.append("Logical binary operation: "
				+ binaryOp.getOperator().getDescription());
		
		// TODO: fix later
		if (scope != null) {
			output.append(", Type: boolean");
			output.append(", Symbol table: " + scope.getId());
		}
		
		depth += 2;
		output.append(binaryOp.getFirstOperand().accept(this, scope));
		output.append(binaryOp.getSecondOperand().accept(this, scope));
		depth -= 2;
		return output.toString();
	}

	@Override
	public Object visit(MathUnaryOp unaryOp, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Mathematical unary operation: "
				+ unaryOp.getOperator().getDescription());
		
		// TODO: fix later
		if (scope != null) {
			output.append(", Type: int");
			output.append(", Symbol table: " + scope.getId());
		}
		
		++depth;
		output.append(unaryOp.getOperand().accept(this, scope));
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, unaryOp);
		output.append("Logical unary operation: "
				+ unaryOp.getOperator().getDescription());
		
		// TODO: fix later
		if (scope != null) {
			output.append(", Type: boolean");
			output.append(", Symbol table: " + scope.getId());
		}
		
		++depth;
		output.append(unaryOp.getOperand().accept(this, scope));
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(Literal literal, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, literal);
		output.append(literal.getType().getDescription() + ": "
				+ literal.getType().toFormattedString(literal.getValue()));
		
		if (scope != null) {
			if (literal.getType() == LiteralTypes.INTEGER) {
				output.append(", Type: int");
			} else if (literal.getType() == LiteralTypes.STRING) {
				output.append(", Type: string");
			} else if (literal.getType() == LiteralTypes.TRUE
					|| literal.getType() == LiteralTypes.FALSE) {
				output.append(", Type: boolean");
			} else if (literal.getType() == LiteralTypes.NULL) {
				output.append(", Type: null");
			}
			
			output.append(", Symbol table: " + scope.getId());
		}
		
		return output.toString();
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, expressionBlock);
		output.append("Parenthesized expression");
		++depth;
		output.append(expressionBlock.getExpression().accept(this, scope));
		--depth;
		return output.toString();
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod, SymbolTable scope) {
		StringBuffer output = new StringBuffer();

		indent(output, fieldOrMethod);
		output.append("Class Members:");
		depth += 2;
		for (Field field : fieldOrMethod.getFields())
			output.append(field.accept(this, scope));
		for (Method method : fieldOrMethod.getMethods())
			output.append(method.accept(this, scope));
		depth -= 2;

		return output.toString();
	}
}