package org.openzen.zenscript.codemodel.annotations;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;

public class InvalidMemberAnnotation implements MemberAnnotation {
	public final CodePosition position;
	public final CompileError error;

	public InvalidMemberAnnotation(CodePosition position, CompileError error) {
		this.position = position;
		this.error = error;
	}

	public InvalidMemberAnnotation(CompileException ex) {
		this.position = ex.position;
		this.error = ex.error;
	}

	@Override
	public AnnotationDefinition getDefinition() {
		return InvalidAnnotationDefinition.INSTANCE;
	}

	@Override
	public void apply(IDefinitionMember member) {

	}

	@Override
	public void applyOnOverridingMethod(FunctionalMember member) {

	}

	@Override
	public void applyOnOverridingGetter(GetterMember member) {

	}

	@Override
	public void applyOnOverridingSetter(SetterMember member) {

	}

	@Override
	public void serialize(CodeSerializationOutput output, IDefinitionMember member, TypeSerializationContext context) {
		output.serialize(position);
		output.writeUInt(error.code.ordinal());
		output.writeString(error.description);
	}
}
