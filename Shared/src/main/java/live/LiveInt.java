package live;

import listeners.ListenerHandle;
import zsynthetic.FunctionIntIntToVoid;

public interface LiveInt {
	int getValue();

	void setValue(int value);

	ListenerHandle<FunctionIntIntToVoid> addListener(FunctionIntIntToVoid listener);
}
