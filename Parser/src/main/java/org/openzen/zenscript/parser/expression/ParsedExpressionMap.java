/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.MapExpression;
import org.openzen.zenscript.codemodel.expression.NewExpression;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.AssocTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.GenericMapTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionMap extends ParsedExpression {
	private final List<ParsedExpression> keys;
	private final List<ParsedExpression> values;

	public ParsedExpressionMap(
			CodePosition position,
			List<ParsedExpression> keys,
			List<ParsedExpression> values) {
		super(position);

		this.keys = keys;
		this.values = values;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		List<ITypeID> keyHints = new ArrayList<>();
		List<ITypeID> valueHints = new ArrayList<>();
		
		boolean hasAssocHint = false;
		for (ITypeID hint : scope.hints) {
			if (hint instanceof AssocTypeID) {
				AssocTypeID assocHint = (AssocTypeID) hint;
				if (!keyHints.contains(assocHint.keyType))
					keyHints.add(assocHint.keyType);
				if (!valueHints.contains(assocHint.valueType))
					valueHints.add(assocHint.valueType);
				
				hasAssocHint = true;
			} else if (hint instanceof GenericMapTypeID) {
				ICallableMember constructor = scope
						.getTypeMembers(hint)
						.getOrCreateGroup(OperatorType.CONSTRUCTOR)
						.selectMethod(position, scope, CallArguments.EMPTY, true, true);
				return new NewExpression(position, hint, (ConstructorMember) constructor, CallArguments.EMPTY);
			}
		}
		
		if (!hasAssocHint && scope.hints.size() == 1 && scope.hints.get(0) != BasicTypeID.ANY) {
			// compile as constructor call
			ITypeID hint = scope.hints.get(0);
			for (int i = 0; i < keys.size(); i++) {
				if (keys.get(i) != null)
					throw new CompileException(position, CompileExceptionCode.UNSUPPORTED_NAMED_ARGUMENTS, "Named constructor arguments not yet supported");
			}
			ParsedCallArguments arguments = new ParsedCallArguments(values);
			return ParsedNewExpression.compile(position, hint, arguments, scope);
		}
		
		Expression[] cKeys = new Expression[keys.size()];
		Expression[] cValues = new Expression[values.size()];

		for (int i = 0; i < keys.size(); i++) {
			if (keys.get(i) == null)
				throw new CompileException(position, CompileExceptionCode.MISSING_MAP_KEY, "Missing key");
			
			cKeys[i] = keys.get(i).compileKey(scope.withHints(keyHints));
			cValues[i] = values.get(i).compile(scope.withHints(valueHints)).eval();
		}
		
		ITypeID keyType = null;
		for (Expression key : cKeys) {
			if (key.type == keyType)
				continue;
			
			if (keyType == null) {
				keyType = key.type;
			} else {
				keyType = scope.getTypeMembers(keyType).union(key.type);
			}
		}
		if (keyType == null)
			keyType = BasicTypeID.ANY;
		for (int i = 0; i < cKeys.length; i++)
			cKeys[i] = cKeys[i].castImplicit(position, scope, keyType);
		
		ITypeID valueType = null;
		for (Expression value : cValues) {
			if (value.type == valueType)
				continue;
			
			if (valueType == null) {
				valueType = value.type;
			} else {
				valueType = scope.getTypeMembers(valueType).union(value.type);
			}
		}
		if (valueType == null)
			valueType = BasicTypeID.ANY;
		for (int i = 0; i < cValues.length; i++)
			cValues[i] = cValues[i].castImplicit(position, scope, valueType);
		
		AssocTypeID asType = scope.getTypeRegistry().getAssociative(keyType, valueType);
		return new MapExpression(position, cKeys, cValues, asType);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}
