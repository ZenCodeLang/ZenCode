package org.openzen.zenscript.codemodel.identifiers;

import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.type.TypeID;

public abstract class MethodID {

	public enum Kind {
		METHOD,
		OPERATOR,
		GETTER,
		SETTER,
		CASTER,
		ITERATOR
	}

	public static MethodID.Method method(String name) {
		return new MethodID.Method(name);
	}

	public static MethodID.Operator operator(OperatorType operator) {
		return new MethodID.Operator(operator);
	}

	public static MethodID.Getter getter(String name) {
		return new MethodID.Getter(name);
	}

	public static MethodID.Setter setter(String name) {
		return new MethodID.Setter(name);
	}

	public static MethodID.Caster caster(TypeID caster) {
		return new MethodID.Caster(caster);
	}

	public static MethodID.Iterator iterator(int variables) {
		return new MethodID.Iterator(variables);
	}

	private MethodID() {}

	public abstract Kind getKind();

	public abstract String toString();

	public abstract <T> T accept(Visitor<T> visitor);

	public static class Method extends MethodID {
		public final String name;

		public Method(String name) {
			this.name = name;
		}

		@Override
		public Kind getKind() {
			return Kind.METHOD;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitMethod(this);
		}
	}

	public static class Operator extends MethodID {
		public final OperatorType operator;

		public Operator(OperatorType operator) {
			this.operator = operator;
		}

		@Override
		public Kind getKind() {
			return Kind.OPERATOR;
		}

		@Override
		public String toString() {
			return operator.operator;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitOperator(this);
		}
	}

	public static class Getter extends MethodID {
		public final String name;

		public Getter(String name) {
			this.name = name;
		}

		@Override
		public Kind getKind() {
			return Kind.GETTER;
		}

		@Override
		public String toString() {
			return "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitGetter(this);
		}
	}

	public static class Setter extends MethodID {
		public final String name;

		public Setter(String name) {
			this.name = name;
		}

		@Override
		public Kind getKind() {
			return Kind.SETTER;
		}

		@Override
		public String toString() {
			return "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitSetter(this);
		}
	}

	public static class Caster extends MethodID {
		public final TypeID toType;

		public Caster(TypeID toType) {
			this.toType = toType;
		}

		@Override
		public Kind getKind() {
			return Kind.CASTER;
		}

		@Override
		public String toString() {
			return "cast<" + toType.toString() + ">";
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitCaster(this);
		}
	}

	public static class Iterator extends MethodID {
		public final int variables;

		public Iterator(int variables) {
			this.variables = variables;
		}

		@Override
		public Kind getKind() {
			return Kind.ITERATOR;
		}

		@Override
		public String toString() {
			return "iterator" + variables;
		}

		@Override
		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitIterator(this);
		}
	}

	public interface Visitor<T> {
		T visitMethod(Method method);

		T visitOperator(Operator operator);

		T visitGetter(Getter getter);

		T visitSetter(Setter setter);

		T visitCaster(Caster caster);

		T visitIterator(Iterator iterator);
	}
}
