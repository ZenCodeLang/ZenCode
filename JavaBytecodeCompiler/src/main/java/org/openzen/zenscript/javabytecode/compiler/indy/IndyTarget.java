package org.openzen.zenscript.javabytecode.compiler.indy;

import org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("SpellCheckingInspection")
enum IndyTarget {
	INDY(Type.getMethodType(IndyHelpers.CALL_SITE_TYPE, IndyHelpers.METHOD_HANDLES_LOOKUP_TYPE, IndyHelpers.STRING_TYPE, IndyHelpers.METHOD_TYPE_TYPE), 0),
	CONDY(Type.getMethodType(IndyHelpers.OBJECT_TYPE, IndyHelpers.METHOD_HANDLES_LOOKUP_TYPE, IndyHelpers.STRING_TYPE, IndyHelpers.CLASS_TYPE), 1);

	private final Type expectedBasicSignature;
	private final int requiredParamCount;

	IndyTarget(final Type expectedBasicSignature, final int requiredParamCount) {
		this.expectedBasicSignature = expectedBasicSignature;
		this.requiredParamCount = requiredParamCount;
	}

	boolean verifySignature(final Type methodType, final boolean relax) {
		if (!this.expectedBasicSignature.getReturnType().equals(methodType.getReturnType())) {
			return false;
		}

		final Type[] expectedArgs = this.expectedBasicSignature.getArgumentTypes();
		final Type[] methodArgs = methodType.getArgumentTypes();
		if (expectedArgs.length > methodArgs.length && !relax) {
			return false;
		}
		if (methodArgs.length < this.requiredParamCount) {
			return false;
		}

		// If we want all expected arguments, we'd not be relaxing, thus we would have bailed beforehand
		for (int i = 0, m = Math.min(expectedArgs.length, methodArgs.length); i < m; ++i) {
			if (!expectedArgs[i].equals(methodArgs[i])) {
				return false;
			}
		}

		return true;
	}

	Type appendTypeArguments(final List<?> args) {
		return Type.getMethodType(
				this.expectedBasicSignature.getReturnType(),
				Stream.concat(Stream.of(this.expectedBasicSignature.getArgumentTypes()), Stream.of(IndyHelpers.toBsmArgTypes(args))).toArray(Type[]::new)
		);
	}
}
