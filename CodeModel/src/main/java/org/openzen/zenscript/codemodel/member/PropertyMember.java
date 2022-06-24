package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

public abstract class PropertyMember extends DefinitionMember {
	public final BuiltinID builtin;
	protected TypeID type;

	public PropertyMember(CodePosition position, HighLevelDefinition definition, Modifiers modifiers, TypeID type, BuiltinID builtin) {
		super(position, definition, modifiers);

		if (type == null)
			throw new NullPointerException();

		this.type = type;
		this.builtin = builtin;
	}

	public TypeID getType() {
		return type;
	}

	public void setType(TypeID type) {
		if (type == null)
			throw new NullPointerException();

		this.type = type;
	}

	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}
}
