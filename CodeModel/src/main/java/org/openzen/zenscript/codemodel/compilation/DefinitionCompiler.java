package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface DefinitionCompiler {
	TypeBuilder types();

	ResolvedType resolve(TypeID type);

    MemberCompiler forMembers(TypeSymbol compiled);

    StatementCompiler forScripts(FunctionHeader scriptHeader);
}
