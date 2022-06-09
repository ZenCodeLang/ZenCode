package org.openzen.zenscript.codemodel.type;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.TypeCompilingExpression;

import java.util.Optional;

public class TypeSymbol implements ISymbol {
	private final HighLevelDefinition definition;

	public TypeSymbol(HighLevelDefinition definition) {
		this.definition = definition;
	}

	@Override
	public CompilableExpression getExpression(CodePosition position, TypeID[] typeArguments) {
		return new CompilableExpression() {

			@Override
			public CodePosition getPosition() {
				return position;
			}

			@Override
			public CompilingExpression compile(ExpressionCompiler compiler) {
				return new TypeCompilingExpression(compiler, position, DefinitionTypeID.create(definition, typeArguments));
			}
		};
	}

	@Override
	public Optional<TypeID> getType(CodePosition position, TypeBuilder types, TypeID[] typeArguments) {
		return Optional.of(types.definitionOf(definition, typeArguments));
	}
}
