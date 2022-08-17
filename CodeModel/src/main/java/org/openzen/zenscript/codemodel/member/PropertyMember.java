package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.type.TypeID;

public abstract class PropertyMember extends DefinitionMember {
	protected TypeID type;

	public PropertyMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, TypeID type) {
		super(position, definition, modifiers);

		if (type == null)
			throw new NullPointerException();

		this.type = type;
	}

	public TypeID getType() {
		return type;
	}

	public void setType(TypeID type) {
		if (type == null)
			throw new NullPointerException();

		this.type = type;
	}
}
