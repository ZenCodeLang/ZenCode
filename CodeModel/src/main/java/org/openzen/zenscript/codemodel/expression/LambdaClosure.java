package org.openzen.zenscript.codemodel.expression;

import java.util.*;

public class LambdaClosure {
	public final Collection<CapturedExpression> captures = new LinkedHashSet<>();

	public void add(CapturedExpression capture) {
		if(!captures.contains(capture)) {
			captures.add(capture);
		}
	}
}
