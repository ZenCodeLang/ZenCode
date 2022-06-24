package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.compilation.CompilingDefinition;
import org.openzen.zenscript.codemodel.compilation.CompilingMember;
import org.openzen.zenscript.codemodel.compilation.MemberCompiler;
import org.openzen.zenscript.codemodel.identifiers.MethodSymbol;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.statements.ParsedFunctionBody;

import java.util.List;
import java.util.Optional;

public class ParsedCaller extends ParsedFunctionalMember {
	private final ParsedFunctionHeader header;
	private CallerMember compiled;

	public ParsedCaller(
			CodePosition position,
			int modifiers,
			ParsedAnnotation[] annotations,
			ParsedFunctionHeader header,
			ParsedFunctionBody body) {
		super(position, modifiers, annotations, body);

		this.header = header;
	}

	@Override
	protected void fillOverride(MemberCompiler compiler, TypeID baseType) throws CompileException {
		FunctionalMemberRef base = scope.getTypeMembers(baseType)
				.getOrCreateGroup(OperatorType.CALL)
				.getOverride(position, scope, compiled);
		if (base.getHeader().hasUnknowns) {
			scope.getPreparer().prepare(base.getTarget());
			base = scope.getTypeMembers(baseType)
					.getOrCreateGroup(OperatorType.CALL)
					.getOverride(position, scope, compiled); // to refresh the header
		}

		compiled.setOverrides(scope.getTypeRegistry(), base);
	}

	@Override
	public CompilingMember compile(
			HighLevelDefinition definition,
			ImplementationMember implementation,
			MemberCompiler compiler
	) {
		return new Compiling(compiler, definition, implementation);
	}

	private class Compiling implements CompilingMember {
		private final MemberCompiler compiler;
		private final HighLevelDefinition definition;
		private final ImplementationMember implementation;

		public Compiling(MemberCompiler compiler, HighLevelDefinition definition, ImplementationMember implementation) {
			this.compiler = compiler;
			this.definition = definition;
			this.implementation = implementation;
		}

		@Override
		public void linkTypes() {
			compiled = new CallerMember(position, definition, new Modifiers(modifiers), header.compile(compiler.types()), null);
		}

		@Override
		public void prepare(List<CompileException> errors) {
			inferHeaders(errors);
		}

		@Override
		public void compile(List<CompileException> errors) {

		}

		private void inferHeaders(List<CompileException> errors) {
			if ((implementation != null && !Modifiers.isPrivate(modifiers))) {
				fillOverride(implementation.type);
			} else if (implementation == null && Modifiers.isOverride(modifiers)) {
				if (definition.getSuperType() == null) {
					errors.add(new CompileException(position, CompileExceptionCode.OVERRIDE_WITHOUT_BASE, "Override specified without base type"));
					return;
				}

				fillOverride(definition.getSuperType());
			}
		}

		private boolean fillOverride(TypeID baseType) {
			Optional<MethodSymbol> base = compiler.resolve(baseType)
							.findOperator(OperatorType.CALL)
							.flatMap(operator -> operator.findOverriddenMethod(compiled.header));


			compiled.setOverrides(base);
			return base.isPresent();
		}
	}
}
