package IC.LirTranslate;

import IC.AST.*;

import java.util.List;

/**
 * Liveness Analisys vistor, in order to produce
 * interference graph and create registers plans
 */
public class SethiUllmanAlgorithm implements Visitor {

    private final ASTNode root;

    public SethiUllmanAlgorithm(ASTNode root) {
        this.root = root;
    }

    public void analyze() {
        try {
            root.accept(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error: " + ex.getMessage());
        }
    }


    @Override
    public ASTNode visit(Program program) {
        program.getClasses().stream().filter(icClass -> !icClass.getName().equals("Library")).forEach(icClass -> {
            icClass.accept(this);
        });

        return null;
    }

    @Override
    public ASTNode visit(ICClass icClass) {
        for (Method method : icClass.getMethods()) {
            method.accept(this);
        }

        return null;
    }

    @Override
    public ASTNode visit(Field field) {
        return null;
    }

    @Override
    public ASTNode visit(VirtualMethod method) {
        List<Statement> stmts = method.getStatements();
        int size = stmts.size() - 1;

        for (int i = size; i >= 0; i--) {
            stmts.get(i).accept(this);
        }

        return null;
    }

    @Override
    public ASTNode visit(StaticMethod method) {
        List<Statement> stmts = method.getStatements();
        int size = stmts.size() - 1;

        for (int i = size; i >= 0; i--) {
            stmts.get(i).accept(this);
        }

        return null;
    }

    @Override
    public ASTNode visit(LibraryMethod method) {
        return null;
    }

    @Override
    public ASTNode visit(Formal formal) {
        return null;
    }

    @Override
    public ASTNode visit(PrimitiveType type) {
        return null;
    }

    @Override
    public ASTNode visit(UserType type) {
        return null;
    }

    @Override
    public ASTNode visit(Assignment assignment) {
        assignment.getVariable().accept(this);
        assignment.getAssignment().accept(this);
        return null;
    }

    @Override
    public ASTNode visit(CallStatement callStatement) {
        callStatement.getCall().accept(this);
        return null;
    }

    @Override
    public ASTNode visit(Return returnStatement) {
        if (returnStatement.hasValue()) {
            return (ASTNode) returnStatement.getValue().accept(this);
        }

        return returnStatement;
    }

    @Override
    public ASTNode visit(If ifStatement) {
        ifStatement.getOperation().accept(this);
        ifStatement.getCondition().accept(this);

        if (ifStatement.hasElse()) {
            ifStatement.getElseOperation().accept(this);
        }

        return ifStatement;
    }

    @Override
    public ASTNode visit(While whileStatement) {
        whileStatement.getOperation().accept(this);
        whileStatement.getCondition().accept(this);
        return whileStatement;
    }

    @Override
    public ASTNode visit(Break breakStatement) {
        return breakStatement;
    }

    @Override
    public ASTNode visit(Continue continueStatement) {
        return continueStatement;
    }

    @Override
    public ASTNode visit(StatementsBlock statementsBlock) {
        List<Statement> stmts = statementsBlock.getStatements();
        int size = stmts.size() - 1;

        for (int i = size; i >= 0; i--) {
            stmts.get(i).accept(this);
        }

        return null;
    }

    @Override
    public ASTNode visit(LocalVariable localVariable) {
        return localVariable;
    }

    @Override
    public ASTNode visit(VariableLocation location) {
        location.setWeight(1);
        return location;
    }

    @Override
    public ASTNode visit(ArrayLocation location) {
        int myWeight = 0;

        myWeight += ((Expression) location.getArray().accept(this)).getWeight();
        myWeight += ((Expression) location.getIndex().accept(this)).getWeight();

        location.setWeight(myWeight);

        return location;
    }

    @Override
    public ASTNode visit(StaticCall call) {
        int myWeight = 0;

        for (Expression expr : call.getArguments()) {
            myWeight += ((Expression)expr.accept(this)).getWeight();
        }

        call.setWeight(myWeight);
        call.setAsSideEffect();

        return call;
    }

    @Override
    public ASTNode visit(VirtualCall call) {
        int myWeight = 0;

        for (Expression expr : call.getArguments()) {
            myWeight += ((Expression)expr.accept(this)).getWeight();
        }

        call.setWeight(myWeight + 1);
        call.setAsSideEffect();

        return call;
    }

    @Override
    public ASTNode visit(This thisExpression) {
        return thisExpression;
    }

    @Override
    public ASTNode visit(NewClass newClass) {
        return newClass;
    }

    @Override
    public ASTNode visit(NewArray newArray) {
        return newArray;
    }

    @Override
    public ASTNode visit(Length length) {
        return length;
    }

    @Override
    public ASTNode visit(MathBinaryOp binaryOp) {
        Expression left = (Expression) binaryOp.getFirstOperand().accept(this);
        Expression right = (Expression) binaryOp.getSecondOperand().accept(this);

        if (left.getWeight() == right.getWeight()) {
            binaryOp.setWeight(left.getWeight() + 1);
        } else {
            if (left.getWeight() < right.getWeight()) {
                binaryOp.setWeight(right.getWeight());
            }
        }

        return binaryOp;
    }

    @Override
    public ASTNode visit(LogicalBinaryOp binaryOp) {
        Expression left = (Expression) binaryOp.getFirstOperand().accept(this);
        Expression right = (Expression) binaryOp.getSecondOperand().accept(this);

        if (left.getWeight() == right.getWeight()) {
            binaryOp.setWeight(left.getWeight() + 1);
        } else {
            if (left.getWeight() < right.getWeight()) {
                binaryOp.setWeight(right.getWeight());
            }
        }

        return binaryOp;
    }

    @Override
    public ASTNode visit(MathUnaryOp unaryOp) {
        return (ASTNode) unaryOp.getOperand().accept(this);
    }

    @Override
    public ASTNode visit(LogicalUnaryOp unaryOp) {
        return (ASTNode) unaryOp.getOperand().accept(this);
    }

    @Override
    public ASTNode visit(Literal literal) {
        literal.setWeight(0);
        return literal;
    }

    @Override
    public Expression visit(ExpressionBlock expressionBlock) {
        return (Expression) expressionBlock.getExpression().accept(this);
    }

    @Override
    public ASTNode visit(FieldOrMethod fieldOrMethod) {
        for (Field field : fieldOrMethod.getFields()) {
            field.accept(this);
        }

        for (Method method : fieldOrMethod.getMethods()) {
            method.accept(this);
        }

        return fieldOrMethod;
    }
}
