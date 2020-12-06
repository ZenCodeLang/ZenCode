package live;

import listeners.DummyListenerHandle;
import listeners.ListenerHandle;
import zsynthetic.FunctionBoolBoolToVoid;

public final class ImmutableLiveBool implements LiveBool {
	public static final ImmutableLiveBool TRUE = new ImmutableLiveBool(true);
	public static final ImmutableLiveBool FALSE = new ImmutableLiveBool(false);
	public final boolean value;

	private ImmutableLiveBool(boolean value) {
		this.value = value;
	}

	@Override
	public ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener) {
		return new DummyListenerHandle<FunctionBoolBoolToVoid>(listener);
	}

	public boolean getValue() {
		return value;
	}
}
