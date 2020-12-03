package org.openzen.zenscript.codemodel.expression;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.statement.StatementTransformer;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;

public abstract class Expression implements IPartialExpression {
	public static final Expression[] NONE = new Expression[0];
	
	public final CodePosition position;
	public final TypeID type;
	public final TypeID thrownType;
	
	public Expression(CodePosition position, TypeID type, TypeID thrownType) {
		if (type == null)
			throw new NullPointerException();
		//if (type.type == BasicTypeID.UNDETERMINED)
		//	throw new IllegalArgumentException(position + ": Cannot use undetermined type as expression type");
		
		this.position = position;
		this.type = type.getNormalized();
		this.thrownType = thrownType;
	}
	
	public abstract <T> T accept(ExpressionVisitor<T> visitor);
	
	public abstract <C, R> R accept(C context, ExpressionVisitorWithContext<C, R> visitor);
	
	public abstract Expression transform(ExpressionTransformer transformer);
	
	public final Expression transform(StatementTransformer transformer) {
		return transform((ExpressionTransformer)expression -> {
			if (expression instanceof FunctionExpression) {
				FunctionExpression function = (FunctionExpression)expression;
				Statement body = function.body.transform(transformer);
				if (body == function.body)
					return function;
				
				return new FunctionExpression(function.position, function.type, function.closure, function.header, body);
			} else {
				return expression;
			}
		});
	}
	
	public abstract Expression normalize(TypeScope scope);
	
	@Override
	public List<TypeID> getAssignHints() {
		return Collections.singletonList(type);
	}
	
	@Override
	public Expression eval() {
		return this;
	}
	
	public Expression castExplicit(CodePosition position, TypeScope scope, TypeID asType, boolean optional) {
		return scope.getTypeMembers(type).castExplicit(position, this, asType, optional);
	}
	
	public Expression castImplicit(CodePosition position, TypeScope scope, TypeID asType) {
		return scope.getTypeMembers(type).castImplicit(position, this, asType, true);
	}
	
	/**
	 * Determines if this expression aborts execution; that is, it is either a
	 * throw or a panic expression.
	 * 
	 * @return abort flag
	 */
	public boolean aborts() {
		return false;
	}
	
	@Override
	public List<TypeID>[] predictCallTypes(CodePosition position, TypeScope scope, List<TypeID> hints, int arguments) {
		TypeMemberGroup group = scope.getTypeMembers(type).getGroup(OperatorType.CALL);
		return group.predictCallTypes(position, scope, hints, arguments);
	}
	
	@Override
	public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<TypeID> hints, int arguments) {
		TypeMemberGroup group = scope.getTypeMembers(type).getGroup(OperatorType.CALL);
		return group.getMethodMembers().stream()
				.filter(method -> method.member.getHeader().accepts(arguments) && !method.member.isStatic())
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
	}
	
	@Override
	public Expression call(CodePosition position, TypeScope scope, List<TypeID> hints, CallArguments arguments) throws CompileException {
		TypeMemberGroup group = scope.getTypeMembers(type).getGroup(OperatorType.CALL);
		return group.call(position, scope, this, arguments, false);
	}
	
	@Override
	public IPartialExpression getMember(CodePosition position, TypeScope scope, List<TypeID> hints, GenericName name) throws CompileException {
		TypeMembers members = scope.getTypeMembers(type);
		IPartialExpression result = members.getMemberExpression(position, scope, this, name, false);
		if (result == null)
		    throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "No such member: " + name.name);
		return result;
	}
	
	@Override
	public TypeID[] getTypeArguments() {
		return null;
	}
	
	public void forEachStatement(Consumer<Statement> consumer) {
		
	}
	
	public String evaluateStringConstant() {
		throw new UnsupportedOperationException("Cannot evaluate this value to a string constant!");
	}
	
	public EnumConstantMember evaluateEnumConstant() {
		throw new UnsupportedOperationException("Cannot evaluate this value to an enum constant!");
	}
	
	public static TypeID binaryThrow(CodePosition position, TypeID left, TypeID right) {
		if (left == right)
			return left;
		else if (left == null)
			return right;
		else if (right == null)
			return left;
		else
			return new InvalidTypeID(position, CompileExceptionCode.DIFFERENT_EXCEPTIONS, "two different exceptions in same operation: " + left.toString() + " and " + right.toString());
	}
	
	public static TypeID multiThrow(CodePosition position, Expression[] expressions) {
		TypeID result = null;
		for (Expression expression : expressions)
			result = binaryThrow(position, result, expression.thrownType);
		return result;
	}
	
	public static Expression[] transform(Expression[] expressions, ExpressionTransformer transformer) {
		Expression[] tExpressions = new Expression[expressions.length];
		boolean changed = false;
		for (int i = 0; i < tExpressions.length; i++) {
			Expression tExpression = expressions[i].transform(transformer);
			changed |= tExpression != expressions[i];
			tExpressions[i] = tExpression;
		}
		return changed ? tExpressions : expressions;
	}
}
