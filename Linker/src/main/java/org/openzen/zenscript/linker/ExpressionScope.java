/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.linker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ExpressionScope extends BaseScope {
	private final BaseScope outer;
	private final Function<CodePosition, Expression> dollar;
	
	public final List<ITypeID> hints;
	public final Map<TypeParameter, ITypeID> genericInferenceMap;
	
	public ExpressionScope(BaseScope outer) {
		this.outer = outer;
		this.hints = Collections.emptyList();
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	public ExpressionScope(BaseScope outer, List<ITypeID> hints) {
		this.outer = outer;
		this.hints = hints;
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	public ExpressionScope(BaseScope scope, ITypeID hint) {
		this.outer = scope;
		this.hints = Collections.singletonList(hint);
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}
	
	public ExpressionScope(BaseScope scope, List<ITypeID> hints, Function<CodePosition, Expression> dollar, Map<TypeParameter, ITypeID> genericInferenceMap) {
		this.outer = scope;
		this.hints = hints;
		this.dollar = dollar;
		this.genericInferenceMap = genericInferenceMap;
	}
	
	public List<ITypeID> getResultTypeHints() {
		return hints;
	}
	
	public ExpressionScope withoutHints() {
		return new ExpressionScope(outer, Collections.emptyList(), dollar, genericInferenceMap);
	}
	
	public ExpressionScope withHint(ITypeID hint) {
		return new ExpressionScope(outer, Collections.singletonList(hint), dollar, genericInferenceMap);
	}
	
	public ExpressionScope withHints(List<ITypeID> hints) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap);
	}
	
	public ExpressionScope createInner(List<ITypeID> hints, Function<CodePosition, Expression> dollar) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap);
	}
	
	public ExpressionScope forCall(FunctionHeader header) {
		if (header.typeParameters.length == 0)
			return this;
		
		Map<TypeParameter, ITypeID> genericInferenceMap = new HashMap<>();
		for (TypeParameter parameter : header.typeParameters)
			genericInferenceMap.put(parameter, null);
		
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap);
	}
	
	@Override
	public LocalMemberCache getMemberCache() {
		return outer.getMemberCache();
	}
	
	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		return outer.get(position, name);
	}
	
	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		return outer.getType(position, name);
	}
	
	@Override
	public Statement getLoop(String name) {
		return outer.getLoop(name);
	}
	
	@Override
	public FunctionHeader getFunctionHeader() {
		return outer.getFunctionHeader();
	}
	
	@Override
	public ITypeID getThisType() {
		return outer.getThisType();
	}

	@Override
	public Function<CodePosition, Expression> getDollar() {
		return dollar == null ? outer.getDollar() : dollar;
	}
	
	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return outer.getOuterInstance(position);
	}
}
