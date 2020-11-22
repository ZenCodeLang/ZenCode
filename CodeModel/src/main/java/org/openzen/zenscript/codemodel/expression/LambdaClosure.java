package org.openzen.zenscript.codemodel.expression;

import java.util.ArrayList;
import java.util.List;

public class LambdaClosure {
	public final List<CapturedExpression> captures = new ArrayList<>();
	
	public void add(CapturedExpression capture) {
		captures.add(capture);
	}
}
