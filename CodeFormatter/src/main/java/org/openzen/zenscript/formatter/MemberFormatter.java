package org.openzen.zenscript.formatter;

import org.openzen.zenscript.codemodel.member.*;
import org.openzen.zenscript.codemodel.statement.Statement;

public class MemberFormatter implements MemberVisitor<Void> {
	private final ScriptFormattingSettings settings;
	private final StringBuilder output;
	private final String indent;
	private final TypeFormatter typeFormatter;
	private boolean isFirst = true;
	private boolean wasField = false;

	public MemberFormatter(ScriptFormattingSettings settings, StringBuilder output, String indent, TypeFormatter typeFormatter) {
		this.settings = settings;
		this.output = output;
		this.indent = indent;
		this.typeFormatter = typeFormatter;
	}

	private void visit(boolean field) {
		output.append(indent);

		if (isFirst) {
			isFirst = false;
		} else if (!field || (wasField != field)) {
			output.append("\n").append(indent);
		}

		wasField = field;
	}

	@Override
	public Void visitField(FieldMember member) {
		visit(true);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append(member.getModifiers().isConst() ? "const " : (member.isFinal() ? "val " : "var "))
				.append(member.name)
				.append(" as ")
				.append(typeFormatter.format(member.getType()));

		if (member.initializer != null) {
			output.append(" = ")
					.append(member.initializer.accept(new ExpressionFormatter(settings, typeFormatter, indent)));
		}
		output.append(";\n");
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append("this");
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append(member.name);
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append("get ");
		output.append(member.name);
		output.append(" as ");
		output.append(typeFormatter.format(member.type));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append("set ");
		output.append(member.name);
		output.append(" as ");
		output.append(typeFormatter.format(member.parameter.type));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		switch (member.operator) {
			case ADD:
				output.append("+");
				break;
			case SUB:
				output.append("-");
				break;
			case MUL:
				output.append("*");
				break;
			case DIV:
				output.append("/");
				break;
			case MOD:
				output.append("%");
				break;
			case CAT:
				output.append("~");
				break;
			case OR:
				output.append("|");
				break;
			case AND:
				output.append("&");
				break;
			case XOR:
				output.append("^");
				break;
			case NEG:
				output.append("-");
				break;
			case NOT:
				output.append("!");
				break;
			case INDEXSET:
				output.append("[]=");
				break;
			case INDEXGET:
				output.append("[]");
				break;
			case CONTAINS:
				output.append("in");
				break;
			case MEMBERGETTER:
				output.append(".");
				break;
			case MEMBERSETTER:
				output.append(".=");
				break;
			case EQUALS:
				output.append("==");
				break;
			case ADDASSIGN:
				output.append("+=");
				break;
			case SUBASSIGN:
				output.append("-=");
				break;
			case MULASSIGN:
				output.append("*=");
				break;
			case DIVASSIGN:
				output.append("/=");
				break;
			case MODASSIGN:
				output.append("%=");
				break;
			case CATASSIGN:
				output.append("~=");
				break;
			case ORASSIGN:
				output.append("|=");
				break;
			case ANDASSIGN:
				output.append("&=");
				break;
			case XORASSIGN:
				output.append("^=");
				break;

			case INCREMENT:
				output.append("++");
				break;
			case DECREMENT:
				output.append("--");
				break;

			case CALL:
				break;
			case DESTRUCTOR:
				output.append("~this");
				break;
			default:
				throw new UnsupportedOperationException("Unknown operator: " + member.operator);
		}
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		visit(false);
		FormattingUtils.formatModifiers(output, member.getSpecifiedModifiers());
		output.append(" as ");
		output.append(typeFormatter.format(member.toType));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitCustomIterator(IteratorMember member) {
		visit(false);
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitImplementation(ImplementationMember implementation) {
		visit(false);
		FormattingUtils.formatModifiers(output, implementation.getSpecifiedModifiers());
		output.append("implements ");
		output.append(implementation.type.accept(typeFormatter));
		if (settings.classBracketOnSameLine) {
			output.append("{\n");
		} else {
			output.append("\n").append(indent).append("{\n");
		}

		for (IDefinitionMember member : implementation.members) {
			member.accept(new MemberFormatter(settings, output, indent + settings.indent, typeFormatter));
		}

		output.append("}\n");
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		visit(false);
		String formatted = member.innerDefinition.accept(new DefinitionFormatter(settings, typeFormatter, indent + settings.indent)).toString();
		output.append(formatted);
		return null;
	}

	private void formatBody(Statement body) {
		FormattingUtils.formatBody(output, settings, indent, typeFormatter, body);
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		visit(false);
		output.append("static ");
		formatBody(member.body);
		return null;
	}
}
