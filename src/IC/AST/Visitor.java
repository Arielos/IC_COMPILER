package IC.AST;

/**
 * AST visitor interface. Declares methods for visiting each type of AST node.
 * 
 * @author Tovi Almozlino
 */
public interface Visitor {

    /**
     * Visitor method for Program nodes
     * @param program the Program node
     * @return result object
     */
	Object visit(Program program);

    /**
     * Visitor method for ICClass nodes
     * @param icClass the ICClass node
     * @return result object
     */
	Object visit(ICClass icClass);

    /**
     * Visitor method for Field nodes
     * @param field the Field node
     * @return result object
     */
	Object visit(Field field);

    /**
     * Visitor method for VirtualMethod nodes
     * @param method the VirtualMethod node
     * @return result object
     */
	Object visit(VirtualMethod method);

    /**
     * Visitor method for StaticMethod nodes
     * @param method the StaticMethod node
     * @return result object
     */
	Object visit(StaticMethod method);

    /**
     * Visitor method for LibraryMethod nodes
     * @param method the LibraryMethod node
     * @return result object
     */
	Object visit(LibraryMethod method);

    /**
     * Visitor method for Formal nodes
     * @param formal the Formal node
     * @return result object
     */
	Object visit(Formal formal);

    /**
     * Visitor method for PrimitiveType nodes
     * @param type the PrimitiveType node
     * @return result object
     */
	Object visit(PrimitiveType type);

    /**
     * Visitor method for UserType nodes
     * @param type the UserType node
     * @return result object
     */
	Object visit(UserType type);

    /**
     * Visitor method for Assignment nodes
     * @param assignment the Assignment node
     * @return result object
     */
	Object visit(Assignment assignment);

    /**
     * Visitor method for CallStatement nodes
     * @param callStatement the CallStatement node
     * @return result object
     */
	Object visit(CallStatement callStatement);

    /**
     * Visitor method for Return nodes
     * @param returnStatement the Return node
     * @return result object
     */
	Object visit(Return returnStatement);

    /**
     * Visitor method for If nodes
     * @param ifStatement the If node
     * @return result object
     */
	Object visit(If ifStatement);

    /**
     * Visitor method for While nodes
     * @param whileStatement the While node
     * @return result object
     */
	Object visit(While whileStatement);

    /**
     * Visitor method for Break nodes
     * @param breakStatement the Break node
     * @return result object
     */
	Object visit(Break breakStatement);

    /**
     * Visitor method for Continue nodes
     * @param continueStatement the Continue node
     * @return result object
     */
	Object visit(Continue continueStatement);

    /**
     * Visitor method for StatementsBlock nodes
     * @param statementsBlock the StatementsBlock node
     * @return result object
     */
	Object visit(StatementsBlock statementsBlock);

    /**
     * Visitor method for LocalVariable nodes
     * @param localVariable the LocalVariable node
     * @return result object
     */
	Object visit(LocalVariable localVariable);

    /**
     * Visitor method for VariableLocation nodes
     * @param location the VariableLocation node
     * @return result object
     */
	Object visit(VariableLocation location);

    /**
     * Visitor method for ArrayLocation nodes
     * @param location the ArrayLocation node
     * @return result object
     */
	Object visit(ArrayLocation location);

    /**
     * Visitor method for StaticCall nodes
     * @param call the StaticCall node
     * @return result object
     */
	Object visit(StaticCall call);

    /**
     * Visitor method for VirtualCall nodes
     * @param call the VirtualCall node
     * @return result object
     */
	Object visit(VirtualCall call);

    /**
     * Visitor method for This nodes
     * @param thisExpression the This node
     * @return result object
     */
	Object visit(This thisExpression);

    /**
     * Visitor method for NewClass nodes
     * @param newClass the NewClass node
     * @return result object
     */
	Object visit(NewClass newClass);

    /**
     * Visitor method for NewArray nodes
     * @param newArray the NewArray node
     * @return result object
     */
	Object visit(NewArray newArray);

    /**
     * Visitor method for Length nodes
     * @param length the Length node
     * @return result object
     */
	Object visit(Length length);

    /**
     * Visitor method for MathBinaryOp nodes
     * @param binaryOp the MathBinaryOp node
     * @return result object
     */
	Object visit(MathBinaryOp binaryOp);

    /**
     * Visitor method for LogicalBinaryOp nodes
     * @param binaryOp the LogicalBinaryOp node
     * @return result object
     */
	Object visit(LogicalBinaryOp binaryOp);

    /**
     * Visitor method for MathUnaryOp nodes
     * @param unaryOp the MathUnaryOp node
     * @return result object
     */
	Object visit(MathUnaryOp unaryOp);

    /**
     * Visitor method for LogicalUnaryOp nodes
     * @param unaryOp the LogicalUnaryOp node
     * @return result object
     */
	Object visit(LogicalUnaryOp unaryOp);

    /**
     * Visitor method for Literal nodes
     * @param literal the Literal node
     * @return result object
     */
	Object visit(Literal literal);

    /**
     * Visitor method for ExpressionBlock nodes
     * @param expressionBlock the ExpressionBlock node
     * @return result object
     */
	Object visit(ExpressionBlock expressionBlock);

    /**
     * Visitor method for FieldOrMethod nodes
     * @param fieldOrMethod the FieldOrMethod node
     * @return result object
     */
	Object visit(FieldOrMethod fieldOrMethod);
}
