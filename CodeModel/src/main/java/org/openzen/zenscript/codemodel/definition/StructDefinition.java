package org.openzen.zenscript.codemodel.definition;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.identifiers.TypeSymbol;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.codemodel.type.builtin.BuiltinMethodSymbol;
import org.openzen.zenscript.codemodel.type.member.MemberSet;

import java.util.List;

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
			List<FieldMember> fields = getFields();
			boolean noUninitializedFields = true;
			if (!fields.isEmpty()) {
				FunctionParameter[] parameters = new FunctionParameter[fields.size()];
				for (int i = 0; i < parameters.length; i++) {
					FieldMember field = fields.get(i);
					noUninitializedFields &= field.initializer != null;
					parameters[i] = new FunctionParameter(field.getType(), field.name, field.initializer, false);
				}

				members.constructor(new MethodInstance(BuiltinMethodSymbol.STRUCT_DEFAULT_CONSTRUCTOR, new FunctionHeader(VOID, parameters), type));
			}

			if (noUninitializedFields) {
				members.constructor(mapper.map(type, BuiltinMethodSymbol.STRUCT_EMPTY_CONSTRUCTOR));
			}
		}
	}
}
