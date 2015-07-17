package IC.LirTranslate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import IC.BinaryOps;
import IC.CompilerUtils;
import IC.TypeTable.*;
import IC.AST.*;
import IC.SemanticAnalysis.Kind;
import IC.SemanticAnalysis.SymbolTable;
import IC.SemanticAnalysis.SymbolTableRow;

public class Translator implements PropagatingVisitor<RegistersInformation, RetObject> {

    private boolean withComments;
    private ASTNode root;
    private ClassLayoutsManager manager;
    private SymbolTable currentScope;
    private SymbolTable currentClassScope;
    private StringBuffer methodsBuffer;
    private StringBuffer mainBuffer;
    private StringLiterals stringLiterals;

    private int loopCounter;
    private int loopCounterId;
    private int ifCounter;
    private int ifCounterId;
    private int cmpCounter;

    public Translator(ASTNode root) {
        super();
        this.root = root;
        this.cmpCounter = 0;
        this.ifCounter = 0;
        this.ifCounterId = 0;
        this.loopCounter = 0;
        this.loopCounterId = 0;
        this.manager = new ClassLayoutsManager();
        this.stringLiterals = new StringLiterals();
    }

    public String translate(boolean debug) throws Exception {
        RegistersInformation regInfo = new RegistersInformation();
        regInfo.initRegisters(50);

        this.methodsBuffer = new StringBuffer();
        this.mainBuffer = new StringBuffer();
        this.withComments = debug;

        return root.accept(this, regInfo).getLir();
    }

    private String errorMessages() {
        StringBuffer output = new StringBuffer();

        output.append("str_null_ref: \"Runtime Error: Null pointer dereference!\"\n");
        output.append("str_array_access: \"Runtime Error: Array index out of bounds!\"\n");
        output.append("str_size: \"Runtime Error: Array allocation with negative array size!\"\n");
        output.append("str_zero: \"Runtime Error: Division by zero!\"\n");

        return output.toString();
    }

    private String runtimeErrors() {
        return "\n# Runtime checks:\n" +
                checkZero() + "\n" +
                checkNullRef() + "\n" +
                checkArrayAccess() + "\n" +
                checkArraySize();
    }

    private String checkZero() {
        return  "__checkZero:\n"+
                "Move b, R1\n"+
                "Compare 0, R1\n"+
                "JumpTrue __checkZero_err\n"+
                "Return 9999\n"+
                "__checkZero_err:\n"+
                "Library __println(str_zero),Rdummy\n"+
                "Jump _error_exit\n";
    }

    private String checkNullRef() {
        return "__checkNullRef:\n" +
                "Move a, R1\n" +
                "Compare 0, R1\n" +
                "JumpTrue __checkNullRef_err\n" +
                "Return 9999\n" +
                "__checkNullRef_err:\n" +
                "Library __println(str_null_ref),Rdummy\n" +
                "Jump _error_exit\n";
    }

    private String checkArrayAccess() {
        return "__checkArrayAccess:\n" +
                "Move a, R1\n" +
                "Move i, R2\n" +
                "ArrayLength R1, R1\n" +
                "Compare R1, R2\n" +
                "JumpGE __checkArrayAccess_err\n" +
                "Compare 0, R2\n" +
                "JumpL __checkArrayAccess_err\n" +
                "Return 9999\n" +
                "__checkArrayAccess_err:\n" +
                "Library __println(str_array_access), Rdummy\n" +
                "Jump _error_exit\n";
    }

    private String checkArraySize() {
        return "__checkSize:\n" +
                "Move n, R1\n" +
                "Compare 0, R1\n" +
                "JumpL __checkSize_err\n" +
                "Return 9999\n" +
                "__checkSize_err:\n" +
                "Library __println(str_size), Rdummy\n" +
                "Jump _error_exit\n";
    }

	@Override
	public RetObject visit(Program program, RegistersInformation context)
			throws Exception {
		
		RetObject output = new RetObject();
		
		List<ICClass> classes = new ArrayList<>();
		classes.addAll(program.getClasses());
		
		manager.build(classes);

		for (ICClass icClass : program.getClasses()) {
			if (!icClass.getName().equals("Library")) {
				output.addLir(icClass.accept(this, context).getLir());
			}
		}

        output.addLir(errorMessages());
        output.addLir(stringLiterals.toString());
		output.addLir(manager.printPointers());
        output.addLir(runtimeErrors());
		output.addLir(methodsBuffer.toString());
		output.addLir(mainBuffer.toString());
        output.addLir("_error_exit:\n");
		return output;
	}
	
	@Override
	public RetObject visit(ICClass icClass, RegistersInformation context)
			throws Exception {
		
		currentClassScope = icClass.enclosingScope();
		
		for (Method method : icClass.getMethods()) {
            if (method.getName().equals("main")) {
				mainBuffer.append("\n# main in " + icClass.getName() + "\n");
				mainBuffer.append("_ic_main:\n");
				mainBuffer.append(method.accept(this, context));
			} else {
				methodsBuffer.append(method.accept(this, context));
			}

            context.freeAllRegisters();
        }
		
		return new RetObject();
	}

	@Override
	public RetObject visit(Field field, RegistersInformation context)
			throws Exception {
		
		return new RetObject();
	}

	@Override
	public RetObject visit(VirtualMethod method, RegistersInformation context)
			throws Exception {
		
		StringBuffer output = new StringBuffer();

		currentScope = method.enclosingScope();
		output.append("\n_" + currentScope.getParentSymbolTable().getId() + "_" + method.getName() + ":\n");
		
		for (Formal formal : method.getFormals()) {
			output.append(formal.accept(this, context).getLir());
		}
		
		for (Statement stmt : method.getStatements()) {
			output.append(stmt.accept(this, context).getLir());
		}

        Register reg;
        for (Map.Entry<String, SymbolTableRow> symbol : method.enclosingScope().getIterator()) {
            reg = context.getRegister(symbol.getKey());
            if (reg != null) {
                //context.freeRegister(reg);
            }

            if (symbol.getValue().getType() == TypeTable.voidType) {
                output.append("Return 9999\n");
            }
        }

		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		return outputLir;
	}

	@Override
	public RetObject visit(StaticMethod method, RegistersInformation context)
			throws Exception {
		
		StringBuffer output = new StringBuffer();
		
		currentScope = method.enclosingScope();

        if (!method.getName().equals("main")) {
            output.append("\n_" + method.getName() + ":\n");
        }

		for (Statement stmt : method.getStatements()) {
			output.append(stmt.accept(this, context).getLir());
		}

        Register reg;
        for (Map.Entry<String, SymbolTableRow> symbol : method.enclosingScope().getIterator()) {
            reg = context.getRegister(symbol.getKey());
            if (reg != null) {
                //context.freeRegister(reg);
            }

            if (!method.getName().equals("main") && symbol.getValue().getType() == TypeTable.voidType) {
                output.append("Return 9999\n");
            }
        }

		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		return outputLir;
	}

	@Override
	public RetObject visit(LibraryMethod method, RegistersInformation context)
			throws Exception {
		
		return new RetObject();
	}

	@Override
	public RetObject visit(Formal formal, RegistersInformation context)
			throws Exception {
		
		return new RetObject();
	}

	@Override
	public RetObject visit(PrimitiveType type, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(type.getLine(), type) : "";
		RetObject retObject = new RetObject();

		retObject.addLir(comment);
		retObject.setType(CompilerUtils.primitiveTypeToMyType(type));
		
		return retObject;
	}

	@Override
	public RetObject visit(UserType type, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(type.getLine(), type) : "";
		RetObject retObject = new RetObject();
		
		retObject.addLir(comment + type.getName());
		retObject.setType(CompilerUtils.userTypeToMyType(type));
		
		return retObject;
	}

	@Override
	public RetObject visit(Assignment assignment, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(assignment.getLine(), assignment) : "";
        StringBuffer output = new StringBuffer();

        RetObject assignmentLir = assignment.getAssignment().accept(this, context);
        Register targetRegister = context.getRegister(assignmentLir.getResult());

        if (targetRegister == null) {
            targetRegister = context.getAvailableRegiser();
        }

        targetRegister.setValue("temp");
        output.append(comment + assignmentLir.getLir());

        if (!assignmentLir.getResult().equals(targetRegister.toString())) {
            output.append(comment + createMove(assignmentLir) + " " + assignmentLir.getResult() + ", " + targetRegister + "\n");
        }

		RetObject variableLir = assignment.getVariable().accept(this, context);
		output.append(comment + variableLir.getLir());

        if (!variableLir.getResult().equals(targetRegister.toString())) {
            output.append(comment + createMove(variableLir) + " " + targetRegister + ", " + variableLir.getResult() + "\n");
        }

        //context.freeRegister(targetRegister);

		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext(variableLir.getContext());
		outputLir.setType(variableLir.getType());
		return outputLir;
	}

	@Override
	public RetObject visit(CallStatement callStatement,
			RegistersInformation context) throws Exception {
		
		RetObject retObject;
		
		retObject = callStatement.getCall().accept(this, context);
		
		return retObject;
	}

	@Override
	public RetObject visit(Return returnStatement, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(returnStatement.getLine(), returnStatement) : "";
		RetObject retObject = new RetObject();
		
		if (returnStatement.hasValue()) {
			retObject = returnStatement.getValue().accept(this, context);
            retObject.addLir(comment + retObject.getLir());

			Register target = context.getRegister(retObject.getContext().toString());

            if (target == null) {
                target = context.getTargetRegister();
            }

            target.setValue(retObject.getContext());

            if (retObject.isLiteral()) {
                retObject.addLir(comment + "Return " + retObject.getContext() + "\n");
            } else {
                retObject.addLir(comment + "Return " + target + "\n");
            }

			//context.freeRegister(target);
		} else {
			retObject.addLir(comment + "Return 9999\n");
		}
		
		return retObject;
	}

	@Override
	public RetObject visit(If ifStatement, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(ifStatement.getLine(), ifStatement) : "";
		StringBuffer output = new StringBuffer();
			
		Register conditionRegister = context.getAvailableRegiser();
		RetObject ifCondition = ifStatement.getCondition().accept(this, context);
		conditionRegister.setValue(ifCondition.getContext());
		
		output.append(comment + ifCondition.getLir());
		output.append(comment + createMove(ifCondition) + " " + ifCondition.getResult() + ", " + conditionRegister + "\n");
		output.append(comment + "Compare 0, " + conditionRegister + "\n");
		
		ifCounter += 1;

        if (ifStatement.hasElse()) {
            output.append(comment + "JumpTrue _false_label_" + ifCounterId + ifCounter + "\n");
        } else {
            output.append(comment + "JumpTrue _end_label_" + ifCounterId + ifCounter + "\n");
        }

        RetObject ifOperation = ifStatement.getOperation().accept(this, context);
        output.append(comment + ifOperation.getLir());

        if (ifStatement.hasElse()) {
            output.append(comment + "Jump _end_label_" + ifCounterId + ifCounter + "\n");
            output.append(comment + "_false_label_" + ifCounterId + ifCounter + ":\n");
            output.append(comment + ifStatement.getElseOperation().accept(this, context).getLir());
        }

		output.append(comment + "_end_label_" + ifCounterId + ifCounter + ":\n");
		
		ifCounter -= 1;
		ifCounterId += 1;
		
		context.freeRegister(conditionRegister);
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		return outputLir;
	}

	@Override
	public RetObject visit(While whileStatement, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(whileStatement.getLine(), whileStatement) : "";
		StringBuffer output = new StringBuffer();
		
		Register conditionRegister = context.getAvailableRegiser();
		RetObject condition = whileStatement.getCondition().accept(this, context);
		conditionRegister.setValue(condition.getContext());

        loopCounter += 1;

        String testLabel = "_while_test_label_" + loopCounterId + loopCounter;
        String endLabel = "_while_end_label_" + loopCounterId + loopCounter;
		
		output.append(comment + testLabel + ":\n");
		output.append(comment + condition.getLir());
		output.append(comment + createMove(condition) + " " + condition.getResult() + ", " + conditionRegister + "\n");
		output.append(comment + "Compare 0, " + conditionRegister + "\n");
		output.append(comment + "JumpTrue " + endLabel + "\n");
		output.append(comment + whileStatement.getOperation().accept(this, context).getLir());
		output.append(comment + "Jump " + testLabel + "\n");
		output.append(comment + endLabel + ":\n");

        loopCounter -= 1;

        loopCounterId += 1;
		context.freeRegister(conditionRegister);
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		return outputLir;
	}

	@Override
	public RetObject visit(Break breakStatement, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(breakStatement.getLine(), breakStatement) : "";
		RetObject output = new RetObject();

        output.addLir(comment + "Jump _end_label_" + loopCounterId + loopCounter + "\n");
		
		return output;
	}

	@Override
	public RetObject visit(Continue continueStatement, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(continueStatement.getLine(), continueStatement) : "";
		RetObject output = new RetObject();
		
		output.addLir(comment + "Jump _test_label_" + loopCounterId + loopCounter + "\n");
		
		return output;
	}

	@Override
	public RetObject visit(StatementsBlock statementsBlock,
			RegistersInformation context) throws Exception {
		
		RetObject output = new RetObject();
		currentScope = statementsBlock.enclosingScope();
		
		for (Statement stmt : statementsBlock.getStatements()) {
			output.addLir(stmt.accept(this, context).getLir());
		}

		return output;
	}

	@Override
	public RetObject visit(LocalVariable localVariable, RegistersInformation context)
			throws Exception {

        String comment = withComments ? addLogComment(localVariable.getLine(), localVariable) : "";
        StringBuffer output = new StringBuffer();
        RetObject outputLir = new RetObject();
        RetObject localVariableInitValue;

        Register target = context.getRegister(localVariable.getName());

        if (target == null) {
            target = context.getAvailableRegiser();
        }

        target.setValue(localVariable.getName());
        context.setTargetRegister(target);

        if (localVariable.hasInitValue()) {
            localVariableInitValue = localVariable.getInitValue().accept(this, context);
            output.append(comment + localVariableInitValue.getLir());
            output.append(comment + createMove(localVariableInitValue) + " " + localVariableInitValue.getResult() + ", " + target + "\n");
            output.append(comment + "Move " + target + ", " + localVariable.getName() + "\n");
        }

        outputLir.addLir(output.toString());
        outputLir.setContext(localVariable.getName());
        outputLir.setResult(target.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
        return outputLir;
    }

	@Override
	public RetObject visit(VariableLocation location, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(location.getLine(), location) : "";
		StringBuffer output = new StringBuffer();

        RetObject outputLir = new RetObject();
        RetObject.WhoType whoType;
        SymbolTableRow row = currentScope.lookup(location.getName());

        if (location.isExternal()) {
            RetObject variableLocationLir = location.getLocation().accept(this, context);

            output.append(comment + variableLocationLir.getLir());
            row = currentScope.lookup(variableLocationLir.getContext());

            Register classRegister = context.getRegister("_DV_" + row.getType());

            if (classRegister == null) {
                classRegister = context.getAvailableRegiser();
                classRegister.setValue("_DV_" + row.getType());
            }

            ClassLayout layout = manager.getLayout(row.getType().getName());
            int index = layout.getFieldIndex(location.getName());
            output.append(comment + createMove(variableLocationLir) + " " + variableLocationLir.getResult() + ", " + classRegister + "\n");
            output.append(comment + "StaticCall __checkNullRef(a=" + classRegister + "), Rdummy\n");

            whoType = RetObject.WhoType.FIELD;
            outputLir.setResult(classRegister + "." + index);
		} else {
            if (row.getKind() == Kind.FIELD) {
                Register classRegister = context.getRegister("this");

                if (classRegister == null) {
                    classRegister = context.getAvailableRegiser();
                    classRegister.setValue("this");
                }

                output.append(comment + "Move " + classRegister.getValue() + ", " + classRegister + "\n");

                ClassLayout layout = manager.getLayout(currentClassScope.getId());
                int index = layout.getFieldIndex(location.getName());

                outputLir.setResult(classRegister + "." + index);
                whoType = RetObject.WhoType.FIELD;

            } else {
                Register varRegister = context.getRegister(location.getName());

                if (varRegister == null) {
                    varRegister = context.getAvailableRegiser();
                }

                varRegister.setValue(location.getName());

                whoType = RetObject.WhoType.VAR;
                outputLir.setResult(location.getName());
            }
        }

		outputLir.addLir(output.toString());
		outputLir.setType(row.getType());
        outputLir.setContext(location.getName());
        outputLir.setWhoType(whoType);
		return outputLir;
	}

	@Override
	public RetObject visit(ArrayLocation location, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(location.getLine(), location) : "";
		StringBuffer output = new StringBuffer();
		RetObject arrayLir;
		RetObject indexLir;

		arrayLir = location.getArray().accept(this, context);

        Register arrayRegister = context.getRegister(arrayLir.getContext());
        if (arrayRegister == null) {
            arrayRegister = context.getAvailableRegiser();
        }

        arrayRegister.setValue(arrayLir.getResult());

        output.append(comment + arrayLir.getLir());
        output.append(comment + createMove(arrayLir) + " " + arrayLir.getResult() + ", " + arrayRegister + "\n");

		indexLir = location.getIndex().accept(this, context);
		Register indexRegister = context.getRegister(indexLir.getContext());

        if (indexRegister == null) {
			indexRegister = context.getAvailableRegiser();
		}

		indexRegister.setValue(indexLir.getResult());
        output.append(comment + indexLir.getLir());
        output.append(comment + createMove(indexLir) + " " + indexLir.getResult() + ", " + indexRegister + "\n");
        output.append(comment + "StaticCall __checkArrayAccess(a=" + arrayRegister + ", i=" + indexRegister + "), Rdummy\n");

		Register target = context.getTargetRegister();
        target.setValue(arrayLir.getContext());

		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext(target.getValue());
        outputLir.setResult(arrayRegister + "[" + indexRegister + "]");
        outputLir.setWhoType(RetObject.WhoType.ARRAY);
		return outputLir;
	}

	@Override
	public RetObject visit(StaticCall call, RegistersInformation context)
			throws Exception {

        String comment = withComments ? addLogComment(call.getLine(), call) : "";
        StringBuffer output = new StringBuffer();

        boolean isLibrary = false;
        List<RetObject> argObjs = new ArrayList<>();
        List<Register> argRegisters = new ArrayList<>();

        Register argRegister;
        for (Expression expr : call.getArguments()) {
            RetObject retArg = expr.accept(this, context);

            argRegister = context.getRegister(retArg.getContext());

            if (argRegister == null) {
                argRegister = context.getAvailableRegiser();
                argRegister.setValue(retArg.getContext());
            }

            if (retArg.getWhoType() != RetObject.WhoType.LITERAL) { // && retArg.getWhoType() != RetObject.WhoType.VAR
                output.append(retArg.getLir());
                output.append(comment + createMove(retArg) + " " + retArg.getResult() + ", " + argRegister + "\n");
            }

            argObjs.add(retArg);
            argRegisters.add(argRegister);
        }

        if (call.getClassName().equals("Library")) {
            output.append(comment + "Library __" + call.getName() + "(");
            isLibrary = true;
        } else {
            output.append(comment + "StaticCall _" + call.getName() + "(");
        }

        ClassLayout classLayout = manager.getLayout(call.getClassName());
        Method method = classLayout.getStaticMethod(call.getName());

        int i = 0;
        for (; i < call.getArguments().size() - 1; i++) {
            if (isLibrary) {
                if (argObjs.get(i).isLiteral()) {
                    output.append(argObjs.get(i).getContext() + ", ");
                } else {
                    output.append(argRegisters.get(i) + ", ");
                }
            } else {
                if (argObjs.get(i).isLiteral()) {
                    output.append(method.getFormals().get(i).getName() + "=" + argObjs.get(i).getContext() + ", ");
                } else {
                    output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i) + ", ");
                }
            }
        }

        if (i < call.getArguments().size()) {
            if (isLibrary) {
                if (argObjs.get(i).isLiteral()) {
                    output.append(argObjs.get(i).getContext());
                } else {
                    output.append(argRegisters.get(i));
                }
            } else {
                if (argObjs.get(i).isLiteral()) {
                    output.append(method.getFormals().get(i).getName() + "=" + argObjs.get(i).getContext());
                } else {
                    output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i));
                }
            }
        }

        RetObject outputLir = new RetObject();

        if (method.getType().getName().equals("void")) {
            output.append("), Rdummy\n");
        } else {
            Register targetRegister = context.getAvailableRegiser();
            targetRegister.setValue(call.getName());
            output.append("), " + targetRegister + "\n");
            outputLir.setResult(targetRegister.toString());
            context.setTargetRegister(targetRegister);
        }

        outputLir.addLir(output.toString());
        outputLir.setContext(call.getName());
        outputLir.setWhoType(RetObject.WhoType.STATIC_METHOD);
        return outputLir;
	}

	@Override
	public RetObject visit(VirtualCall call, RegistersInformation context)
			throws Exception {

        String comment = withComments ? addLogComment(call.getLine(), call) : "";
        StringBuffer output = new StringBuffer();

        Register locRegister = context.getAvailableRegiser();
        List<RetObject> argObjs = new ArrayList<>();
        List<Register> argRegisters = new ArrayList<>();

        Register argRegister;
        for (Expression expr : call.getArguments()) {
            RetObject retArg = expr.accept(this, context);
            argRegister = context.getAvailableRegiser();
            argRegister.setValue(retArg.getContext());

            output.append("#\t\t" + expr.getClass().getSimpleName() + "\n");
            output.append(comment + retArg.getLir());

            if (retArg.getWhoType() != RetObject.WhoType.LITERAL) { // && retArg.getWhoType() != RetObject.WhoType.VAR
                output.append(comment + createMove(retArg) + " " + retArg.getResult() + ", " + argRegister + "\n");
            }

            argObjs.add(retArg);
            argRegisters.add(argRegister);
        }

        ClassLayout classLayout;

        if (call.isExternal()) {
            RetObject location = call.getLocation().accept(this, context);
            SymbolTableRow callSymbol = currentScope.lookup(location.getContext().toString());
            classLayout = manager.getLayout(callSymbol.getType().getName());

            output.append(comment + location.getLir());

            locRegister = context.getRegister("_DV_" + callSymbol.getType().getName());

            if (locRegister == null) {
                locRegister = context.getAvailableRegiser();
                locRegister.setValue("_DV_" + callSymbol.getType().getName());
                //output.append(comment + "MoveField " + locRegister.getValue() + ", " + locRegister + ".0\n");
            }

            output.append(comment + "StaticCall __checkNullRef(a=" + location.getResult() + "), Rdummy\n");
            output.append(comment + createMove(location) + " " + location.getResult() + ", " + locRegister + "\n");

        } else {
            classLayout = manager.getLayout(currentClassScope.getType().toString());
            output.append(comment + "Move this, " + locRegister + "\n");
            locRegister.setValue("_DV_" + classLayout.getName());
        }

        output.append(comment + "VirtualCall " + locRegister + ".");

        output.append(classLayout.getMethodIndex(call.getName()) + "(");
        Method method = classLayout.getMethodSymbol(call.getName());

        int i = 0;
        for (; i < call.getArguments().size() - 1; i++) {
            if (argObjs.get(i).isLiteral()) {
                output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i).getValue() + ", ");
            } else {
                output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i) + ", "); //
            }
        }

        if (i < call.getArguments().size()) {
            if (argObjs.get(i).isLiteral()) {
                output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i).getValue());
            } else {
                output.append(method.getFormals().get(i).getName() + "=" + argRegisters.get(i));
            }
        }

        RetObject outputLir = new RetObject();

        if (method.getType().getName().equals("void")) {
            output.append("), Rdummy");
        } else {
            Register targetRegister = context.getAvailableRegiser();
            targetRegister.setValue(call.getName());
            output.append("), " + targetRegister);
            outputLir.setResult(targetRegister.toString());
        }

        output.append("\n");

        outputLir.addLir(output.toString());
        outputLir.setContext(call.getClass());
        outputLir.setWhoType(RetObject.WhoType.VIRTUAL_METHOD);
		return outputLir;
	}

	@Override
	public RetObject visit(This thisExpression, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ?  addLogComment(thisExpression.getLine(), thisExpression) : "";

		StringBuffer output = new StringBuffer();
		Register reg = context.getRegister("this");
        if (reg == null) {
            reg = context.getAvailableRegiser();
            reg.setValue("this");
        }

		output.append(comment + "Move " + reg.getValue() + ", " + reg + "\n");
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext("$this");
		outputLir.setResult(reg.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(NewClass newClass, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(newClass.getLine(), newClass) : "";

		StringBuffer output = new StringBuffer();
		
		int size = manager.getLayout(newClass.getName()).getSize();
		
		Register classRegister = context.getAvailableRegiser();
		classRegister.setValue("_DV_" + newClass.getName());
		
		output.append(comment + "Library __allocateObject(" + size + "), " + classRegister + "\n");
		output.append(comment + "MoveField " + classRegister.getValue() + ", " + classRegister + ".0\n");
		
		//context.setTargetRegister(classRegister);
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext(newClass.getName());
        outputLir.setResult(classRegister.toString());
        outputLir.setWhoType(RetObject.WhoType.CLASS);
		return outputLir;
	}

	@Override
	public RetObject visit(NewArray newArray, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(newArray.getLine(), newArray) : "";

		StringBuffer output = new StringBuffer();
		RetObject sizeVar = newArray.getSize().accept(this, context);

        Register target = context.getTargetRegister();
        target.setValue("NewArray");

        output.append(comment + sizeVar.getLir());
        output.append(comment + createMove(sizeVar) + " " + sizeVar.getResult() + ", " + target + "\n");
        output.append(comment + "Mul 4, " + target + "\n");
        output.append(comment + "StaticCall __checkSize(n=" + target + "), Rdummy\n");
		output.append(comment + "Library __allocateArray(" + target + "), " + target + "\n");

		context.setTargetRegister(target);

		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext(target.getValue());
        outputLir.setResult(target.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(Length length, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(length.getLine(), length) : "";

		StringBuffer output = new StringBuffer();
		RetObject arrayLir = length.getArray().accept(this, context); 

		Register target = context.getRegister(arrayLir.getContext());

        if (target == null) {
            target = context.getAvailableRegiser();
            target.setValue(arrayLir.getContext());
        }

		output.append(comment + arrayLir.getLir());
        output.append(comment + createMove(arrayLir) + " " + arrayLir.getResult() + ", " + target + "\n");
        output.append(comment + "StaticCall __checkNullRef(a=" + target + "), Rdummy\n");
		output.append(comment + "ArrayLength " + target + ", " + target + "\n");
		
		context.setTargetRegister(target);
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
        outputLir.setContext(arrayLir.getContext());
        outputLir.setResult(target.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public RetObject visit(MathBinaryOp binaryOp, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(binaryOp.getLine(), binaryOp) : "";

		StringBuffer output = new StringBuffer();
		RetObject outputLir = new RetObject();
		String operation = null;

		RetObject firstOperand = binaryOp.getFirstOperand().accept(this, context);
        Register target = context.getAvailableRegiser();

        if (target == null) {
            target = context.getAvailableRegiser();
        }

        target.setValue(firstOperand.getContext());

        output.append(comment + firstOperand.getLir());
        if (firstOperand.isLiteral()) {
            output.append(comment + createMove(firstOperand) + " " + firstOperand.getContext() + ", " + target + "\t\t#TARGET\n");
        } else {
            output.append(comment + createMove(firstOperand) + " " + firstOperand.getResult() + ", " + target + "\t\t#TARGET\n");
        }

        RetObject secondOperand = binaryOp.getSecondOperand().accept(this, context);
        Register tempReg = context.getRegister(secondOperand.getContext().toString());

        if (tempReg == null) {
            tempReg = context.getAvailableRegiser();
        }

        tempReg.setValue(secondOperand.getResult());

        output.append(comment + secondOperand.getLir());
        if (secondOperand.isLiteral()) {
            output.append(comment + createMove(secondOperand) + " " + secondOperand.getContext() + ", " + tempReg + "\t\t#TEMP REG\n");
        } else {
            output.append(comment + createMove(secondOperand) + " " + secondOperand.getResult() + ", " + tempReg + "\t\t#TEMP REG\n");
        }

		if (firstOperand.getType() == TypeTable.stringType) {
            output.append(comment + "Library __stringCat(");
            if (firstOperand.isLiteral()) {
                output.append(target.getValue());
            } else {
                output.append(target);
            }

            output.append(", ");

            if (secondOperand.isLiteral()) {
                output.append(tempReg.getValue());
            } else {
                output.append(tempReg);
            }

            output.append("), " + target + "\n");

			outputLir.setType(TypeTable.stringType);
		} else {
			switch (binaryOp.getOperator()) {
			case DIVIDE:
				operation = "Div";
				break;
			case MINUS:
				operation = "Sub";
				break;
			case MOD:
				operation = "Mod";
				break;
			case MULTIPLY:
				operation = "Mul";
				break;
			case PLUS:
				operation = "Add";
				break;
			}
			
			outputLir.setType(TypeTable.intType);
            if (binaryOp.getOperator() == BinaryOps.DIVIDE) {
                output.append(comment + "StaticCall __checkZero(b=" + tempReg + "), Rdummy\n");
            }

            output.append(comment + operation + " " + tempReg + ", " + target + "\n");
		}
		
		context.setTargetRegister(target);
        context.freeRegister(tempReg);

        outputLir.addLir(output.toString());
		outputLir.setContext(target.getValue());
        outputLir.setResult(target.toString());
		outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(LogicalBinaryOp binaryOp, RegistersInformation context)
			throws Exception {

		String comment = withComments ? addLogComment(binaryOp.getLine(), binaryOp) : "";

		String jumpType = "JumpTrue";
		StringBuffer output = new StringBuffer();

        Register tempRegister1 = context.getAvailableRegiser();
        tempRegister1.setValue("temp1");

        Register tempRegister2 = context.getAvailableRegiser();
        tempRegister2.setValue("temp2");

        RetObject firstOperand = binaryOp.getFirstOperand().accept(this, context);
		output.append(comment + firstOperand.getLir());
        output.append(comment + createMove(firstOperand) + " " + firstOperand.getResult() + ", " + tempRegister1 + "\n");

        RetObject secondOperand = binaryOp.getSecondOperand().accept(this, context);
        output.append(comment + secondOperand.getLir());
        output.append(comment + createMove(secondOperand) + " " + secondOperand.getResult() + ", " + tempRegister2 + "\n");

        cmpCounter += 1;

        if (binaryOp.getOperator() != BinaryOps.LAND && binaryOp.getOperator() != BinaryOps.LOR) {
            output.append(comment + "Compare " + tempRegister2 + ", " + tempRegister1 + "\n");
        }

		switch (binaryOp.getOperator()) {
		case GT:
            output.append("JumpG _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
			break;
		case GTE:
            output.append("JumpGE _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
			break;
		case LT:
            output.append("JumpL _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
			break;
		case LTE:
            output.append("JumpLE _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
			break;
		case LOR:
            output.append(comment + "Compare 0, " + tempRegister1 + "\n");
            output.append(comment + "JumpFalse _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            output.append(comment + "Compare 0, " + tempRegister2 + "\n");
            output.append(comment + "JumpFalse _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
			break;
		case LAND:
            output.append(comment + "Compare 0, " + tempRegister1 + "\n");
            output.append(comment + "JumpTrue _if_false_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            output.append(comment + "Compare 0, " + tempRegister2 + "\n");
            output.append(comment + "JumpTrue _if_false_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            output.append(comment + "Jump _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            output.append(comment + "_if_false_label_" + ifCounterId + ifCounter + cmpCounter + ":\n");
            break;
        case EQUAL:
            output.append("JumpTrue _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            break;
        case NEQUAL:
            output.append("JumpFalse _if_true_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
            break;
		default:
			break;
		}

        output.append(comment + "Move 0, " + tempRegister1 + "\n");
        output.append(comment + "Jump _if_end_label_" + ifCounterId + ifCounter + cmpCounter + "\n");
        output.append(comment + "_if_true_label_" + ifCounterId + ifCounter + cmpCounter + ":\n");
        output.append(comment + "Move 1, " + tempRegister1 + "\n");
        output.append(comment + "_if_end_label_" + ifCounterId + ifCounter + cmpCounter + ":\n");
        output.append("\n");

		//context.freeRegister(tempRegister2);
		context.setTargetRegister(tempRegister1);

		RetObject outputLir = new RetObject();
        outputLir.addLir(output.toString());
        outputLir.setContext(tempRegister1.toString());
        outputLir.setResult(tempRegister1.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(MathUnaryOp unaryOp, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(unaryOp.getLine(), unaryOp) : "";

		StringBuffer output = new StringBuffer();

        RetObject unaryObj = unaryOp.getOperand().accept(this, context);

        Register target = context.getAvailableRegiser();
        target.setValue(unaryObj.getContext());

        output.append(comment + createMove(unaryObj) + " " + unaryObj.getResult() + ", " + target + "\n");
        output.append(comment + "Neg " + target + "\n");

        RetObject outputLir = new RetObject();
        outputLir.addLir(output.toString());
        outputLir.setContext(target.getValue());
        outputLir.setResult(target.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(LogicalUnaryOp unaryOp, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(unaryOp.getLine(), unaryOp) : "";

        StringBuffer output = new StringBuffer();

        RetObject operand = unaryOp.getOperand().accept(this, context);
        Register reg = context.getTargetRegister();
		reg.setValue(operand);
		
		output.append(comment + "Move  " + reg + ", " + reg.getValue() + " \n");
		output.append(comment + "Neg " + reg + "\n");
		
		RetObject outputLir = new RetObject();
		outputLir.addLir(output.toString());
		outputLir.setContext("MathUnaryOp");
        outputLir.setResult(reg.toString());
        outputLir.setWhoType(RetObject.WhoType.VAR);
		return outputLir;
	}

	@Override
	public RetObject visit(Literal literal, RegistersInformation context)
			throws Exception {
		
		String comment = withComments ? addLogComment(literal.getLine(), literal) : "";

		String strLitValue;
		StringBuffer output = new StringBuffer();
        RetObject outputLir = new RetObject();
		
		switch (literal.getType()) {
		case STRING:
			strLitValue = stringLiterals.addStringlIteral(literal.getValue().toString());
			outputLir.setContext(strLitValue);
			break;
		case INTEGER:
			outputLir.setContext(literal.getValue().toString());
			break;
		case FALSE:
			outputLir.setContext(0);
			break;
		case TRUE:
			outputLir.setContext(1);
			break;
		case NULL:
            outputLir.setContext(0);
            break;
            default:
                break;
        }

        outputLir.setType(CompilerUtils.literalTypeToMyType(literal.getType()));
        outputLir.setWhoType(RetObject.WhoType.LITERAL);
        outputLir.setResult(outputLir.getContext().toString());
        return outputLir;
	}

	@Override
	public RetObject visit(ExpressionBlock expressionBlock,
			RegistersInformation context) throws Exception {
		
		return expressionBlock.getExpression().accept(this, context);
	}

	@Override
	public RetObject visit(FieldOrMethod fieldOrMethod,
			RegistersInformation context) throws Exception {

		return new RetObject();
	}

    private String createMove(RetObject object) {
        String moveString;

        switch (object.getWhoType()) {
            case ARRAY:
                moveString = "MoveArray";
                break;
            case FIELD:
                moveString = "MoveField";
                break;
            default:
                moveString = "Move";
        }

        return moveString;
    }

    private void printToConsole(String print) {
        printToConsole(print, Colors.WHITE);
    }

    private void printToConsole(String print, Colors color) {
        System.out.println((char) 27 + "[" + color.getValue() + "m" + print + (char) 27 + "[0m");
    }

    private String addLogComment(int line, ASTNode comment) {
        return (char)27 + "[36m " + line + (char)27 + "[0m " +
               (char)27 + "[34m(" + comment.getClass().getSimpleName() + ")" + (char)27 + "[0m ";
    }

    private enum Colors {

        WHITE(0),
        RED(31),
        GREEN(32),
        YELLOW(33),
        BLUE(34),
        PINK(35),
        CYAN(36),
        GREY(37);

        private int value;

        Colors(int val) {
            this.value = val;
        }

        public int getValue() {
            return this.value;
        }
    }
}