package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

public interface MemberAnnotation {
	MemberAnnotation[] NONE = new MemberAnnotation[0];

	AnnotationDefinition getDefinition();

	void apply(IDefinitionMember member);

	void applyOnOverridingMethod(FunctionalMember member);

	void applyOnOverridingGetter(GetterMember member);

	void applyOnOverridingSetter(SetterMember member);

	void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeSerializationContext context);
}
