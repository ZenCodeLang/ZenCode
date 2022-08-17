package org.openzen.zenscript.codemodel.globals;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.compilation.CompilableExpression;
import org.openzen.zenscript.codemodel.compilation.CompilingExpression;
import org.openzen.zenscript.codemodel.compilation.ExpressionCompiler;
import org.openzen.zenscript.codemodel.compilation.TypeBuilder;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class ExpressionGlobal implements IGlobal {
	private final Resolver resolver;

	public ExpressionGlobal(Resolver resolver) {
		this.resolver = resolver;
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
				return resolver.resolve(compiler, position, typeArguments);
			}
		};
	}

	@Override
	public Optional<TypeID> getType(CodePosition position, TypeBuilder types, TypeID[] typeArguments) {
		return Optional.empty();
	}

	public interface Resolver {
		CompilingExpression resolve(ExpressionCompiler compiler, CodePosition position, TypeID[] typeArguments);
	}
}
