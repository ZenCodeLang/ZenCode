/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;

/**
 *
 * @author Hoofdgebruiker
 */
public class FunctionScope extends StatementScope {
	private final BaseScope outer;
	private final FunctionHeader header;
	private final GenericMapper typeParameterMap;
	private final TypeID thisType;
	private final DollarEvaluator dollar;
	
	public FunctionScope(CodePosition position, BaseScope outer, FunctionHeader header) {
		this(position, outer, header, null);
	}
	
	public FunctionScope(CodePosition position, BaseScope outer, FunctionHeader header, DollarEvaluator dollar) {
		this.outer = outer;
		this.header = header;
		this.thisType = outer.getThisType();
		this.dollar = dollar;
		
		if (outer.getLocalTypeParameters() == null)
			throw new NullPointerException();
		if (header == null)
			throw new NullPointerException();
		
		typeParameterMap = outer.getLocalTypeParameters().getInner(position, outer.getTypeRegistry(), header.typeParameters);
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
	public LoopStatement getLoop(String name) {
		return null; // not in a loop
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return header;
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) throws CompileException {
		IPartialExpression fromSuper = super.get(position, name);
		if (fromSuper != null)
			return fromSuper;
		
		if (name.hasNoArguments()) {
			for (FunctionParameter parameter : header.parameters) {
				if (parameter.name.equals(name.name)) {
					return new GetFunctionParameterExpression(position, parameter);
				}
			}
			
			if (header.typeParameters != null) {
				for (TypeParameter parameter : header.typeParameters) {
					if (parameter.name.equals(name.name))
						return new PartialTypeExpression(position, getTypeRegistry().getGeneric(parameter), name.arguments);
				}
			}
		}
		
		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (name.size() == 1 && name.get(0).hasNoArguments()) {
			if (header.typeParameters != null) {
				for (TypeParameter parameter : header.typeParameters) {
					if (parameter.name.equals(name.get(0).name))
						return getTypeRegistry().getGeneric(parameter);
				}
			}
		}
		
		return outer.getType(position, name);
	}

	@Override
	public TypeID getThisType() {
		return thisType;
	}

	@Override
	public DollarEvaluator getDollar() {
		return dollar;
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
		return typeParameterMap;
	}
}
