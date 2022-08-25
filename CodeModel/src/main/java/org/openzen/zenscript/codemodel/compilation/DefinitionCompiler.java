package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.context.CompilingPackage;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.type.TypeID;

public interface DefinitionCompiler extends TypeResolver {
	CompilingPackage getPackage();

	TypeBuilder types();

    MemberCompiler forMembers(TypeSymbol compiled);

    StatementCompiler forScripts(FunctionHeader scriptHeader);
}
