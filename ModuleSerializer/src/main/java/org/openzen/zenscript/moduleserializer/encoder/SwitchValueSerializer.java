/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.expression.switchvalue.CharSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.EnumConstantSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.IntSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.StringSwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValueVisitorWithContext;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.moduleserialization.SwitchValueEncoding;

/**
 * @author Hoofdgebruiker
 */
public class SwitchValueSerializer implements SwitchValueVisitorWithContext<TypeContext, Void> {
	private final CodeSerializationOutput output;
	private final boolean localVariableNames;

	public SwitchValueSerializer(CodeSerializationOutput output, boolean localVariableNames) {
		this.output = output;
		this.localVariableNames = localVariableNames;
	}

	@Override
	public Void acceptInt(TypeContext context, IntSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_INT);
		output.writeInt(value.value);
		return null;
	}

	@Override
	public Void acceptChar(TypeContext context, CharSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_CHAR);
		output.writeUInt(value.value);
		return null;
	}

	@Override
	public Void acceptString(TypeContext context, StringSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_STRING);
		output.writeString(value.value);
		return null;
	}

	@Override
	public Void acceptEnumConstant(TypeContext context, EnumConstantSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_ENUM);
		output.write(value.constant);
		return null;
	}

	@Override
	public Void acceptVariantOption(TypeContext context, VariantOptionSwitchValue value) {
		output.writeUInt(SwitchValueEncoding.TYPE_VARIANT_OPTION);
		output.write(value.option);
		if (localVariableNames) {
			for (String parameter : value.parameters)
				output.writeString(parameter);
		}
		return null;
	}
}
