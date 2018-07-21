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
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedOperator extends ParsedFunctionalMember {
	private final OperatorType operator;
	private final ParsedFunctionHeader header;
	private OperatorMember compiled;
	
	public ParsedOperator(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			OperatorType operator,
			ParsedFunctionHeader header,
			ParsedFunctionBody body)
	{
		super(position, definition, implementation, modifiers, annotations, body);
		
		this.operator = operator;
		this.header = header;
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new OperatorMember(position, definition, modifiers, operator, header.compile(scope), null);
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}

	@Override
	protected void fillOverride(TypeScope scope, ITypeID baseType, PrecompilationState state) {
		DefinitionMemberGroup group = scope.getTypeMembers(baseType).getOrCreateGroup(operator);
		FunctionalMemberRef override = group.getOverride(position, scope, compiled);
		if (override == null)
			return;
		
		if (override.getHeader().hasUnknowns) {
			if (!state.precompile(override.getTarget()))
				throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Precompilation failed; could not complete method header");
			
			override = scope.getTypeMembers(baseType)
				.getOrCreateGroup(operator)
				.getOverride(position, scope, compiled); // to refresh the header
		}
		
		compiled.setOverrides(scope.getTypeRegistry(), override);
	}
}
