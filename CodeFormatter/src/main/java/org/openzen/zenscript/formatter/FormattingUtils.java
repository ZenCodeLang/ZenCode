package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.generic.TypeParameterBound;
import org.openzen.zenscript.codemodel.statement.*;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;

public class FormattingUtils {
	private FormattingUtils() {
	}

	public static void formatModifiers(StringBuilder output, Modifiers modifiers) {
		if (modifiers.isPrivate())
			output.append("private ");
		if (modifiers.isProtected())
			output.append("protected ");
		if (modifiers.isPublic())
			output.append("public ");
		if (modifiers.isInternal())
			output.append("internal ");
		if (modifiers.isStatic())
			output.append("static ");
		if (modifiers.isAbstract())
			output.append("abstract ");
		if (modifiers.isVirtual())
			output.append("virtual ");
		if (modifiers.isFinal())
			output.append("final ");
		if (modifiers.isExtern())
			output.append("extern ");
		if (modifiers.isImplicit())
			output.append("implicit ");
		if (modifiers.isConst())
			output.append("const ");
		if (modifiers.isConstOptional())
			output.append("const? ");
	}

	public static void formatHeader(StringBuilder result, ScriptFormattingSettings settings, FunctionHeader header, TypeFormatter typeFormatter) {
		FormattingUtils.formatTypeParameters(result, header.typeParameters, typeFormatter);
		result.append("(");
		int parameterIndex = 0;
		for (FunctionParameter parameter : header.parameters) {
			if (parameterIndex > 0)
				result.append(", ");

			result.append(parameter.name);
			if (parameter.variadic)
				result.append("...");

			if (!settings.showAnyInFunctionHeaders || parameter.type != BasicTypeID.UNDETERMINED) {
				result.append(" as ");
				result.append(typeFormatter.format(header.getReturnType()));
			}

			parameterIndex++;
		}
		result.append(")");
		if (!settings.showAnyInFunctionHeaders || header.getReturnType() != BasicTypeID.UNDETERMINED) {
			result.append(" as ");
			result.append(typeFormatter.format(header.getReturnType()));
		}
	}

	public static void formatTypeParameters(StringBuilder result, TypeParameter[] parameters, TypeFormatter typeFormatter) {
		if (parameters != null && parameters.length > 0) {
			result.append("<");
			int index = 0;
			for (TypeParameter parameter : parameters) {
				if (index > 0)
					result.append(", ");

				result.append(parameter.name);

				if (parameter.bounds.size() > 0) {
					for (TypeParameterBound bound : parameter.bounds) {
						result.append(": ");
						result.append(bound.accept(typeFormatter));
					}
				}

				index++;
			}
			result.append(">");
		}
	}

	public static void formatBody(StringBuilder output, ScriptFormattingSettings settings, String indent, TypeFormatter typeFormatter, Statement body) {
		body.accept(new BodyFormatter(output, settings, indent, typeFormatter));
		output.append("\n");
	}

	public static void formatCall(StringBuilder result, TypeFormatter typeFormatter, ExpressionFormatter expressionFormatter, CallArguments arguments) {
		if (arguments == null || arguments.typeArguments == null)
			throw new IllegalArgumentException("Arguments cannot be null!");

		if (arguments.typeArguments.length > 0) {
			result.append("<");

			int index = 0;
			for (TypeID typeArgument : arguments.typeArguments) {
				if (index > 0)
					result.append(", ");
				result.append(typeFormatter.format(typeArgument));
				index++;
			}
			result.append(">");
		}
		result.append("(");
		int index = 0;
		for (Expression argument : arguments.arguments) {
			if (index > 0)
				result.append(", ");
			result.append(argument.accept(expressionFormatter).value);
			index++;
		}
		result.append(")");
	}

	private static class BodyFormatter implements StatementVisitor<Void> {
		private final StringBuilder output;
		private final ScriptFormattingSettings settings;
		private final StatementFormatter statementFormatter;
		private final String indent;
		private final TypeFormatter typeFormatter;

		public BodyFormatter(StringBuilder output, ScriptFormattingSettings settings, String indent, TypeFormatter typeFormatter) {
			this.output = output;
			this.settings = settings;
			this.indent = indent;
			this.typeFormatter = typeFormatter;

			statementFormatter = new StatementFormatter(output, indent, settings, new ExpressionFormatter(settings, typeFormatter, indent));
		}

		@Override
		public Void visitBlock(BlockStatement statement) {
			return statementFormatter.visitBlock(statement);
		}

		@Override
		public Void visitBreak(BreakStatement statement) {
			return statementFormatter.visitBreak(statement);
		}

		@Override
		public Void visitContinue(ContinueStatement statement) {
			return statementFormatter.visitContinue(statement);
		}

		@Override
		public Void visitDoWhile(DoWhileStatement statement) {
			return statementFormatter.visitDoWhile(statement);
		}

		@Override
		public Void visitEmpty(EmptyStatement statement) {
			output.append(";");
			return null;
		}

		@Override
		public Void visitExpression(ExpressionStatement statement) {
			if (settings.lambdaMethodOnSameLine) {
				output.append(" => ");
			} else {
				output.append("\n").append(indent).append(settings.indent).append("=> ");
			}

			output.append(statement.expression.accept(new ExpressionFormatter(settings, typeFormatter, indent)));
			output.append(";");
			return null;
		}

		@Override
		public Void visitForeach(ForeachStatement statement) {
			return statementFormatter.visitForeach(statement);
		}

		@Override
		public Void visitIf(IfStatement statement) {
			return statementFormatter.visitIf(statement);
		}

		@Override
		public Void visitLock(LockStatement statement) {
			return statementFormatter.visitLock(statement);
		}

		@Override
		public Void visitReturn(ReturnStatement statement) {
			if (settings.lambdaMethodOnSameLine) {
				output.append(" => ");
			} else {
				output.append("\n").append(indent).append(settings.indent).append("=> ");
			}

			output.append(statement.value.accept(new ExpressionFormatter(settings, typeFormatter, indent)));
			output.append(";");
			return null;
		}

		@Override
		public Void visitSwitch(SwitchStatement statement) {
			return statementFormatter.visitSwitch(statement);
		}

		@Override
		public Void visitThrow(ThrowStatement statement) {
			return statementFormatter.visitThrow(statement);
		}

		@Override
		public Void visitTryCatch(TryCatchStatement statement) {
			return statementFormatter.visitTryCatch(statement);
		}

		@Override
		public Void visitVar(VarStatement statement) {
			return statementFormatter.visitVar(statement);
		}

		@Override
		public Void visitWhile(WhileStatement statement) {
			return statementFormatter.visitWhile(statement);
		}
	}
}
