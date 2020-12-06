package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

public class NativeMemberAnnotation implements MemberAnnotation {
	private final String identifier;

	public NativeMemberAnnotation(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return NativeAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(IDefinitionMember member, BaseScope scope) {
		member.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope) {
		// not inherited
	}

	@Override
	public void applyOnOverridingGetter(GetterMember member, BaseScope scope) {
		// not inherited
	}

	@Override
	public void applyOnOverridingSetter(SetterMember member, BaseScope scope) {
		// not inherited
	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeContext context) {
		output.writeString(identifier);
	}
}
