package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;

public class TypeSerializationContext {
	private final TypeSerializationContext parent;
	public final TypeID thisType;
	private final int parentParameters;
	private final TypeParameter[] typeParameters;

	public TypeSerializationContext(TypeSerializationContext parent, TypeID thisType, TypeParameter[] typeParameters) {
		this.parent = parent;
		this.thisType = thisType;
		this.parentParameters = parent.getTypeParameters();
		this.typeParameters = typeParameters;
	}

	private int getTypeParameters() {
		return parentParameters + typeParameters.length;
	}

	public int getId(TypeParameter typeParameter) {
		for (int i = 0; i < this.typeParameters.length; i++) {
			if (this.typeParameters[i] == typeParameter)
				return parentParameters + i;
		}

		if (parent == null) {
			throw new IllegalArgumentException("Type parameter not available in this scope");
		} else {
			return parent.getId(typeParameter);
		}
	}

	public TypeParameter getTypeParameter(int id) {
		if (id >= parentParameters) {
			return typeParameters[id - parentParameters];
		} else {
			return parent.getTypeParameter(id);
		}
	}

	public StatementSerializationContext forMethod(FunctionHeader header) {
		return new StatementSerializationContext(this, header);
	}
}
