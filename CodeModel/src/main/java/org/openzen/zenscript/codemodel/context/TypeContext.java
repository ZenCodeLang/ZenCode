package org.openzen.zenscript.codemodel.context;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class TypeContext {
	protected final CodePosition position;
	protected final TypeParameter[] typeParameters;
	public final TypeID thisType;
	private final LocalMemberCache memberCache;
	public final ModuleContext moduleContext;
	
	public TypeContext(CodePosition position, ModuleContext context, TypeParameter[] parameters, TypeID thisType) {
		this.position = position;
		this.typeParameters = parameters;
		this.thisType = thisType;
		memberCache = new LocalMemberCache(context.registry, context.expansions);
		moduleContext = context;
	}
	
	public TypeContext(CodePosition position, TypeContext outer, TypeID thisType, TypeParameter... inner) {
		this.position = position;
		typeParameters = concat(outer.typeParameters, inner);
		this.thisType = thisType;
		moduleContext = outer.moduleContext;
		memberCache = new LocalMemberCache(moduleContext.registry, moduleContext.expansions);
	}
	
	public TypeContext(CodePosition position, TypeContext outer, TypeID thisType, List<TypeParameter> inner) {
		this(position, outer, thisType, inner.toArray(TypeParameter.NONE));
	}
	
	public int getId(TypeParameter parameter) {
		for (int i = 0; i < typeParameters.length; i++)
			if (typeParameters[i] == parameter)
				return i;
		
		return -1;
	}
	
	public TypeParameter getTypeParameter(int index) {
		return typeParameters[index];
	}
	
	public TypeMembers getTypeMembers(TypeID type) {
		return memberCache.get(type);
	}
	
	public GenericMapper getMapper() {
		Map<TypeParameter, TypeID> mapper = TypeID.getSelfMapping(moduleContext.registry, typeParameters);
		return new GenericMapper(position, moduleContext.registry, mapper);
	}
	
	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
}
