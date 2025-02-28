package org.openzen.zenscript.javabytecode.compiler.lambda;

import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;

public final class LambdaClosureInfo {
	private final LambdaClosure closure;
	private final TypeID thisCapture;

	private LambdaClosureInfo(final LambdaClosure closure, final TypeID thisCapture) {
		this.closure = closure;
		this.thisCapture = thisCapture;
	}

	static LambdaClosureInfo from(final JavaBytecodeContext context, final LambdaClosure closure, final JavaClass thisClass) {
		CapturedThisExpression capturedThis = null;
		for (final CapturedExpression capture : closure.captures) {
			if (capture instanceof CapturedThisExpression) {
				capturedThis = (CapturedThisExpression) capture;
				break;
			}
		}

		final TypeID thisType = computeCapturedThisType(capturedThis);
		return new LambdaClosureInfo(closure, thisType);
	}

	private static TypeID computeCapturedThisType(final CapturedThisExpression expression) {
		if (expression != null) {
			return expression.type;
		}

		// TODO("Make this default to void")
		return null;
	}

	public LambdaClosure closure() {
		return this.closure;
	}

	public TypeID thisType() {
		return this.thisCapture;
	}
}
