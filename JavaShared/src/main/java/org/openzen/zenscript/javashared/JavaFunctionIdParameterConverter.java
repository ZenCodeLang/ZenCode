package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.HashMap;
import java.util.Map;

public class JavaFunctionIdParameterConverter {
	private int typeParameterIndex = 0;

	//Check if we already know the type, so that function(String,String) becomes function(TT) instead of TU
	private final Map<TypeID, String> alreadyKnownParameters = new HashMap<>();

	public String convertTypeToId(TypeID parameterType) {
		if(alreadyKnownParameters.containsKey(parameterType)) {
			return alreadyKnownParameters.get(parameterType);
		}

		JavaTypeInfo typeInfo = JavaTypeInfo.get(parameterType);
		String id = typeInfo.primitive ? parameterType.accept(new JavaSyntheticTypeSignatureConverter()) : JavaContext.getTypeParameter(typeParameterIndex++);
		alreadyKnownParameters.put(parameterType, id);
		return id;
	}
}
