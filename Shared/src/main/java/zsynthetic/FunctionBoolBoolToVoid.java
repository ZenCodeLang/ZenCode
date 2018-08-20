package zsynthetic;

@FunctionalInterface
public interface FunctionBoolBoolToVoid {
    void invoke(boolean oldValue, boolean newValue);
}
