package org.openzen.zenscript.codemodel.globals;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.TypeCompilingExpression;
import org.openzen.zenscript.codemodel.globals.IGlobal;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class TypeGlobal implements IGlobal {
	private final HighLevelDefinition definition;

	public TypeGlobal(HighLevelDefinition definition) {
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
