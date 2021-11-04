package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

import java.util.Collections;

public class ParsedExpressionFloat extends ParsedExpression {
	public final double value;
	public final String suffix;

	public ParsedExpressionFloat(CodePosition position, String value) {
		super(position);

		int split = value.length();
		while (isLetter(value.charAt(split - 1)))
			split--;

		this.value = Double.parseDouble(value.substring(0, split));
		suffix = value.substring(split);
	}

	private ParsedExpressionFloat(CodePosition position, double value) {
		super(position);

		this.value = value;
		this.suffix = "";
	}

	private static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		// Inbuilt suffix -> Float/Double
		if (suffix.equals("f") || suffix.equals("F"))
			return new ConstantFloatExpression(position, (float) value);
		if (suffix.equals("d") || suffix.equals("D"))
			return new ConstantDoubleExpression(position, value);


		// Check if the typeHints can give us additional information?
		for (TypeID hint : scope.hints) {
			if (suffix.isEmpty()) {
				// No suffix but expression to be known as Float or Double -> That type
				if (hint == BasicTypeID.DOUBLE)
					return new ConstantDoubleExpression(position, value);
				else if (hint == BasicTypeID.FLOAT)
					return new ConstantFloatExpression(position, (float) value);
			} else {
				// Suffix and TypeHint given
				// Check <TypeHint>.<Suffix>(<value>)
				// E.g. 10.0s as TimeSpan -> TimeSpan.s(10.0)
				TypeMembers members = scope.getTypeMembers(hint);
				FunctionalMemberRef method = members.getOrCreateGroup(suffix, true).getStaticMethod(1, hint);
				if (method != null) {
					try {
						ParsedCallArguments parsedArguments = new ParsedCallArguments(Collections.emptyList(), Collections.singletonList(new ParsedExpressionFloat(position, value)));
						CallArguments arguments = parsedArguments.compileCall(position, scope, TypeID.NONE, method.getHeader());
						return method.callStatic(position, hint, method.getHeader(), arguments, scope);
					} catch (CompileException ex) {
						return new InvalidExpression(hint, ex);
					}
				}
			}
		}

		if (suffix.isEmpty()) {
			// No suffix and no TypeHint matched -> Double, so that implicit casters will be checked
			return new ConstantDoubleExpression(position, value);
		} else {
			// Suffix but no TypeHint matched -> Error
			throw new CompileException(position, CompileExceptionCode.INVALID_SUFFIX, "Invalid suffix: " + suffix);
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
