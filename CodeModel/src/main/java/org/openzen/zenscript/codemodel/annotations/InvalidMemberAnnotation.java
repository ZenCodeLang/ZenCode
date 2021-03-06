package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;

public class InvalidMemberAnnotation implements MemberAnnotation {
	public final CodePosition position;
	public final CompileExceptionCode code;
	public final String message;

	public InvalidMemberAnnotation(CompileException ex) {
		this.position = ex.position;
		this.code = ex.code;
		this.message = ex.getMessage();
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(IDefinitionMember member, BaseScope scope) {

	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member, BaseScope scope) {

	}

	@Override
	public void applyOnOverridingGetter(GetterMember member, BaseScope scope) {

	}

	@Override
	public void applyOnOverridingSetter(SetterMember member, BaseScope scope) {

	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeContext context) {
		output.serialize(position);
		output.writeUInt(code.ordinal());
		output.writeString(message);
	}
}
