package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.InstanceCallable;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.compilation.ResolvedType;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

public class ParsedMethod extends ParsedFunctionalMember {
	private final String name;
	private final ParsedFunctionHeader header;
	private MethodMember compiled;

	public ParsedMethod(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			ParsedFunctionHeader header,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.name = name;
		this.header = header;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new MethodMember(position, definition, modifiers, name, header.compile(context), null);
	}

	@Override
	public FunctionalMember getCompiled() {
		return compiled;
	}

	@Override
	protected void fillOverride(MemberCompiler compiler, TypeID baseType) throws CompileException {
		ResolvedType type = compiler.resolve(baseType);
		InstanceCallable method = type.findMethod(name).orElseThrow(() -> new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Could not determine overridden method"));

		TypeMembers typeMembers = scope.getTypeMembers(baseType);
		FunctionalMemberRef override = typeMembers
				.getOrCreateGroup(name, false)
				.getOverride(position, scope, compiled);
		if (override == null)
			throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Could not determine overridden method");
		if (override.getHeader().hasUnknowns) {
			scope.getPreparer().prepare(override.getTarget());
			override = scope.getTypeMembers(baseType)
					.getOrCreateGroup(name, false)
					.getOverride(position, scope, compiled); // to refresh the header
		}

		compiled.setOverrides(override);
	}
}
