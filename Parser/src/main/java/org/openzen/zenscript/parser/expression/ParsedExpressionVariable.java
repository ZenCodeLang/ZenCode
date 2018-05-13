/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.expression.ConstantStringExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.VariantValueExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.definitions.ParsedFunctionHeader;
import org.openzen.zenscript.parser.definitions.ParsedFunctionParameter;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.parser.type.ParsedTypeBasic;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionVariable extends ParsedExpression {
	public final String name;
	private final List<IParsedType> genericParameters;
	
	public ParsedExpressionVariable(CodePosition position, String name, List<IParsedType> genericParameters) {
		super(position);

		this.name = name;
		this.genericParameters = genericParameters;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID[] genericArguments = null;
		if (genericParameters != null) {
			genericArguments = new ITypeID[genericParameters.size()];
			for (int i = 0; i < genericParameters.size(); i++) {
				genericArguments[i] = genericParameters.get(i).compile(scope);
			}
		}
		
		IPartialExpression result = scope.get(position, new GenericName(name, genericArguments));
		if (result == null) {
			for (ITypeID hint : scope.hints) {
				TypeMembers members = scope.getTypeMembers(hint);
				EnumConstantMember member = members.getEnumMember(name);
				if (member != null)
					return new EnumConstantExpression(position, hint, member);
				
				VariantDefinition.Option option = members.getVariantOption(name);
				if (option != null)
					return new VariantValueExpression(position, hint, option);
			}
			
			throw new CompileException(position, CompileExceptionCode.UNDEFINED_VARIABLE, "No such symbol: " + name);
		} else {
			return result;
		}
	}

	@Override
	public Expression compileKey(ExpressionScope scope) {
		return new ConstantStringExpression(position, name);
	}
	
	@Override
	public SwitchValue compileToSwitchValue(ITypeID type, ExpressionScope scope) {
		TypeMembers members = scope.getTypeMembers(type);
		if (type.isEnum()) {
			EnumConstantMember member = members.getEnumMember(name);
			if (member == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Enum member does not exist: " + name);
				
			return new EnumConstantSwitchValue(member);
		} else if (type.isVariant()) {
			VariantDefinition.Option option = members.getVariantOption(name);
			if (option == null)
				throw new CompileException(position, CompileExceptionCode.NO_SUCH_MEMBER, "Variant option does not exist: " + name);
			if (option.types.length > 0)
				throw new CompileException(position, CompileExceptionCode.MISSING_VARIANT_CASEPARAMETERS, "Variant case is missing parameters");
			
			return new VariantOptionSwitchValue(option, new String[0]);
		} else {
			throw new CompileException(position, CompileExceptionCode.INVALID_SWITCH_CASE, "Invalid switch case");
		}
	}
	
	@Override
	public ParsedFunctionHeader toLambdaHeader() {
		return new ParsedFunctionHeader(Collections.singletonList(toLambdaParameter()), ParsedTypeBasic.ANY);
	}
	
	@Override
	public ParsedFunctionParameter toLambdaParameter() {
		return new ParsedFunctionParameter(name, ParsedTypeBasic.ANY, null, false);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}
