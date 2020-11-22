package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;

public interface TypeMemberPreparer {
	void prepare(IDefinitionMember member) throws CompileException;
}
