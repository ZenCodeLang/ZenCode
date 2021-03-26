package org.openzen.zenscript.codemodel.context;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TypeContext {
	public final TypeID thisType;
	public final ModuleContext moduleContext;
	protected final CodePosition position;
	protected final TypeParameter[] typeParameters;
	private final LocalMemberCache memberCache;

	@Deprecated
	public TypeContext(ModuleContext context, TypeParameter[] parameters, TypeID thisType) {
		//FIXME: What position?
		this(CodePosition.UNKNOWN, context, parameters, thisType);
	}

	public TypeContext(CodePosition position, ModuleContext context, TypeParameter[] parameters, TypeID thisType) {
		this.position = position;
		this.typeParameters = parameters;
		this.thisType = thisType;
		memberCache = new LocalMemberCache(context.registry, context.expansions);
		moduleContext = context;
	}

	@Deprecated
	public TypeContext(TypeContext outer, TypeID thisType, TypeParameter... inner) {
		this(outer.getPosition(), outer, thisType, inner);
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

	public static <T> T[] concat(T[] first, T[] second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
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

	public CodePosition getPosition() {
		return position;
	}
}
