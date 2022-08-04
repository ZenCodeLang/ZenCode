package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.expression.ArrayExpression;
import org.openzen.zenscript.codemodel.expression.EnumConstantExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.type.ArrayTypeID;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.*;
import static org.openzen.zenscript.codemodel.type.member.BuiltinID.*;

public class EnumDefinition extends HighLevelDefinition {
	public TypeID asType;
	public List<EnumConstantMember> enumConstants = new ArrayList<>();

	public EnumDefinition(CodePosition position, Module module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
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
		members.getter(mapper.map(type, BuiltinMethodSymbol.ENUM_NAME));
		members.getter(mapper.map(type, BuiltinMethodSymbol.ENUM_ORDINAL));

		members.staticGetter(new MethodInstance(BuiltinMethodSymbol.ENUM_VALUES, new FunctionHeader(new ArrayTypeID(type)), type));
		members.operator(new MethodInstance(BuiltinMethodSymbol.ENUM_COMPARE, new FunctionHeader(BasicTypeID.BOOL, type), type));

		//if (!members.canCast(STRING)) {
		//	castImplicit(definition, ENUM_TO_STRING, STRING);
		//}
		for (EnumConstantMember constant : enumConstants) {
			members.contextMember(constant);
		}

		if (members.hasNoConstructor()) {
			members.constructor(new ConstructorMember(
					BUILTIN,
					definition,
					Modifiers.PRIVATE,
					new FunctionHeader(VOID),
					ENUM_EMPTY_CONSTRUCTOR).ref(type), TypeMemberPriority.SPECIFIED);
		}
	}

	public void addEnumConstant(EnumConstantMember constant) {
		enumConstants.add(constant);
	}
}
