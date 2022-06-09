package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Taggable;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;

public abstract class DefinitionMember extends Taggable implements IDefinitionMember {
	public final CodePosition position;
	public final HighLevelDefinition definition;
	protected final int modifiers;
	public MemberAnnotation[] annotations = MemberAnnotation.NONE;

	public DefinitionMember(CodePosition position, HighLevelDefinition definition, int modifiers) {
		this.position = position;
		this.definition = definition;
		this.modifiers = modifiers;
	}

	@Override
	public final CodePosition getPosition() {
		return position;
	}

	@Override
	public int getSpecifiedModifiers() {
		return modifiers;
	}

	@Override
	public final HighLevelDefinition getDefinition() {
		return definition;
	}

	@Override
	public MemberAnnotation[] getAnnotations() {
		return annotations;
	}

	@Override
	public String toString() {
		return describe();
	}

	public boolean isStatic() {
		return Modifiers.isStatic(getEffectiveModifiers());
	}

	public boolean isFinal() {
		return Modifiers.isFinal(getEffectiveModifiers());
	}

	public boolean isExtern() {
		return Modifiers.isExtern(getEffectiveModifiers());
	}

	public boolean isPrivate() {
		return Modifiers.isPrivate(getEffectiveModifiers());
	}

	public boolean isPublic() {
		return Modifiers.isPublic(getEffectiveModifiers());
	}

	public boolean isProtected() {
		return Modifiers.isProtected(getEffectiveModifiers());
	}

	public boolean isImplicit() {
		return Modifiers.isImplicit(getEffectiveModifiers());
	}
}
