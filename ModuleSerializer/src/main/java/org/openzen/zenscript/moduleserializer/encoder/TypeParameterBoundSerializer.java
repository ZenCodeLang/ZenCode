/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.moduleserializer.encoder;

import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.generic.GenericParameterBoundVisitorWithContext;
import org.openzen.zenscript.codemodel.generic.ParameterSuperBound;
import org.openzen.zenscript.codemodel.generic.ParameterTypeBound;
import org.openzen.zenscript.codemodel.serialization.CodeSerializationOutput;
import org.openzen.zenscript.moduleserialization.TypeParameterEncoding;

/**
 *
 * @author Hoofdgebruiker
 */
public class TypeParameterBoundSerializer implements GenericParameterBoundVisitorWithContext<TypeContext, Void> {
	private final CodeSerializationOutput output;
	
	public TypeParameterBoundSerializer(CodeSerializationOutput output) {
		this.output = output;
	}

	@Override
	public Void visitSuper(TypeContext context, ParameterSuperBound bound) {
		output.writeUInt(TypeParameterEncoding.TYPE_SUPER_BOUND);
		output.serialize(context, bound.type);
		return null;
	}

	@Override
	public Void visitType(TypeContext context, ParameterTypeBound bound) {
		output.writeUInt(TypeParameterEncoding.TYPE_TYPE_BOUND);
		output.serialize(context, bound.type);
		return null;
	}
}
