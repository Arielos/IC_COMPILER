package IC.AST;

/**
 * AST visitor interface. Declares methods for visiting each type of AST node.
 * 
 * @author Tovi Almozlino
 */
public interface Visitor {

	Object visit(Program program);

	Object visit(ICClass icClass);

	Object visit(Field field);

	Object visit(VirtualMethod method);

	Object visit(StaticMethod method);

	Object visit(LibraryMethod method);

	Object visit(Formal formal);

	Object visit(PrimitiveType type);

	Object visit(UserType type);

	Object visit(Assignment assignment);

	Object visit(CallStatement callStatement);

	Object visit(Return returnStatement);

	Object visit(If ifStatement);

	Object visit(While whileStatement);

	Object visit(Break breakStatement);

	Object visit(Continue continueStatement);

	Object visit(StatementsBlock statementsBlock);

	Object visit(LocalVariable localVariable);

	Object visit(VariableLocation location);

	Object visit(ArrayLocation location);

	Object visit(StaticCall call);

	Object visit(VirtualCall call);

	Object visit(This thisExpression);

	Object visit(NewClass newClass);

	Object visit(NewArray newArray);

	Object visit(Length length);

	Object visit(MathBinaryOp binaryOp);

	Object visit(LogicalBinaryOp binaryOp);

	Object visit(MathUnaryOp unaryOp);

	Object visit(LogicalUnaryOp unaryOp);

	Object visit(Literal literal);

	Object visit(ExpressionBlock expressionBlock);

	Object visit(FieldOrMethod fieldOrMethod);
}
