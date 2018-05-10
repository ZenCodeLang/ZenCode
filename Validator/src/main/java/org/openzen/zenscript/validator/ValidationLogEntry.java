/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.validator;

import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ValidationLogEntry {
	public final Kind kind;
	public final Code code;
	public final CodePosition position;
	public final String message;
	
	public ValidationLogEntry(Kind kind, Code code, CodePosition position, String message) {
		this.kind = kind;
		this.code = code;
		this.position = position;
		this.message = message;
	}
	
	public static enum Kind {
		ERROR,
		WARNING
	}
	
	public static enum Code {
		SUPERCLASS_NOT_A_CLASS,
		SUPERCLASS_NOT_VIRTUAL,
		INVALID_MODIFIER,
		INVALID_IDENTIFIER,
		DUPLICATE_FIELD_NAME,
		DUPLICATE_MEMBER_NAME,
		INVALID_TYPE,
		DUPLICATE_PARAMETER_NAME,
		INVALID_OPERAND_TYPE,
		INVALID_TYPE_ARGUMENT,
		INVALID_CALL_ARGUMENT,
		VARIADIC_PARAMETER_MUST_BE_LAST,
		CONSTRUCTOR_FORWARD_OUTSIDE_CONSTRUCTOR,
		CONSTRUCTOR_FORWARD_NOT_FIRST_STATEMENT,
		CONSTRUCTOR_FORWARD_MISSING,
		DUPLICATE_CONSTRUCTOR,
		DUPLICATE_METHOD,
		BODY_REQUIRED,
		INVALID_CONDITION_TYPE,
		DUPLICATE_VARIABLE_NAME,
		SCRIPT_CANNOT_RETURN,
		INVALID_RETURN_TYPE,
		TRY_CATCH_RESOURCE_REQUIRES_INITIALIZER,
		TYPE_ALREADY_IMPLEMENTED,
		THIS_IN_STATIC_SCOPE,
		ENUM_CONSTANT_NOT_YET_INITIALIZED,
		FIELD_NOT_YET_INITIALIZED,
		LOCAL_VARIABLE_NOT_YET_INITIALIZED,
		INVALID_SOURCE_TYPE,
		SETTING_FINAL_FIELD,
		SETTING_FINAL_VARIABLE
	}
}
