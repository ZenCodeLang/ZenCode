package org.openzen.zenscript.codemodel.member.ref;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.Tag;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.annotations.MemberAnnotation;
import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.FunctionalKind;
import org.openzen.zenscript.codemodel.member.FunctionalMember;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

import java.util.Objects;

public class FunctionalMemberRef implements DefinitionMemberRef {
	private final FunctionalMember target;
	private final TypeID type;
	private FunctionHeader header;
	private GenericMapper mapper;

	public FunctionalMemberRef(FunctionalMember target, TypeID type, GenericMapper mapper) {
		this.target = target;
		this.type = type;

		if (target.header.hasUnknowns) {
			header = null;
			this.mapper = mapper;
		} else {
			header = mapper == null ? target.header : mapper.map(target.header);
			this.mapper = null;
		}
	}

	public boolean accepts(int arguments) {
		return target.header.accepts(arguments);
	}

	@Override
	public FunctionHeader getHeader() {
		if (header == null) {
			if (target.header.hasUnknowns)
				throw new IllegalStateException("member is not yet resolved!");

			header = mapper == null ? target.header : mapper.map(target.header);
			this.mapper = null;
		}

		return header;
	}

	@Override
	public CodePosition getPosition() {
		return target.position;
	}

	@Override
	public TypeID getOwnerType() {
		return type;
	}

	@Override
	public FunctionalMember getTarget() {
		return target;
	}

	public String getCanonicalName() {
		return target.getCanonicalName();
	}

	@Override
	public String describe() {
		return target.describe();
	}

	@Override
	public <T extends Tag> T getTag(Class<T> cls) {
		return target.getTag(cls);
	}

	@Override
	public MemberAnnotation[] getAnnotations() {
		return target.annotations;
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return target.getOverrides();
	}

	public BuiltinID getBuiltin() {
		return target.builtin;
	}

	public boolean isStatic() {
		return target.isStatic();
	}

	public boolean isConstructor() {
		return target.getKind() == FunctionalKind.CONSTRUCTOR;
	}

	public boolean isOperator() {
		return target.getKind() == FunctionalKind.OPERATOR;
	}

	// TODO: shouldn't this be a call operator?
	public boolean isCaller() {
		return target.getKind() == FunctionalKind.CALLER;
	}

	public OperatorType getOperator() {
		return ((OperatorMember) target).operator;
	}

	public String getMethodName() {
		return ((MethodMember) target).name;
	}

	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallExpression(position, target, this, instancedHeader, arguments);
	}

	public final Expression call(CodePosition position, Expression target, CallArguments arguments, TypeScope scope) {
		return call(position, target, header, arguments, scope);
	}

	public Expression callWithComparator(CodePosition position, CompareType comparison, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CompareExpression(position, target, arguments.arguments[0], this, comparison);
	}

	public Expression callStatic(CodePosition position, TypeID target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		return new CallStaticExpression(position, target, this, instancedHeader, arguments);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		FunctionalMemberRef that = (FunctionalMemberRef) o;

		if (!target.equals(that.target))
			return false;
		if (!header.equals(that.header))
			return false;
		if (!type.equals(that.type))
			return false;
		return Objects.equals(mapper, that.mapper);
	}

	@Override
	public int hashCode() {
		int result = target.hashCode();
		result = 31 * result + header.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + (mapper != null ? mapper.hashCode() : 0);
		return result;
	}
}
