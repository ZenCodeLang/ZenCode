/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedDestructor extends ParsedFunctionalMember {
	private DestructorMember compiled;
	
	public ParsedDestructor(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionBody body) {
		super(position, definition, implementation, modifiers, annotations, body);
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new DestructorMember(position, definition, modifiers);
	}

	@Override
	public DestructorMember getCompiled() {
		return compiled;
	}

	@Override
	protected void fillOverride(TypeScope scope, StoredType baseType) throws CompileException {
		compiled.overrides = scope.getTypeMembers(baseType).getOrCreateGroup(OperatorType.DESTRUCTOR).getOverride(position, scope, compiled);
	}
}
