package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.NewExpression;

public class EnumConstantMember {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	public final String name;
	public final int ordinal;

	public Expression value = null;
	public NewExpression constructor = null;

	public EnumConstantMember(CodePosition position, HighLevelDefinition definition, String name, int ordinal) {
		this.position = position;
		this.definition = definition;
		this.name = name;
		this.ordinal = ordinal;
	}
}
