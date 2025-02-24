package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javashared.JavaClass;

public final class LambdaClosureInfo {
	private final LambdaClosure closure;
	private final TypeID thisCapture;
	private final boolean isDifferent;

	private LambdaClosureInfo(final LambdaClosure closure, final TypeID thisCapture, final boolean isDifferent) {
		this.closure = closure;
		this.thisCapture = thisCapture;
		this.isDifferent = isDifferent;
	}

	static LambdaClosureInfo from(final JavaBytecodeContext context, final LambdaClosure closure, final JavaClass thisClass) {
		CapturedThisExpression capturedThis = null;
		for (final CapturedExpression capture : closure.captures) {
			if (capture instanceof CapturedThisExpression) {
				capturedThis = (CapturedThisExpression) capture;
				break;
			}
		}

		final TypeID thisType = capturedThis == null? null : capturedThis.type;
		final boolean isDifferent = capturedThis != null && !thisClass.internalName.equals(context.getInternalName(capturedThis.type));

		return new LambdaClosureInfo(closure, thisType, isDifferent);
	}

	public LambdaClosure closure() {
		return this.closure;
	}

	public TypeID thisType() {
		return this.thisCapture;
	}

	public boolean isDifferentThis() {
		return this.isDifferent;
	}
}
