package IC.SemanticAnalysis;

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
import IC.AST.Return;
import IC.AST.StatementsBlock;
import IC.AST.StaticCall;
import IC.AST.StaticMethod;
import IC.AST.This;
import IC.AST.UserType;
import IC.AST.VariableLocation;
import IC.AST.VirtualCall;
import IC.AST.VirtualMethod;
import IC.AST.Visitor;
import IC.AST.While;
import IC.TypeTable.TypeTable;

public class SemanticTablePrinter implements Visitor {

	private int depth = 0; // depth of indentation

	private String ICFilePath;

	private ASTNode root;

	/**
	 * Constructs a new pretty printer visitor.
	 * 
	 * @param ICFilePath
	 *            The path + name of the IC file being compiled.
	 */
	public SemanticTablePrinter(String ICFilePath) {
		this.ICFilePath = ICFilePath;
	}

	private void indent(StringBuffer output, ASTNode node) {
		output.append("\n");
		for (int i = 0; i < depth; ++i) {
			output.append(" ");
		}
	}

	private void indent(StringBuffer output) {
		indent(output, null);
	}

	@Override
	public Object visit(Program program) {
		StringBuffer output = new StringBuffer();
		SymbolTable table = program.enclosingScope();
		
		System.out.print(table);
		
		output.append("Type Table: " + table.getId() + "\n");
		depth += 4;
		output.append(TypeTable.print());
		depth -= 4;

		return output.toString();
	}

	@Override
	public Object visit(ICClass icClass) {
		return null;
	}

	@Override
	public Object visit(Formal formal) {
		return null;
	}

	@Override
	public Object visit(VirtualMethod method) {
		return null;
	}

	@Override
	public Object visit(StaticMethod method) {
		return null;
	}

	@Override
	public Object visit(LibraryMethod method) {
		return null;
	}

	@Override
	public Object visit(If ifStatement) {
		return null;
	}

	@Override
	public Object visit(While whileStatement) {
		return null;
	}

	@Override
	public Object visit(StatementsBlock statementsBlock) {
		return null;
	}

	@Override
	public Object visit(PrimitiveType type) {
		return null;
	}

	@Override
	public Object visit(UserType type) {
		return null;
	}

	@Override
	public Object visit(Field field) {
		return null;
	}

	@Override
	public Object visit(Break breakStatement) {
		return null;
	}

	@Override
	public Object visit(Continue continueStatement) {
		return null;
	}

	@Override
	public Object visit(Assignment assignment) {
		return null;
	}

	@Override
	public Object visit(CallStatement callStatement) {
		return null;
	}

	@Override
	public Object visit(Return returnStatement) {
		return null;
	}

	@Override
	public Object visit(LocalVariable localVariable) {
		return null;
	}

	@Override
	public Object visit(VariableLocation location) {
		return null;
	}

	@Override
	public Object visit(ArrayLocation location) {
		return null;
	}

	@Override
	public Object visit(StaticCall call) {
		return null;
	}

	@Override
	public Object visit(VirtualCall call) {
		return null;
	}

	@Override
	public Object visit(This thisExpression) {
		return null;
	}

	@Override
	public Object visit(NewClass newClass) {
		return null;
	}

	@Override
	public Object visit(NewArray newArray) {
		return null;
	}

	@Override
	public Object visit(Length length) {
		return null;
	}

	@Override
	public Object visit(MathBinaryOp binaryOp) {
		return null;
	}

	@Override
	public Object visit(LogicalBinaryOp binaryOp) {
		return null;
	}

	@Override
	public Object visit(MathUnaryOp unaryOp) {
		return null;
	}

	@Override
	public Object visit(LogicalUnaryOp unaryOp) {
		return null;
	}

	@Override
	public Object visit(Literal literal) {
		return null;
	}

	@Override
	public Object visit(ExpressionBlock expressionBlock) {
		return null;
	}

	@Override
	public Object visit(FieldOrMethod fieldOrMethod) {
		return null;
	}
}
