package IC.AST;

/**
 * An interface for a propagating AST visitor. The visitor passes down objects
 * of type <code>DownType</code> and propagates up objects of type
 * <code>UpType</code>.
 */
public interface PropagatingVisitor<DownType, UpType> {

	public UpType visit(Program program, DownType context);

	public UpType visit(ICClass icClass, DownType context);

	public UpType visit(Field field, DownType context);

	public UpType visit(VirtualMethod method, DownType context);

	public UpType visit(StaticMethod method, DownType context);

	public UpType visit(LibraryMethod method, DownType context);

	public UpType visit(Formal formal, DownType context);

	public UpType visit(PrimitiveType type, DownType context);

	public UpType visit(UserType type, DownType context);

	public UpType visit(Assignment assignment, DownType context);

	public UpType visit(CallStatement callStatement, DownType context);

	public UpType visit(Return returnStatement, DownType context);

	public UpType visit(If ifStatement, DownType context);

	public UpType visit(While whileStatement, DownType context);

	public UpType visit(Break breakStatement, DownType context);

	public UpType visit(Continue continueStatement, DownType context);

	public UpType visit(StatementsBlock statementsBlock, DownType context);

	public UpType visit(LocalVariable localVariable, DownType context);

	public UpType visit(VariableLocation location, DownType context);

	public UpType visit(ArrayLocation location, DownType context);

	public UpType visit(StaticCall call, DownType context);

	public UpType visit(VirtualCall call, DownType context);

	public UpType visit(This thisExpression, DownType context);

	public UpType visit(NewClass newClass, DownType context);

	public UpType visit(NewArray newArray, DownType context);

	public UpType visit(Length length, DownType context);

	public UpType visit(MathBinaryOp binaryOp, DownType context);

	public UpType visit(LogicalBinaryOp binaryOp, DownType context);

	public UpType visit(MathUnaryOp unaryOp, DownType context);

	public UpType visit(LogicalUnaryOp unaryOp, DownType context);

	public UpType visit(Literal literal, DownType context);

	public UpType visit(ExpressionBlock expressionBlock, DownType context);

	public UpType visit(FieldOrMethod fieldOrMethod, DownType context);
}
