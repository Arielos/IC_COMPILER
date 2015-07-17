package IC.LirTranslate;

import IC.AST.*;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

/**
 * Liveness Analisys vistor, in order to produce
 * interference graph and create registers plans
 */
public class LivenessAnalysis implements PropagatingVisitor<Set<String>, Set<String>> {

    private final ASTNode root;

    public LivenessAnalysis(ASTNode root) {
        this.root = root;
    }

    public void analyze() {

        try {
            root.accept(this, new HashSet<>());
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Error: " + ex.getMessage());
        }
    }


    @Override
    public Set<String> visit(Program program, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        for (ICClass icClass : program.getClasses()) {
            if (!icClass.getName().equals("Library")) {
                inLive.addAll(icClass.accept(this, inLive));
            }
        }

        //System.out.println("-------------------------------- Program " + program.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(ICClass icClass, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        for (Method method : icClass.getMethods()) {
            inLive.addAll(method.accept(this, inLive));
        }

        //System.out.println("-------------------------------- ICClass " + icClass.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(Field field, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(VirtualMethod method, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;
        StringBuffer output = new StringBuffer();

        List<Statement> stmts = method.getStatements();
        int size = stmts.size() - 1;

        output.append("-------------------------------- VirtualMethod " + method.getLine() + "\n");
        output.append("Out Live: " + outLive + "\n");

        for (int i = size; i >= 0; i--) {
            inLive.addAll(stmts.get(i).accept(this, inLive));
        }

        //for (Formal formal : method.getFormals()) {
        //    inLive.addAll(formal.accept(this, inLive));
        //}

        output.append("In Live: " + inLive);
        System.out.println(output);

        return inLive;
    }

    @Override
    public Set<String> visit(StaticMethod method, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;
        StringBuffer output = new StringBuffer();

        List<Statement> stmts = method.getStatements();
        int size = stmts.size() - 1;

        output.append("-------------------------------- StaticMethod " + method.getLine() + "\n");
        output.append("Out Live: " + outLive + "\n");

        for (int i = size; i >= 0; i--) {
            inLive.addAll(stmts.get(i).accept(this, inLive));
        }

        //for (Formal formal : method.getFormals()) {
        //    inLive.addAll(formal.accept(this, inLive));
        //}

        output.append("In Live: " + inLive);
        System.out.println(output);

        return inLive;
    }

    @Override
    public Set<String> visit(LibraryMethod method, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(Formal formal, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.add(formal.getName());

        //System.out.println("-------------------------------- Formal " + formal.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(PrimitiveType type, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(UserType type, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(Assignment assignment, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;
        Set<String> ref = new HashSet<>();
        Set<String> def = new HashSet<>();

        StringBuffer output = new StringBuffer();

        output.append("-------------------------------- Assignment " + assignment.getLine() + "\n");
        output.append("Out Live: " + outLive + "\n");

        def.addAll(assignment.getVariable().accept(this, def));      // def
        ref.addAll(assignment.getAssignment().accept(this, ref));    // ref

        inLive.removeAll(def);
        inLive.addAll(ref);

        output.append("In Live: " + inLive);
        System.out.println(output);

        return inLive;
    }

    @Override
    public Set<String> visit(CallStatement callStatement, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(callStatement.getCall().accept(this, inLive));

        System.out.println("-------------------------------- CallStatement " + callStatement.getLine());
        printInLive(inLive);
        printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(Return returnStatement, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;
        StringBuffer output = new StringBuffer();

        output.append("-------------------------------- Return " + returnStatement.getLine() + "\n");
        output.append("Out Live: " + outLive + "\n");

        if (returnStatement.hasValue()) {
            inLive.addAll(returnStatement.getValue().accept(this, inLive));
        }

        output.append("In Live: " + inLive);
        System.out.println(output);

        return inLive;
    }

    @Override
    public Set<String> visit(If ifStatement, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(ifStatement.getOperation().accept(this, inLive));
        inLive.addAll(ifStatement.getCondition().accept(this, inLive));

        if (ifStatement.hasElse()) {
            inLive.addAll(ifStatement.getElseOperation().accept(this, inLive));
        }

        System.out.println("-------------------------------- If " + ifStatement.getLine());
        printInLive(inLive);
        printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(While whileStatement, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;
        StringBuffer output = new StringBuffer();
        output.append("-------------------------------- While " + whileStatement.getLine() + "\n");

        inLive.addAll(whileStatement.getOperation().accept(this, inLive));
        output.append("Out Live: " + inLive + "\n");

        inLive.addAll(whileStatement.getCondition().accept(this, inLive));

        output.append("In Live: " + inLive);
        System.out.println(output);

        return inLive;
    }

    @Override
    public Set<String> visit(Break breakStatement, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(Continue continueStatement, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(StatementsBlock statementsBlock, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        List<Statement> stmts = statementsBlock.getStatements();
        int size = stmts.size() - 1;

        for (int i = size; i >= 0; i--) {
            inLive.addAll(stmts.get(i).accept(this, inLive));
        }

        //System.out.println("-------------------------------- StatementsBlock " + statementsBlock.getLine());
        //printOutLive(outLive);
        //printInLive(inLive);

        return inLive;
    }

    @Override
    public Set<String> visit(LocalVariable localVariable, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.remove(localVariable.getName());

        if (localVariable.hasInitValue()) {
            inLive.addAll(localVariable.getInitValue().accept(this, inLive));
        }

        //System.out.println("-------------------------------- LocalVariable " + localVariable.getLine());
        //printOutLive(outLive);
        //printInLive(inLive);

        return inLive;
    }

    @Override
    public Set<String> visit(VariableLocation location, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.add(location.getName());

        if (location.isExternal()) {
            inLive.addAll(location.getLocation().accept(this, inLive));
        }

        //System.out.println("-------------------------------- VariableLocation " + location.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(ArrayLocation location, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(location.getArray().accept(this, inLive));
        inLive.addAll(location.getIndex().accept(this, inLive));

        System.out.println("-------------------------------- ArrayLocation " + location.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(StaticCall call, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        for (Expression expr : call.getArguments()) {
            inLive.addAll(expr.accept(this, inLive));
        }

        System.out.println("-------------------------------- StaticCall " + call.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(VirtualCall call, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        for (Expression expr : call.getArguments()) {
            inLive.addAll(expr.accept(this, inLive));
        }

        System.out.println("-------------------------------- VirtualCall " + call.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(This thisExpression, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(NewClass newClass, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(NewArray newArray, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(newArray.getSize().accept(this, inLive));

        System.out.println("-------------------------------- NewArray " + newArray.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(Length length, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(length.getArray().accept(this, inLive));

        System.out.println("-------------------------------- Length " + length.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(MathBinaryOp binaryOp, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(binaryOp.getFirstOperand().accept(this, inLive));
        inLive.addAll(binaryOp.getSecondOperand().accept(this, inLive));

        //System.out.println("-------------------------------- MathBinaryOp " + binaryOp.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(LogicalBinaryOp binaryOp, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(binaryOp.getFirstOperand().accept(this, inLive));
        inLive.addAll(binaryOp.getSecondOperand().accept(this, inLive));

        //System.out.println("-------------------------------- LogicalBinaryOp " + binaryOp.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(MathUnaryOp unaryOp, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(unaryOp.getOperand().accept(this, inLive));

        //System.out.println("-------------------------------- MathUnaryOp " + unaryOp.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(LogicalUnaryOp unaryOp, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(unaryOp.getOperand().accept(this, inLive));

        //System.out.println("-------------------------------- LogicalUnaryOp " + unaryOp.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(Literal literal, Set<String> outLive) throws Exception {
        return outLive;
    }

    @Override
    public Set<String> visit(ExpressionBlock expressionBlock, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        inLive.addAll(expressionBlock.getExpression().accept(this, inLive));

        //System.out.println("-------------------------------- ExpressionBlock " + expressionBlock.getLine());
        //printInLive(inLive);
        //printOutLive(outLive);

        return inLive;
    }

    @Override
    public Set<String> visit(FieldOrMethod fieldOrMethod, Set<String> outLive) throws Exception {
        Set<String> inLive = outLive;

        for (Field field : fieldOrMethod.getFields()) {
            inLive.addAll(field.accept(this, inLive));
        }

        for (Method method : fieldOrMethod.getMethods()) {
            inLive.addAll(method.accept(this, inLive));
        }

        return inLive;
    }

    private void printOutLive(Set<String> outLive) {
        //System.out.println("Out Live: " + outLive);
    }

    private void printInLive(Set<String> inLive) {
        System.out.println("In Live: " + inLive);
    }

}
