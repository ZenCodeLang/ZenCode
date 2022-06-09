package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.FunctionScope;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedGetter extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final ParsedImplementation implementation;
	private final ParsedFunctionBody body;

	private final String name;
	private final IParsedType type;
	private GetterMember compiled;
	private boolean isCompiled = false;

	public ParsedGetter(
			CodePosition position,
			HighLevelDefinition definition,
			ParsedImplementation implementation,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedFunctionBody body) {
		super(definition, annotations);

		this.implementation = implementation;
		this.position = position;
		this.modifiers = modifiers;
		this.body = body;

		this.name = name;
		this.type = type;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new GetterMember(position, definition, modifiers, name, type.compile(context), null);
	}

	@Override
	public GetterMember getCompiled() {
		return compiled;
	}

	private void inferHeaders(BaseScope scope) throws CompileException {
		if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
			fillOverride(scope, implementation.getCompiled().type);
		} else if (implementation == null && Modifiers.isOverride(modifiers)) {
			if (definition.getSuperType() == null)
				throw new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type");

			fillOverride(scope, definition.getSuperType());
		}

		if (compiled == null)
			throw new IllegalStateException("Types not yet linked");
	}

	private void fillOverride(TypeScope scope, TypeID baseType) {
		scope.getTypeMembers(baseType).getGroup(name)
				.flatMap(TypeMemberGroup::getGetter)
				.ifPresent(getter -> compiled.setOverrides(getter));
	}

	@Override
	public final void compile(BaseScope scope) throws CompileException {
		if (isCompiled)
			return;
		isCompiled = true;

		inferHeaders(scope);

		FunctionHeader header = new FunctionHeader(compiled.getType());
		FunctionScope innerScope = new FunctionScope(position, scope, header);
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, getCompiled(), scope);
		compiled.setBody(body.compile(innerScope, header));
	}
}
