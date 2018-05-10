/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.formatter;

import java.util.List;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.statement.Statement;

/**
 *
 * @author Hoofdgebruiker
 */
public class MemberFormatter implements MemberVisitor<Void> {
	private final FormattingSettings settings;
	private final StringBuilder output;
	private final String indent;
	private final TypeFormatter typeFormatter;
	
	public MemberFormatter(FormattingSettings settings, StringBuilder output, String indent, TypeFormatter typeFormatter) {
		this.settings = settings;
		this.output = output;
		this.indent = indent;
		this.typeFormatter = typeFormatter;
	}

	@Override
	public Void visitField(FieldMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append(member.isFinal() ? "val " : "var ")
				.append(member.name)
				.append(" as ")
				.append(member.type.accept(typeFormatter));
		
		if (member.initializer != null) {
			output.append(" = ")
					.append(member.initializer.accept(new ExpressionFormatter(settings, typeFormatter)));
		}
		output.append(";").append("\n").append(indent);
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append("this");
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitMethod(MethodMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append(member.name);
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append("get ");
		output.append(member.name);
		output.append(" as ");
		output.append(member.type.accept(typeFormatter));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append("set ");
		output.append(member.name);
		output.append(" as ");
		output.append(member.type.accept(typeFormatter));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitEnumConstant(EnumConstantMember member) {
		return null; // TODO: handle separately
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		switch (member.operator) {
			case ADD: output.append("+"); break;
			case SUB: output.append("-"); break;
			case MUL: output.append("*"); break;
			case DIV: output.append("/"); break;
			case MOD: output.append("%"); break;
			case CAT: output.append("~"); break;
			case OR: output.append("|"); break;
			case AND: output.append("&"); break;
			case XOR: output.append("^"); break;
			case NEG: output.append("-"); break;
			case NOT: output.append("!"); break;
			case INDEXSET: output.append("[]="); break;
			case INDEXGET: output.append("[]"); break;
			case CONTAINS: output.append("in"); break;
			case MEMBERGETTER: output.append("."); break;
			case MEMBERSETTER: output.append(".="); break;
			case EQUALS: output.append("=="); break;
			case ADDASSIGN: output.append("+="); break;
			case SUBASSIGN: output.append("-="); break;
			case MULASSIGN: output.append("*="); break;
			case DIVASSIGN: output.append("/="); break;
			case MODASSIGN: output.append("%="); break;
			case CATASSIGN: output.append("~="); break;
			case ORASSIGN: output.append("|="); break;
			case ANDASSIGN: output.append("&="); break;
			case XORASSIGN: output.append("^="); break;
	
			case POST_INCREMENT: output.append("++"); break;
			case POST_DECREMENT: output.append("--"); break;
			default:
				throw new UnsupportedOperationException("Unknown operator: " + member.operator);
		}
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		output.append(" as ");
		output.append(member.toType.accept(typeFormatter));
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitCustomIterator(CustomIteratorMember member) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Void visitCaller(CallerMember member) {
		FormattingUtils.formatModifiers(output, member.modifiers & ~Modifiers.FINAL);
		FormattingUtils.formatHeader(output, settings, member.header, typeFormatter);
		formatBody(member.body);
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember implementation) {
		FormattingUtils.formatModifiers(output, implementation.modifiers & ~Modifiers.FINAL);
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
		String formatted = member.innerDefinition.accept(new DefinitionFormatter(settings, typeFormatter, indent + settings.indent)).toString();
		output.append(formatted);
		return null;
	}
	
	private void formatBody(Statement body) {
		FormattingUtils.formatBody(output, settings, indent, typeFormatter, body);
	}
}
