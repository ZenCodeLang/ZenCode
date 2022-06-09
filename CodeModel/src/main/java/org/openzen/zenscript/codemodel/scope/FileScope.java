package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialGlobalExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ISymbol;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FileScope extends BaseScope {
	private final TypeResolutionContext context;
	private final ZSPackage root;
	private final LocalMemberCache memberCache;
	private final Map<String, ISymbol> globals;
	private final TypeMemberPreparer preparer;

	public FileScope(
			TypeResolutionContext context,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globals) {
		this(context, expansions, globals, member -> {
		});
	}

	public FileScope(
			TypeResolutionContext context,
			List<ExpansionDefinition> expansions,
			Map<String, ISymbol> globals,
			TypeMemberPreparer preparer) {
		this.context = context;
		this.globals = globals;
		this.preparer = preparer;
		this.root = context.getRootPackage();

		memberCache = new LocalMemberCache(context.getTypeRegistry(), expansions);
	}

	@Override
	public ZSPackage getRootPackage() {
		return root;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return memberCache;
	}

	@Override
	public IPartialExpression get(CodePosition position, GenericName name) {
		TypeID type = context.getType(position, Collections.singletonList(name));
		if (type != null)
			return new PartialTypeExpression(position, type, name.arguments);

		/*if (globals.containsKey(name.name)) {
			IPartialExpression resolution = globals.get(name.name).getExpression(position, name.arguments);
			return new PartialGlobalExpression(position, name.name, resolution, name.arguments);
		} else if (root.contains(name.name)) {
			try {
				return root.getMember(position, context.getTypeRegistry(), name);
			} catch (CompileException ex) {
				return new InvalidExpression(BasicTypeID.UNDETERMINED, ex);
			}
		}*/

		return null;
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		TypeID type = context.getType(position, name);
		if (type != null)
			return type;

		/*if (globals.containsKey(name.get(0).name)) {
			type = globals.get(name.get(0).name).getType(position, context, name.get(0).arguments);
			for (int i = 1; i < name.size(); i++) {
				type = getTypeMembers(type).getInnerType(position, name.get(i));
				if (type == null)
					break;
			}

			if (type != null)
				return type;
		}*/

		return null;
	}

	@Override
	public LoopStatement getLoop(String name) {
		return null;
	}

	@Override
	public FunctionHeader getFunctionHeader() {
		return null;
	}

	@Override
	public TypeID getThisType() {
		return null;
	}

	@Override
	public DollarEvaluator getDollar() {
		return null;
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.NO_OUTER_BECAUSE_OUTSIDE_TYPE, "Not in an inner type");
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return context.getAnnotation(name);
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return preparer;
	}

	@Override
	public GenericMapper getLocalTypeParameters() {
		return GenericMapper.EMPTY;
	}
}
