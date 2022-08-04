package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

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
	public void apply(IDefinitionMember member) {
		member.setTag(NativeTag.class, new NativeTag(identifier));
	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member) {
		// not inherited
	}

	@Override
	public void applyOnOverridingGetter(GetterMember member) {
		// not inherited
	}

	@Override
	public void applyOnOverridingSetter(SetterMember member) {
		// not inherited
	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeSerializationContext context) {
		output.writeString(identifier);
	}
}
