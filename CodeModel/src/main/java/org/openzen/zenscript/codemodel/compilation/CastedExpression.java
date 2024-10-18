package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileError;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;

public class CastedExpression {
	public enum Level {
		EXACT,
		WIDENING,
		IMPLICIT,
		EXPLICIT,
		INVALID;

		public Level min(Level other) {
			return ordinal() < other.ordinal() ? this : other;
		}

		public Level max(Level other) {
			return ordinal() < other.ordinal() ? other : this;
		}
	}

	public static CastedExpression exact(Expression value) {
		return new CastedExpression(Level.EXACT, value);
	}

	public static CastedExpression implicit(Expression value) {
		return new CastedExpression(Level.IMPLICIT, value);
	}

	public static CastedExpression explicit(Expression value) {
		return new CastedExpression(Level.EXPLICIT, value);
	}

	public static CastedExpression invalid(CodePosition position, CompileError error) {
		return new CastedExpression(position, error);
	}

	public static CastedExpression invalid(Expression expression, CompileError error) {
		return new CastedExpression(Level.INVALID, expression, error);
	}

	public static CastedExpression invalidType(Expression expression) {
		return new CastedExpression(Level.INVALID, expression, expression.type.asInvalid().error);
	}

	public final Level level;
	public final Expression value;
	public final CompileError error;

	public CastedExpression(Level level, Expression value) {
		this.level = level;
		this.value = value;
		this.error = level == Level.INVALID ? value.asInvalid().map(i -> i.error).orElse(null) : null;
	}

	public CastedExpression(CodePosition position, CompileError error) {
		this.level = Level.INVALID;
		this.value = new InvalidExpression(position, BasicTypeID.INVALID, error);
		this.error = error;
	}

	private CastedExpression(Level level, Expression value, CompileError error) {
		this.level = level;
		this.value = value;
		this.error = error;
	}

	public boolean isFailed() {
		return level == Level.INVALID;
	}
}
