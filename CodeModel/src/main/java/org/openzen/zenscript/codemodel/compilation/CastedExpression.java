package org.openzen.zenscript.codemodel.compilation;

import org.openzen.zenscript.codemodel.expression.Expression;

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

	public static CastedExpression invalid(Expression value) {
		return new CastedExpression(Level.INVALID, value);
	}

	public final Level level;
	public final Expression value;

	public CastedExpression(Level level, Expression value) {
		this.level = level;
		this.value = value;
	}

	public boolean isFailed() {
		return level == Level.INVALID;
	}
}
