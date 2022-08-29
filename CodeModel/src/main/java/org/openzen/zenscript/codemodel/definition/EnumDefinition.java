package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.ModuleSymbol;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.ArrayList;
import java.util.List;

public class EnumDefinition extends HighLevelDefinition {
	public TypeID asType;
	public List<EnumConstantMember> enumConstants = new ArrayList<>();

	public EnumDefinition(CodePosition position, ModuleSymbol module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
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
	public void collectMembers(MemberCollector collector) {
		super.collectMembers(collector);

		for (EnumConstantMember member : enumConstants)
			collector.enumConstant(member);
	}

	@Override
	protected void resolveAdditional(TypeID type, MemberSet.Builder members, GenericMapper mapper) {
		members.method(mapper.map(type, BuiltinMethodSymbol.ENUM_NAME));
		members.method(mapper.map(type, BuiltinMethodSymbol.ENUM_ORDINAL));

		members.method(new MethodInstance(BuiltinMethodSymbol.ENUM_VALUES, new FunctionHeader(new ArrayTypeID(type)), type));
		members.method(new MethodInstance(BuiltinMethodSymbol.ENUM_COMPARE, new FunctionHeader(BasicTypeID.BOOL, type), type));

		//if (!members.canCast(STRING)) {
		//	castImplicit(definition, ENUM_TO_STRING, STRING);
		//}
		for (EnumConstantMember constant : enumConstants) {
			members.contextMember(constant.name, constant);
		}

		if (members.hasNoConstructor()) {
			members.constructor(new MethodInstance(BuiltinMethodSymbol.ENUM_EMPTY_CONSTRUCTOR));
		}
	}

	public void addEnumConstant(EnumConstantMember constant) {
		enumConstants.add(constant);
	}
}
