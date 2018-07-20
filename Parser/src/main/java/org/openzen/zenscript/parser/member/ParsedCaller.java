/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCaller extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;
	private CallerMember compiled;
	
	public ParsedCaller(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionHeader header,
			ParsedFunctionBody body) {
		super(position, definition, implementation, modifiers, annotations, body);
		
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new CallerMember(position, definition, modifiers, header.compile(scope), null);
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}

	@Override
	protected void fillOverride(TypeScope scope, ITypeID baseType, PrecompilationState state) {
		FunctionalMemberRef base = scope.getTypeMembers(baseType)
				.getOrCreateGroup(OperatorType.CALL)
				.getOverride(position, scope, compiled);
		if (base.header.hasUnknowns) {
			if (!state.precompile(base.getTarget()))
				throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Precompilation failed; could not complete method header");
			
			base = scope.getTypeMembers(baseType)
				.getOrCreateGroup(OperatorType.CALL)
				.getOverride(position, scope, compiled); // to refresh the header
		}
		
		compiled.setOverrides(scope.getTypeRegistry(), base);
	}
}
