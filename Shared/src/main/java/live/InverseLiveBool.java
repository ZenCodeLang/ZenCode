package live;

import listeners.ListenerHandle;
import zsynthetic.FunctionBoolBoolToVoid;

public final class InverseLiveBool implements LiveBool {
	private final LiveBool source;

	public InverseLiveBool(LiveBool source) {
		this.source = source;
	}

	@Override
	public boolean getValue() {
		return !source.getValue();
	}

	@Override
	public ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener) {
		return source.addListener((oldVal, newVal) ->
				listener.invoke(!oldVal, !newVal));
	}
}
