package zsynthetic;

public final class Shared<T extends AutoCloseable> {
	private final T value;
	private int refcount = 1;

	public Shared(T value) {
		this.value = value;
	}

	public synchronized void addRef() {
		refcount++;
	}

	public synchronized void release() {
		refcount--;
		if (refcount == 0) {
			try {
				value.close();
			} catch (Exception ex) {
			}
		}
	}
}
