package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.Module;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnumDefinition extends HighLevelDefinition {
	public TypeID asType;
	public List<EnumConstantMember> enumConstants = new ArrayList<>();

	public EnumDefinition(CodePosition position, Module module, ZSPackage pkg, String name, int modifiers, TypeSymbol outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitEnum(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitEnum(context, this);
	}

	@Override
	public Optional<EnumDefinition> asEnum() {
		return Optional.of(this);
	}

	@Override
	public void collectMembers(MemberCollector collector) {
		super.collectMembers(collector);

		for (EnumConstantMember member : enumConstants)
			collector.enumConstant(member);
	}

	public void addEnumConstant(EnumConstantMember constant) {
		enumConstants.add(constant);
	}
}
