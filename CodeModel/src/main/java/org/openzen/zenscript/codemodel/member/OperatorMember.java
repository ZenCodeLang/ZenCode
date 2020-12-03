package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.member.ref.FunctionalMemberRef;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

public class OperatorMember extends FunctionalMember {
	public final OperatorType operator;
	private FunctionalMemberRef overrides;
	
	public OperatorMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			OperatorType operator,
			FunctionHeader header,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, header, builtin);
		
		this.operator = operator;
	}
	
	@Override
	public String getCanonicalName() {
		return definition.getFullName() + ":" + operator.operator + header.getCanonical();
	}
	
	@Override
	public FunctionalKind getKind() {
		return FunctionalKind.OPERATOR;
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority, GenericMapper mapper) {
		type.addOperator(operator, ref(type.type, mapper), priority);
	}

	@Override
	public String describe() {
		return operator.operator + header.toString();
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitOperator(this);
	}
	
	@Override
	public <C, R> R accept(C context, MemberVisitorWithContext<C, R> visitor) {
		return visitor.visitOperator(context, this);
	}
	
	@Override
	public int getEffectiveModifiers() {
		int result = super.getEffectiveModifiers();
		if (overrides != null) {
			if (overrides.getTarget().isPublic())
				result |= Modifiers.PUBLIC;
			if (overrides.getTarget().isProtected())
				result |= Modifiers.PROTECTED;
		}
		return result;
	}

	@Override
	public FunctionalMemberRef getOverrides() {
		return overrides;
	}
	
	public void setOverrides(GlobalTypeRegistry registry, FunctionalMemberRef overrides) {
		this.overrides = overrides;
		header = header.inferFromOverride(registry, overrides.getHeader());
	}
}
