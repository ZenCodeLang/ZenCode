package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.member.ImplementationMember;

public interface CompilableMember {
	CompilingMember compile(
			HighLevelDefinition definition,
			ImplementationMember implementation,
			MemberCompiler compiler
	);
}
