package IC.AST;

import java.util.ArrayList;
import java.util.List;

public class FieldOrMethod extends ASTNode {

	private List<Field> fields;
	private List<Method> methods;

	public FieldOrMethod(int line) {
		super(line);
		this.fields = new ArrayList<Field>();
		this.methods = new ArrayList<Method>();
	}

	@Override
	public Object accept(Visitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) throws Exception {
		return visitor.visit(this, context);
	}

	public List<Field> getFields() {
		return fields;
	}

	public List<Method> getMethods() {
		return methods;
	}

	public void addFields(List<Field> f) {
		fields.addAll(f);
	}

	public void addMethod(Method m) {
		methods.add(m);
	}
}
