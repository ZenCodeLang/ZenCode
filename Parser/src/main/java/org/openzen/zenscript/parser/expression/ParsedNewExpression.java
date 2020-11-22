package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.parser.type.IParsedType;

public class ParsedNewExpression extends ParsedExpression{
	private final IParsedType type;
	private final ParsedCallArguments arguments;
	
	public ParsedNewExpression(CodePosition position, IParsedType type, ParsedCallArguments arguments) {
		super(position);
		
		this.type = type;
		this.arguments = arguments;
	}
	
	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		TypeID type = this.type.compile(scope);
		return compile(position, type, arguments, scope);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
	
	public static Expression compile(CodePosition position, TypeID type, ParsedCallArguments arguments, ExpressionScope scope) {
		try {
			TypeMembers members = scope.getTypeMembers(type);
			TypeMemberGroup constructors = members.getOrCreateGroup(OperatorType.CONSTRUCTOR);

			List<TypeID>[] predictedTypes = constructors.predictCallTypes(position, scope, scope.hints, arguments.arguments.size());
			CallArguments compiledArguments = arguments.compileCall(position, scope, null, constructors);
			FunctionalMemberRef member = constructors.selectMethod(position, scope, compiledArguments, true, true);
			if (member == null)
				return new InvalidExpression(position, type, CompileExceptionCode.CALL_NO_VALID_METHOD, "No matching constructor found");
			if (!member.isConstructor())
				return new InvalidExpression(position, type, CompileExceptionCode.INTERNAL_ERROR, "COMPILER BUG: constructor is not a constructor");

			return new NewExpression(
					position,
					type,
					member,
					compiledArguments,
					member.getHeader().fillGenericArguments(position, scope, compiledArguments.typeArguments));
		} catch (CompileException ex) {
			return new InvalidExpression(type, ex);
		}
	}
}
