package IC.LirTranslate;

public class NodeWeight {

    private int weight;
    private String name;

    public NodeWeight() {

    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int value) {
        this.weight = value;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    @Override
    public String toString() {
        return "[Name: " + name + " Weight: " + weight + "]";
    }
}
