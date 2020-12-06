package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetMatchingVariantField;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ExpressionScope extends BaseScope {
	public final List<TypeID> hints;
	public final Map<TypeParameter, TypeID> genericInferenceMap;
	private final BaseScope outer;
	private final DollarEvaluator dollar;
	private final Map<String, Function<CodePosition, Expression>> innerVariables = new HashMap<>();

	public ExpressionScope(BaseScope outer) {
		this.outer = outer;
		this.hints = Collections.emptyList();
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}

	public ExpressionScope(BaseScope outer, List<TypeID> hints) {
		this.outer = outer;
		this.hints = hints;
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}

	public ExpressionScope(BaseScope scope, TypeID hint) {
		this.outer = scope;
		this.hints = hint == BasicTypeID.UNDETERMINED ? Collections.emptyList() : Collections.singletonList(hint);
		this.dollar = null;
		this.genericInferenceMap = Collections.emptyMap();
	}

	private ExpressionScope(
			BaseScope scope,
			List<TypeID> hints,
			DollarEvaluator dollar,
			Map<TypeParameter, TypeID> genericInferenceMap,
			Map<String, Function<CodePosition, Expression>> innerVariables) {
		this.outer = scope;
		this.hints = hints;
		this.dollar = dollar;
		this.genericInferenceMap = genericInferenceMap;
		this.innerVariables.putAll(innerVariables);
	}

	public void addMatchingVariantOption(String name, int index, VariantOptionSwitchValue value) {
		innerVariables.put(name, position -> new GetMatchingVariantField(position, value, index));
	}

	public List<TypeID> getResultTypeHints() {
		return hints;
	}

	public ExpressionScope withoutHints() {
		return new ExpressionScope(outer, Collections.emptyList(), dollar, genericInferenceMap, innerVariables);
	}

	public ExpressionScope withHint(TypeID hint) {
		return new ExpressionScope(outer, Collections.singletonList(hint), dollar, genericInferenceMap, innerVariables);
	}

	public ExpressionScope withHints(List<TypeID> hints) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap, innerVariables);
	}

	public ExpressionScope createInner(List<TypeID> hints, DollarEvaluator dollar) {
		return new ExpressionScope(outer, hints, dollar, genericInferenceMap, innerVariables);
	}

	public ExpressionScope forCall(FunctionHeader header) {
		if (header.typeParameters == null)
			return this;

		Map<TypeParameter, TypeID> genericInferenceMap = new HashMap<>();
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
	public LoopStatement getLoop(String name) {
		return outer.getLoop(name);
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return outer.getFunctionHeader();
	}

	@Override
	public TypeID getThisType() {
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
