package org.openzen.zenscript.codemodel.scope;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.GenericName;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ZSPackage;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.statement.LoopStatement;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

import java.util.List;

public class ImplementationScope extends BaseScope {
	private final BaseScope outer;
	private final TypeMembers members;

	public ImplementationScope(BaseScope outer, ImplementationMember implementation) {
		this.outer = outer;
		members = outer.getTypeMembers(implementation.type);
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
		if (members.hasInnerType(name.name))
			return new PartialTypeExpression(position, members.getInnerType(position, name), name.arguments);
		if (members.hasMember(name.name))
			return members.getMemberExpression(position, this, new ThisExpression(position, outer.getThisType()), name, true);

		return outer.get(position, name);
	}

	@Override
	public TypeID getType(CodePosition position, List<GenericName> name) {
		if (members.hasInnerType(name.get(0).name)) {
			TypeID result = members.getInnerType(position, name.get(0));
			for (int i = 1; i < name.size(); i++) {
				result = getTypeMembers(result).getInnerType(position, name.get(i));
			}
			return result;
		}

		return outer.getType(position, name);
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
		return outer.getThisType();
	}

	@Override
	public DollarEvaluator getDollar() {
		return outer.getDollar();
	}

	@Override
	public IPartialExpression getOuterInstance(CodePosition position) {
		return new ThisExpression(position, outer.getThisType());
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
