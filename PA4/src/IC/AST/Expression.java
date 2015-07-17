package IC.AST;

/**
 * Abstract base class for expression AST nodes.
 * 
 * @author Tovi Almozlino
 */
public abstract class Expression extends ASTNode {

	/**
	 * Constructs a new expression node. Used by subclasses.
	 * 
	 * @param line
	 *            Line number of expression.
	 */
	protected Expression(int line) {
		super(line);
        this.sideEffect = false;
	}

    private int weight;

    private boolean sideEffect;

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int value) {
        this.weight = value;
    }

    public boolean canCouseSideEffects() {
        return this.sideEffect;
    }

    public void setAsSideEffect() {
        this.sideEffect = true;
    }
}