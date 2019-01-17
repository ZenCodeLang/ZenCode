package live;

// TODO: write in ZenCode
public interface MutableLiveObject<T> extends LiveObject<T> {
	public void setValue(T value);
}
