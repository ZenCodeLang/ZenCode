package zsynthetic;

@FunctionalInterface
public interface FunctionIntIntToVoid {
    void invoke(int oldValue, int newValue);
}
