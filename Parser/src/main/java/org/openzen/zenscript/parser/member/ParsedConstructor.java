package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

public class ParsedConstructor extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;
	private ConstructorMember compiled;

	public ParsedConstructor(CodePosition position, HighLevelDefinition definition, ParsedImplementation implementation, int modifiers, ParsedAnnotation[] annotations, ParsedFunctionHeader header, ParsedFunctionBody body) {
		super(position, definition, implementation, modifiers, annotations, body);

		this.header = header;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new ConstructorMember(position, definition, modifiers, header.compile(context), null);
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}

	@Override
	protected void fillOverride(TypeScope scope, TypeID baseType) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.OVERRIDE_CONSTRUCTOR, "Cannot override a constructor");
	}
}
