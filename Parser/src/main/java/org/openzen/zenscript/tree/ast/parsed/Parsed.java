package org.openzen.zenscript.tree.ast.parsed;

public class Parsed<T> {

	private final T thing;

	public Parsed(T thing) {
		this.thing = thing;
	}

	public T thing() {
		return thing;
	}

	public <U> U cast() {
		return (U) this.thing();
	}

}
