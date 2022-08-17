package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;

import static org.openzen.zencode.shared.CodePosition.BUILTIN;
import static org.openzen.zenscript.codemodel.type.BasicTypeID.VOID;

public class StructDefinition extends HighLevelDefinition {
	public StructDefinition(CodePosition position, Module module, ZSPackage pkg, String name, Modifiers modifiers, TypeSymbol outerDefinition) {
		super(position, module, pkg, name, modifiers, outerDefinition);
	}

	@Override
	public <T> T accept(DefinitionVisitor<T> visitor) {
		return visitor.visitStruct(this);
	}

	@Override
	public <C, R> R accept(C context, DefinitionVisitorWithContext<C, R> visitor) {
		return visitor.visitStruct(context, this);
	}

	@Override
	protected void resolveAdditional(TypeID type, MemberSet.Builder members, GenericMapper mapper) {
		if (members.hasNoConstructor()) {
			// add default struct constructors (TODO: only works if all fields have a default value)
			constructor(definition, STRUCT_EMPTY_CONSTRUCTOR);

			List<FieldMember> fields = definition.getFields();
			if (!fields.isEmpty()) {
				FunctionParameter[] parameters = new FunctionParameter[fields.size()];
				for (int i = 0; i < parameters.length; i++) {
					FieldMember field = fields.get(i);
					parameters[i] = new FunctionParameter(field.getType(), field.name, field.initializer, false);
				}

				constructors.addMethod(new ConstructorMember(
						BUILTIN,
						definition,
						Modifiers.PUBLIC,
						new FunctionHeader(VOID, parameters),
						STRUCT_VALUE_CONSTRUCTOR).ref(type), TypeMemberPriority.SPECIFIED);
			}
		}
	}
}
