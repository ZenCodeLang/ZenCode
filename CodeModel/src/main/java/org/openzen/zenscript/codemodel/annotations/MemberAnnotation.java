package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

public interface MemberAnnotation {
	MemberAnnotation[] NONE = new MemberAnnotation[0];
	
	AnnotationDefinition getDefinition();
	
	void apply(IDefinitionMember member, BaseScope scope);
	
	void applyOnOverridingMethod(FunctionalMember member, BaseScope scope);
	
	void applyOnOverridingGetter(GetterMember member, BaseScope scope);
	
	void applyOnOverridingSetter(SetterMember member, BaseScope scope);
	
	void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeContext context);
}
