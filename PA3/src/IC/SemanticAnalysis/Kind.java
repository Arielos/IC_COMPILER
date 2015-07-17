package IC.SemanticAnalysis;

public enum Kind {

	CLASS(0), FIELD(1), METHOD(2), PARAM(3), VAR(4), BLOCK(5), RET_VAR(6), THIS(7);

	private int value;

	Kind(int value) {
		this.value = value;
	}
}
