package IC.AST;

/**
 * An interface for a propagating AST visitor. The visitor passes down objects
 * of type <code>DownType</code> and propagates up objects of type
 * <code>UpType</code>.
 */
public interface PropagatingVisitor<DownType, UpType> {

	UpType visit(Program program, DownType context) throws Exception;

	UpType visit(ICClass icClass, DownType context) throws Exception;

	UpType visit(Field field, DownType context) throws Exception;

    UpType visit(VirtualMethod method, DownType context) throws Exception;

	UpType visit(StaticMethod method, DownType context) throws Exception;

	UpType visit(LibraryMethod method, DownType context) throws Exception;

	UpType visit(Formal formal, DownType context) throws Exception;

	UpType visit(PrimitiveType type, DownType context) throws Exception;

	UpType visit(UserType type, DownType context) throws Exception;

	UpType visit(Assignment assignment, DownType context) throws Exception;

	UpType visit(CallStatement callStatement, DownType context) throws Exception;

	UpType visit(Return returnStatement, DownType context) throws Exception;

	UpType visit(If ifStatement, DownType context) throws Exception;

	UpType visit(While whileStatement, DownType context) throws Exception;

	UpType visit(Break breakStatement, DownType context) throws Exception;

	UpType visit(Continue continueStatement, DownType context) throws Exception;

	UpType visit(StatementsBlock statementsBlock, DownType context) throws Exception;

	UpType visit(LocalVariable localVariable, DownType context) throws Exception;

	UpType visit(VariableLocation location, DownType context) throws Exception;

	UpType visit(ArrayLocation location, DownType context) throws Exception;

	UpType visit(StaticCall call, DownType context) throws Exception;

	UpType visit(VirtualCall call, DownType context) throws Exception;

	UpType visit(This thisExpression, DownType context) throws Exception;

	UpType visit(NewClass newClass, DownType context) throws Exception;

	UpType visit(NewArray newArray, DownType context) throws Exception;

	UpType visit(Length length, DownType context) throws Exception;

	UpType visit(MathBinaryOp binaryOp, DownType context) throws Exception;

	UpType visit(LogicalBinaryOp binaryOp, DownType context) throws Exception;

	UpType visit(MathUnaryOp unaryOp, DownType context) throws Exception;

	UpType visit(LogicalUnaryOp unaryOp, DownType context) throws Exception;

	UpType visit(Literal literal, DownType context) throws Exception;

	UpType visit(ExpressionBlock expressionBlock, DownType context) throws Exception;

	UpType visit(FieldOrMethod fieldOrMethod, DownType context) throws Exception;
}