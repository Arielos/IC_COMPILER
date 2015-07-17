package IC.SemanticAnalysis;

import IC.AST.Field;
import IC.AST.Formal;
import IC.AST.ICClass;
import IC.AST.If;
import IC.AST.LibraryMethod;
import IC.AST.LocalVariable;
import IC.AST.PrimitiveType;
import IC.AST.Program;
import IC.AST.StatementsBlock;
import IC.AST.StaticMethod;
import IC.AST.UserType;
import IC.AST.VirtualMethod;
import IC.AST.While;

public interface ScopeVisitor {

	public Object visit(Program program, SymbolTable scope);

	public Object visit(ICClass icClass, SymbolTable scope);

	public Object visit(Field field, SymbolTable scope);

	public Object visit(VirtualMethod method, SymbolTable scope);

	public Object visit(StaticMethod method, SymbolTable scope);

	public Object visit(LibraryMethod method, SymbolTable scope);

	public Object visit(Formal formal, SymbolTable scope);

	public Object visit(PrimitiveType type, SymbolTable scope);

	public Object visit(UserType type, SymbolTable scope);

	public Object visit(If ifStatement, SymbolTable scope);

	public Object visit(While whileStatement, SymbolTable scope);

	public Object visit(StatementsBlock statementsBlock, SymbolTable scope);

	public Object visit(LocalVariable localVariable, SymbolTable scope);
}
