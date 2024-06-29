package org.openzen.zenscript.codemodel.expression;

import java.util.ArrayList;
import java.util.List;

public class LambdaClosure {
	public final List<CapturedExpression> captures = new ArrayList<>();

	public CapturedExpression add(CapturedExpression capture) {
		int i = captures.indexOf(capture);
		if (i != -1) {
			return captures.get(i);
		}
		captures.add(capture);
		return capture;
	}
}
