package util.semigroup;

public class IntSum implements Semigroup<IntSum> {
    private int i;

    public IntSum(int i) {
        this.i = i;
    }

    public IntSum() {
        this(0);
    }

    public int get() {
        return i;
    }

    @Override
    public void append(IntSum other) {
        this.i += other.i;
    }

    @Override
    public String toString() {
        return "" + i;
    }
}
