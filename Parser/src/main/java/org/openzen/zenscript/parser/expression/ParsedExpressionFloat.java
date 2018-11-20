/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.Collections;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.ConstantDoubleExpression;
import org.openzen.zenscript.codemodel.expression.ConstantFloatExpression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
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

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		if (scope.hints.isEmpty())
			return new ConstantDoubleExpression(position, value);
		
		if (suffix.equals("f") || suffix.equals("F"))
			return new ConstantFloatExpression(position, (float)value);
		if (suffix.equals("d") || suffix.equals("D"))
			return new ConstantDoubleExpression(position, value);
		
		for (StoredType hint : scope.hints) {
			if (suffix.isEmpty()) {
				if (hint.isBasic(BasicTypeID.DOUBLE))
					return new ConstantDoubleExpression(position, value);
				else if (hint.isBasic(BasicTypeID.FLOAT))
					return new ConstantFloatExpression(position, (float) value);
			} else {
				TypeMembers members = scope.getTypeMembers(hint);
				FunctionalMemberRef method = members.getOrCreateGroup(suffix, true).getStaticMethod(1, hint);
				if (method != null) {
					try {
						ParsedCallArguments parsedArguments = new ParsedCallArguments(Collections.emptyList(), Collections.singletonList(new ParsedExpressionFloat(position, value)));
						CallArguments arguments = parsedArguments.compileCall(position, scope, StoredType.NONE, method.getHeader());
						method.callStatic(position, hint.type, method.getHeader(), arguments, scope);
					} catch (CompileException ex) {
						return new InvalidExpression(hint, ex);
					}
				}
			}
		}
		
		if (suffix.isEmpty()) {
			StringBuilder types = new StringBuilder();
			for (int i = 0; i < scope.hints.size(); i++) {
				if (i > 0)
					types.append(", ");

				types.append(scope.hints.get(i).toString());
			}

			throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Cannot cast a floating-point value to any of these types: " + types);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SUFFIX, "Invalid suffix: " + suffix);
		}
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
	
	private static boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
	}
}
