package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.NewExpression;

public class EnumConstantMember {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	public final String name;
	public final String fieldName;
	public final int ordinal;

	public Expression value = null;
	public NewExpression constructor = null;

	public EnumConstantMember(CodePosition position, HighLevelDefinition definition, String name, String fieldName, int ordinal) {
		this.position = position;
		this.definition = definition;
		this.name = name;
		this.fieldName = fieldName;
		this.ordinal = ordinal;
	}
}
