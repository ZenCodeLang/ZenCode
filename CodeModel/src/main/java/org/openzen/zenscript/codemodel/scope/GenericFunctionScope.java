/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;

/**
 *
 * @author Hoofdgebruiker
 */
public class GenericFunctionScope extends BaseScope {
	private final BaseScope outer;
	private final Map<String, TypeParameter> parameters = new HashMap<>();
	
	public GenericFunctionScope(BaseScope outer, TypeParameter[] parameters) {
		this.outer = outer;
		
		if (parameters != null)
			for (TypeParameter parameter : parameters)
				this.parameters.put(parameter.name, parameter);
	}
	
	@Override
	public ZSPackage getRootPackage() {
		return outer.getRootPackage();
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		if (parameters.containsKey(name.name) && name.hasNoArguments())
			return new PartialTypeExpression(position, getTypeRegistry().getGeneric(parameters.get(name.name)), name.arguments);
		
		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (name.size() == 1 && parameters.containsKey(name.get(0).name) && name.get(0).hasNoArguments())
			return getTypeRegistry().getGeneric(parameters.get(name.get(0).name));
		
		return outer.getType(position, name);
	}

	@Override
	public StorageTag getStorageTag(CodePosition position, String name, String[] parameters) {
		return outer.getStorageTag(position, name, parameters);
	}

	@Override
	public LoopStatement getLoop(String name) {
		return outer.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return outer.getFunctionHeader();
	}

	@Override
	public StoredType getThisType() {
		return outer.getThisType();
	}

	@Override
	public DollarEvaluator getDollar() {
		return outer.getDollar();
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
		return outer.getOuterInstance(position);
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return outer.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return outer.getPreparer();
	}

	@Override
	public GenericMapper getLocalTypeParameters() {
		return outer.getLocalTypeParameters();
	}
}
