/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.scope;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetLocalVariableExpression;
import org.openzen.zenscript.codemodel.expression.GetMatchingVariantField;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.storage.StorageTag;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionScope extends BaseScope {
	private final BaseScope outer;
	private final DollarEvaluator dollar;
	
	public final List<StoredType> hints;
	public final Map<TypeParameter, StoredType> genericInferenceMap;
	public final Map<String, Function<CodePosition, Expression>> innerVariables = new HashMap<>();
	
	public ExpressionScope(BaseScope outer) {
		this.outer = outer;
		this.hints = Collections.emptyList();
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	public ExpressionScope(BaseScope outer, List<StoredType> hints) {
		this.outer = outer;
		this.hints = hints;
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	public ExpressionScope(BaseScope scope, StoredType hint) {
		this.outer = scope;
		this.hints = hint.type == BasicTypeID.UNDETERMINED ? Collections.emptyList() : Collections.singletonList(hint);
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	private ExpressionScope(
			BaseScope scope,
			List<StoredType> hints,
			DollarEvaluator dollar,
			Map<TypeParameter, StoredType> genericInferenceMap,
			Map<String, Function<CodePosition, Expression>> innerVariables) {
		this.outer = scope;
		this.hints = hints;
		this.dollar = dollar;
		this.genericInferenceMap = genericInferenceMap;
		this.innerVariables.putAll(innerVariables);
	}
	
	public void addInnerVariable(VarStatement variable) {
		innerVariables.put(variable.name, position -> new GetLocalVariableExpression(position, variable));
	}
	
	public void addMatchingVariantOption(String name, int index, VariantOptionSwitchValue value) {
		innerVariables.put(name, position -> new GetMatchingVariantField(position, value, index));
	}
	
	public List<StoredType> getResultTypeHints() {
		return hints;
	}
	
	public ExpressionScope withoutHints() {
		return new ExpressionScope(outer, Collections.emptyList(), dollar, genericInferenceMap, innerVariables);
	}
	
	public ExpressionScope withHint(StoredType hint) {
		return new ExpressionScope(outer, Collections.singletonList(hint), dollar, genericInferenceMap, innerVariables);
	}
	
	public ExpressionScope withHints(List<StoredType> hints) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap, innerVariables);
	}
	
	public ExpressionScope createInner(List<StoredType> hints, DollarEvaluator dollar) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap, innerVariables);
	}
	
	public ExpressionScope forCall(FunctionHeader header) {
		if (header.typeParameters == null)
			return this;
		
		Map<TypeParameter, StoredType> genericInferenceMap = new HashMap<>();
		for (TypeParameter parameter : header.typeParameters)
			genericInferenceMap.put(parameter, null);
		
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap, innerVariables);
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
		if (name.hasNoArguments() && innerVariables.containsKey(name.name))
			return innerVariables.get(name.name).apply(position);
		
		return outer.get(position, name);
	}
	
	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
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
		return dollar == null ? outer.getDollar() : dollar;
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
