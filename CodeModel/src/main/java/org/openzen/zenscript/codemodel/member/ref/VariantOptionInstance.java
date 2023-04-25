package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.compilation.*;
import org.openzen.zenscript.codemodel.compilation.expression.AbstractCompilingExpression;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.ssa.CodeBlockStatement;
import org.openzen.zenscript.codemodel.ssa.SSAVariableCollector;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public class VariantOptionInstance implements CompilableExpression, ResolvedType.SwitchMember {
	public final TypeID variant;
	public final TypeID[] types;
	private final VariantDefinition.Option option;

	public VariantOptionInstance(VariantDefinition.Option option, TypeID variant, TypeID[] types) {
		this.option = option;
		this.variant = variant;
		this.types = types;
	}

	public String getName() {
		return option.name;
	}

	public TypeID getParameterType(int index) {
		return types[index];
	}

	public <T extends Tag> T getTag(Class<T> type) {
		return option.getTag(type);
	}

	public int getOrdinal() {
		return option.ordinal;
	}

	public VariantDefinition.Option getOption() {
		return option;
	}

	@Override
	public CodePosition getPosition() {
		return option.position;
	}

	@Override
	public CompilingExpression compile(ExpressionCompiler compiler) {
		return new AbstractCompilingExpression(compiler, option.position) {
			@Override
			public Expression eval() {
				if (types.length == 0) {
					return new VariantValueExpression(position, variant, VariantOptionInstance.this);
				} else {
					return new InvalidExpression(position, variant, CompileErrors.variantValueWithoutArguments());
				}
			}

			@Override
			public Optional<CompilingCallable> call() {
				return Optional.of(new OptionCallable(compiler, variant, VariantOptionInstance.this));
			}

			@Override
			public void collect(SSAVariableCollector collector) {}

			@Override
			public void linkVariables(CodeBlockStatement.VariableLinker linker) {}
		};
	}

	@Override
	public SwitchValue toSwitchValue(String[] bindingNames) {
		return new VariantOptionSwitchValue(this, bindingNames);
	}

	private static class OptionCallable implements CompilingCallable {
		private final ExpressionCompiler compiler;
		private final TypeID variant;
		private final VariantOptionInstance option;

		public OptionCallable(ExpressionCompiler compiler, TypeID variant, VariantOptionInstance option) {
			this.compiler = compiler;
			this.variant = variant;
			this.option = option;
		}

		@Override
		public Expression call(CodePosition position, CompilingExpression[] arguments) {
			if (arguments.length != option.types.length) {
				return compiler.at(position).invalid(CompileErrors.invalidNumberOfArguments(arguments.length, option.types.length));
			}

			Expression[] compiledArguments = new Expression[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				compiledArguments[i] = arguments[i].cast(CastedEval.implicit(compiler, position, option.types[i])).value;
			}
			return new VariantValueExpression(position, variant, option, compiledArguments);
		}

		@Override
		public CastedExpression casted(CodePosition position, CastedEval cast, CompilingExpression[] arguments) {
			return cast.of(call(position, arguments));
		}
	}
}
