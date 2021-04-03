/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.java;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Hoofdgebruiker
 */
public interface ZenCodeType {
	enum OperatorType {
		ADD,        // binary + (addition)
		SUB,        // binary - (subtraction)
		MUL,            // binary * (multiplication)
		DIV,        // binary / (division)
		MOD,        // binary % (modulo)
		CAT,        // binary ~ (concatenation)
		OR,            // binary | (or)
		AND,        // binary & (and)
		XOR,        // binary ^ (xor)
		NEG,        // unary - (negation)
		INVERT,        // unary ~ (bitwise not)
		NOT,        // unary ! (logical not)
		INDEXSET,    // indexed set (array[x, y] = z will be translated to array.INDEXSET(x, y, z)
		INDEXGET,    // indexed get (array[x, y] will be translated to array.INDEXGET(x, y)
		CONTAINS,    // contains (x in y will be translated to y.CONTAINS(x)
		COMPARE,    // comparison (x < y will be translated to x.COMPARE(y) < 0)
		MEMBERGETTER, // member getter (x.name will be translated to x.MEMBERGETTER("name") if there is no member name in x)
		MEMBERSETTER, // member setter (x.name = y will be translated to x.MEMBERSETTER("name", y) if there is no member name in x)
		EQUALS,        // equality operator ==
		NOTEQUALS,    // inequality operator != (note that by default, the inverse of equals will be used)
		SHL,            // binary << (shift left)
		SHR,        // binary >> (shift right)

		ADDASSIGN,    // += (by default, x += y will be translated to x = x + y unless this operator is implemented)
		SUBASSIGN,    // -= (by default, x -= y will be translated to x = x - y unless this operator is implemented)
		MULASSIGN,    // *= (by default, x *= y will be translated to x = x * y unless this operator is implemented)
		DIVASSIGN,    // /= (by default, x /= y will be translated to x = x / y unless this operator is implemented)
		MODASSIGN,    // %= (by default, x %= y will be translated to x = x % y unless this operator is implemented)
		CATASSIGN,    // ~= (by default, x ~= y will be translated to x = x ~ y unless this operator is implemented)
		ORASSIGN,    // |= (by default, x |= y will be translated to x = x | y unless this operator is implemented)
		ANDASSIGN,    // &= (by default, x &= y will be translated to x = x & y unless this operator is implemented)
		XORASSIGN,    // ^= (by default, x ^= y will be translated to x = x ^ y unless this operator is implemented)
		SHLASSIGN,    // >>= (by default, x >>= y will be translated to x = x >> y unless this operator is implemented)
		SHRASSIGN    // <<= (by default, x <<= y will be translated to x = x << y unless this operator is implemented)
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Expansion {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Name {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@interface Struct {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	@interface Field {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.CONSTRUCTOR)
	@interface Constructor {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Method {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface StaticExpansionMethod {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Operator {
		OperatorType value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Getter {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Setter {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@interface Caster {
		boolean implicit() default false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Nullable {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface Unsigned {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface USize {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface NullableUSize {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface BorrowForCall {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE_USE)
	@interface BorrowForThis {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface Optional {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalInt {
		int value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalLong {
		long value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalFloat {
		float value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalDouble {
		double value() default 0;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalString {
		String value() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalBoolean {
		boolean value() default false;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface OptionalChar {
		char value() default ' ';
	}
}
