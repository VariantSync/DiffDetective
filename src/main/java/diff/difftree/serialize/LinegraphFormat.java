package diff.difftree.serialize;

public interface LinegraphFormat {
    default String getName() {
        return this.getClass().getName();
    }
}
