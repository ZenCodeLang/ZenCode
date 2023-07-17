/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.expression.CallExpression;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.Expression;

/**
 * @author Hoofdgebruiker
 */
public interface JavaNativeTranslator<T> {
	T isEmptyAsLengthZero(Expression value);

	T listToArray(CastExpression value);

	T setToArray(CastExpression value);

	T containsAsIndexOf(Expression target, Expression value);

	T sorted(Expression value);

	T sortedWithComparator(Expression value, Expression comparator);

	T arrayCopy(Expression value);

	T arrayCopyResize(CallExpression value);

	T arrayCopyTo(CallExpression call);

	T stringToAscii(Expression value);

	T stringToUTF8(Expression value);

	T bytesAsciiToString(Expression value);

	T bytesUTF8ToString(Expression value);
}
