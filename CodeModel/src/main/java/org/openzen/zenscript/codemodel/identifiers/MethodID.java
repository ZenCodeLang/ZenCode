package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.Optional;

public final class MethodID {
	public enum Kind {
		INSTANCEMETHOD(false),
		STATICMETHOD(true),
		OPERATOR(false),
		STATICOPERATOR(true),
		GETTER(false),
		SETTER(false),
		STATICGETTER(true),
		STATICSETTER(true),
		CASTER(false),
		ITERATOR(false);

		private final boolean static_;

		Kind(boolean static_) {
			this.static_ = static_;
		}
	}

	public static MethodID instanceMethod(String name) {
		return new MethodID(Kind.INSTANCEMETHOD, name);
	}

	public static MethodID staticMethod(String name) {
		return new MethodID(Kind.STATICMETHOD, name);
	}

	public static MethodID operator(OperatorType operator) {
		return new MethodID(Kind.OPERATOR, operator);
	}

	public static MethodID staticOperator(OperatorType operator) {
		return new MethodID(Kind.STATICOPERATOR, operator);
	}

	public static MethodID getter(String name) {
		return new MethodID(Kind.GETTER, name);
	}

	public static MethodID setter(String name) {
		return new MethodID(Kind.SETTER, name);
	}

	public static MethodID staticGetter(String name) {
		return new MethodID(Kind.STATICGETTER, name);
	}

	public static MethodID staticSetter(String name) {
		return new MethodID(Kind.STATICSETTER, name);
	}

	public static MethodID caster(TypeID caster) {
		return new MethodID(Kind.CASTER, caster);
	}

	public static MethodID iterator(int variables) {
		return new MethodID(Kind.OPERATOR, variables);
	}

	// I wish we had unions ^^
	private final Kind kind;
	private final String name;
	private final OperatorType operator;
	private final TypeID type;
	private final int variables;

	private MethodID(Kind kind, String name) {
		this.kind = kind;
		this.name = name;
		this.operator = null;
		this.type = null;
		this.variables = 0;
	}

	private MethodID(Kind kind, OperatorType operator) {
		this.kind = kind;
		this.name = null;
		this.operator = operator;
		this.type = null;
		this.variables = 0;
	}

	private MethodID(Kind kind, TypeID type) {
		this.kind = kind;
		this.name = null;
		this.operator = null;
		this.type = type;
		this.variables = 0;
	}

	private MethodID(Kind kind, int variables) {
		this.kind = kind;
		this.name = null;
		this.operator = null;
		this.type = null;
		this.variables = variables;
	}

	public Kind getKind() {
		return kind;
	}

	public String toString() {
		if (name != null)
			return name;
		if (operator != null)
			return operator.operator;
		if (type != null)
			return type.toString();

		return "iterator" + variables;
	}

	public <T> T accept(Visitor<T> visitor) {
		switch (kind) {
			case INSTANCEMETHOD: return visitor.visitInstanceMethod(name);
			case STATICMETHOD: return visitor.visitStaticMethod(name);
			case OPERATOR: return visitor.visitOperator(operator);
			case STATICOPERATOR: return visitor.visitStaticOperator(operator);
			case GETTER: return visitor.visitGetter(name);
			case SETTER: return visitor.visitSetter(name);
			case STATICGETTER: return visitor.visitStaticGetter(name);
			case STATICSETTER: return visitor.visitStaticSetter(name);
			case CASTER: return visitor.visitCaster(type);
			case ITERATOR: return visitor.visitIterator(variables);
			default: throw new IllegalStateException();
		}
	}

	public boolean isStatic() {
		return kind.static_;
	}

	public Optional<OperatorType> getOperator() {
		return Optional.ofNullable(operator);
	}

	public interface Visitor<T> {
		T visitInstanceMethod(String name);

		T visitStaticMethod(String name);

		T visitOperator(OperatorType operator);

		T visitStaticOperator(OperatorType operator);

		T visitGetter(String name);

		T visitSetter(String name);

		T visitStaticGetter(String name);

		T visitStaticSetter(String name);

		T visitCaster(TypeID type);

		T visitIterator(int variables);
	}
}
