package org.openzen.zenscript.javabytecode.compiler.lambda.capturing;

import org.objectweb.asm.Type;

public final class LambdaCaptureData {
	private final int position;
	private final Type type;
	private final String name;

	private LambdaCaptureData(final int position, final Type type, final String name) {
		this.position = position;
		this.type = type;
		this.name = name;
	}

	static LambdaCaptureData of(final int position, final Type type, final String name) {
		return new LambdaCaptureData(position, type, name);
	}

	public int position() {
		return this.position;
	}

	public Type type() {
		return this.type;
	}

	public String name() {
		return this.name;
	}
}
