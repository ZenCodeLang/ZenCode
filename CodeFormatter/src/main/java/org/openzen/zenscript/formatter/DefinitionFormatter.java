package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.definition.*;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;

import java.util.List;

public class DefinitionFormatter implements DefinitionVisitor<Void> {
	private final ScriptFormattingSettings settings;
	private final TypeFormatter typeFormatter;
	private final StringBuilder output = new StringBuilder();
	private final String indent;

	public DefinitionFormatter(ScriptFormattingSettings settings, TypeFormatter typeFormatter, String indent) {
		this.settings = settings;
		this.typeFormatter = typeFormatter;
		this.indent = indent;
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("class ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.typeParameters, typeFormatter);
		output.append(" ");
		if (definition.getSuperType() != null) {
			output.append("extends ");
			output.append(definition.getSuperType().accept(typeFormatter));
			output.append(" ");
		}
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}

		MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberFormatter);
		}

		output.append("}\n");
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("class ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.typeParameters, typeFormatter);
		output.append(" ");

		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}

		MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberFormatter);
		}

		output.append("}\n");
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("enum ");
		output.append(definition.name);
		FormattingUtils.formatTypeParameters(output, definition.typeParameters, typeFormatter);
		output.append(" ");

		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}

		List<EnumConstantMember> enumConstants = definition.enumConstants;
		boolean first = true;
		ExpressionFormatter expressionFormatter = new ExpressionFormatter(settings, typeFormatter, indent);
		for (EnumConstantMember enumConstant : enumConstants) {
			if (first)
				first = false;
			else
				output.append(",\n");

			output.append(indent).append(settings.indent).append(enumConstant.name);
			if (enumConstant.constructor != null) {
				FormattingUtils.formatCall(output, typeFormatter, expressionFormatter, enumConstant.constructor.arguments);
			}
		}

		if (definition.members.size() > enumConstants.size()) {
			output.append(";\n").append(indent).append(settings.indent).append("\n");

			MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
			for (IDefinitionMember member : definition.members) {
				member.accept(memberFormatter);
			}
		}

		output.append("}\n");
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		OperatorMember caller = definition.caller;
		FormattingUtils.formatModifiers(output, definition.modifiers);
		output.append("function ");
		output.append(definition.name);

		FormattingUtils.formatHeader(output, settings, caller.header, typeFormatter);
		FormattingUtils.formatBody(output, settings, indent, typeFormatter, caller.body);
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		FormattingUtils.formatModifiers(output, variant.modifiers);
		output.append("variant ");
		output.append(variant.name);
		FormattingUtils.formatTypeParameters(output, variant.typeParameters, typeFormatter);
		output.append(" ");

		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n")
					.append(indent)
					.append("{\n");
		}

		List<VariantDefinition.Option> options = variant.options;
		boolean first = true;
		for (VariantDefinition.Option option : options) {
			if (first)
				first = false;
			else
				output.append(",\n");

			output.append(indent).append(settings.indent).append(option.name);
			if (option.types.length > 0) {
				output.append("(");
				for (int i = 0; i < option.types.length; i++) {
					if (i > 0)
						output.append(", ");
					output.append(typeFormatter.format(option.types[i]));
				}
			}
		}

		if (variant.members.size() > options.size()) {
			output.append(";\n").append(indent).append(settings.indent).append("\n");

			MemberFormatter memberFormatter = new MemberFormatter(settings, output, indent + settings.indent, typeFormatter);
			for (IDefinitionMember member : variant.members) {
				member.accept(memberFormatter);
			}
		}

		output.append("}\n");
		return null;
	}

	@Override
	public String toString() {
		return output.toString();
	}
}
