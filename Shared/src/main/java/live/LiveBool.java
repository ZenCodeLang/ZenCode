package live;

import listeners.ListenerHandle;
import zsynthetic.FunctionBoolBoolToVoid;

public interface LiveBool {
	boolean getValue();

	ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener);
}
