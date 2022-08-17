/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.expression.switchvalue.*;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.codemodel.serialization.TypeSerializationContext;
import org.openzen.zenscript.moduleserialization.SwitchValueEncoding;

/**
 * @author Hoofdgebruiker
 */
public class SwitchValueSerializer implements SwitchValueVisitorWithContext<TypeSerializationContext, Void> {
	private final CodeSerializationOutput output;
	private final boolean localVariableNames;

	public SwitchValueSerializer(CodeSerializationOutput output, boolean localVariableNames) {
		this.output = output;
		this.localVariableNames = localVariableNames;
	}

	@Override
	public Void acceptInt(TypeSerializationContext context, IntSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_INT);
		output.writeInt(value.value);
		return null;
	}

	@Override
	public Void acceptChar(TypeSerializationContext context, CharSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_CHAR);
		output.writeUInt(value.value);
		return null;
	}

	@Override
	public Void acceptString(TypeSerializationContext context, StringSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_STRING);
		output.writeString(value.value);
		return null;
	}

	@Override
	public Void acceptEnumConstant(TypeSerializationContext context, EnumConstantSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_ENUM);
		output.write(value.constant);
		return null;
	}

	@Override
	public Void acceptVariantOption(TypeSerializationContext context, VariantOptionSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_VARIANT_OPTION);
		output.write(value.option);
		if (localVariableNames) {
			for (String parameter : value.parameters)
				output.writeString(parameter);
		}
		return null;
	}

	@Override
	public Void acceptError(TypeSerializationContext context, ErrorSwitchValue value) {
		throw new UnsupportedOperationException("Cannot serialize errors");
	}
}
