/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package live;

import listeners.ListenerHandle;
import listeners.ListenerList;
import zsynthetic.FunctionBoolBoolToVoid;

import java.util.function.BiConsumer;
import java.util.function.Predicate;

// TODO: rewrite to zencode
public class LivePredicateBool<T> implements LiveBool, AutoCloseable, BiConsumer<T, T> {
	private final ListenerList<FunctionBoolBoolToVoid> listeners = new ListenerList<>();
	private final LiveObject<T> source;
	private final Predicate<T> predicate;
	private final ListenerHandle<BiConsumer<T, T>> sourceListener;
	private boolean value;

	public LivePredicateBool(LiveObject<T> source, Predicate<T> predicate) {
		this.source = source;
		this.predicate = predicate;
		this.sourceListener = source.addListener(this);
		setValueInternal(predicate.test(source.getValue()));
	}

	@Override
	public void close() {
		sourceListener.close();
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public ListenerHandle<FunctionBoolBoolToVoid> addListener(FunctionBoolBoolToVoid listener) {
		return listeners.add(listener);
	}

	private void setValueInternal(boolean value) {
		if (value == this.value)
			return;

		boolean oldValue = this.value;
		this.value = value;
		listeners.accept(listener -> listener.invoke(oldValue, value));
	}

	@Override
	public void accept(T oldValue, T newValue) {
		setValueInternal(predicate.test(newValue));
	}
}
