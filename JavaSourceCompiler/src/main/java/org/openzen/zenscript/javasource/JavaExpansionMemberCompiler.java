/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.CustomIteratorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;
import org.openzen.zenscript.codemodel.statement.BlockStatement;
import org.openzen.zenscript.codemodel.statement.EmptyStatement;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javasource.scope.JavaSourceFileScope;
import org.openzen.zenscript.javasource.scope.JavaSourceStatementScope;
import org.openzen.zenscript.shared.StringUtils;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaExpansionMemberCompiler implements MemberVisitor<Void> {
	private final String indent;
	private final StringBuilder output;
	private final JavaSourceFileScope scope;
	private final JavaSourceStatementScope fieldInitializerScope;
	private final JavaSourceFormattingSettings settings;
	private final List<FieldMember> fields = new ArrayList<>();
	private final ITypeID targetType;
	private boolean first = true;
	
	public JavaExpansionMemberCompiler(JavaSourceFormattingSettings settings, ITypeID targetType, String indent, StringBuilder output, JavaSourceFileScope scope) {
		this.indent = indent;
		this.output = output;
		this.scope = scope;
		this.settings = settings;
		this.targetType = targetType;
		
		fieldInitializerScope = new JavaSourceStatementScope(
				scope,
				settings,
				null,
				indent + settings.indent,
				null,
				null,
				true);
	}
	
	private void addSpacing() {
		if (first)
			first = false;
		else
			output.append(indent).append("\n");
	}
	
	@Override
	public Void visitConst(ConstMember member) {
		modifiers(member.modifiers | Modifiers.STATIC | Modifiers.FINAL);
		output.append(scope.type(member.type));
		output.append(" ");
		output.append(member.name);
		output.append(" = ");
		output.append(fieldInitializerScope.expression(null, member.value));
		output.append(";\n");
		return null;
	}

	@Override
	public Void visitField(FieldMember member) {
		// TODO: nonstatic fields!
		first = false;
		
		output.append(indent);
		int modifiers = 0;
		if (member.isStatic())
			modifiers |= Modifiers.STATIC;
		if (member.isFinal())
			modifiers |= Modifiers.FINAL;
		if (member.autoGetterAccess != 0 && (member.isFinal() ? member.autoSetterAccess == 0 : member.autoGetterAccess == member.autoSetterAccess))
			modifiers |= member.autoGetterAccess;
		else
			modifiers |= Modifiers.PRIVATE;
		
		this.modifiers(modifiers);
		
		output.append(scope.type(member.type));
		output.append(" ");
		output.append(member.name);
		if (member.initializer != null) {
			output.append(" = ");
			output.append(fieldInitializerScope.expression(null, member.initializer)); // TODO: formatting target
		}
		output.append(";\n");
		
		fields.add(member);
		return null;
	}

	@Override
	public Void visitConstructor(ConstructorMember member) {
		// TODO: constructors
		/*addSpacing();
		
		output.append(indent);
		modifiers(member.modifiers);
		JavaSourceUtils.formatTypeParameters(importer, output, member.header.typeParameters);
		output.append(importer.getName());
		formatParameters(true, member.header);
		compileBody(member.body, member.header);*/
		throw new UnsupportedOperationException("Expansions cannot declare constructors yet");
	}

	@Override
	public Void visitDestructor(DestructorMember member) {
		throw new UnsupportedOperationException("Expansions cannot declare destructors");
	}

	@Override
	public Void visitMethod(MethodMember member) {
		addSpacing();
		
		output.append(indent);
		
		modifiers(member.modifiers | Modifiers.STATIC);
		JavaSourceUtils.formatTypeParameters(scope, output, member.header.typeParameters);
		output.append(scope.type(member.header.returnType));
		output.append(" ");
		output.append(member.name);
		formatParameters(member.isStatic(), member.header);
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitGetter(GetterMember member) {
		addSpacing();
		
		output.append(indent);
		modifiers(member.modifiers | Modifiers.STATIC);
		output.append(scope.type(member.type));
		output.append(" ");
		output.append("get").append(StringUtils.capitalize(member.name));
		output.append("()");
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitSetter(SetterMember member) {
		addSpacing();
		
		output.append(indent);
		modifiers(member.modifiers | Modifiers.STATIC);
		output.append("void set").append(StringUtils.capitalize(member.name));
		output.append("(");
		output.append(scope.type(member.type));
		output.append(" ");
		output.append("value");
		output.append(")");
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitOperator(OperatorMember member) {
		addSpacing();
		
		output.append(indent);
		modifiers(member.modifiers | Modifiers.STATIC);
		output.append(scope.type(member.header.returnType));
		output.append(' ');
		output.append(getOperatorName(member.operator));
		formatParameters(member.isStatic(), member.header);
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitCaster(CasterMember member) {
		addSpacing();
		
		output.append(indent);
		modifiers(member.modifiers | Modifiers.STATIC);
		output.append(scope.type(member.toType));
		output.append(" ");
		output.append("to").append(member.toType.accept(new JavaSourceTypeNameVisitor()));
		output.append("(").append(scope.type(targetType)).append(" self)");
		compileBody(member.body, member.header);
		return null;
	}

	@Override
	public Void visitCustomIterator(CustomIteratorMember member) {
		addSpacing();
		
		return null;
	}

	@Override
	public Void visitCaller(CallerMember member) {
		addSpacing();
		
		return null;
	}

	@Override
	public Void visitImplementation(ImplementationMember member) {
		return null;
	}

	@Override
	public Void visitInnerDefinition(InnerDefinitionMember member) {
		return null;
	}

	@Override
	public Void visitStaticInitializer(StaticInitializerMember member) {
		addSpacing();
		output.append(indent).append("static ");
		compileBody(member.body, new FunctionHeader(BasicTypeID.VOID));
		return null;
	}
	
	public void finish() {
		// TODO: needs to be moved elsewhere (normalization stage?)
		for (FieldMember field : fields) {
			if (field.autoGetter != null)
				visitGetter(field.autoGetter);
			if (field.autoSetter != null)
				visitSetter(field.autoSetter);
		}
	}
	
	private void modifiers(int modifiers) {
		if (Modifiers.isPublic(modifiers))
			output.append("public ");
		if (Modifiers.isProtected(modifiers))
			output.append("protected ");
		if (Modifiers.isPrivate(modifiers))
			output.append("private ");
		if (Modifiers.isStatic(modifiers))
			output.append("static ");
		if (Modifiers.isFinal(modifiers))
			output.append("final ");
	}
	
	private void compileBody(Statement body, FunctionHeader header) {
		if (body == null) {
			output.append(";\n");
		} else {
			if (!(body instanceof BlockStatement))
				body = new BlockStatement(body.position, Collections.singletonList(body));
			
			JavaSourceStatementScope scope = new JavaSourceStatementScope(this.scope, settings, header, indent + settings.indent, null, null, true);
			body.accept(new JavaSourceStatementCompiler(scope, output, true, false));
			output.append('\n');
		}
	}
	
	private void formatParameters(boolean isStatic, FunctionHeader header) {
		output.append("(");
		boolean first = true;
		if (!isStatic) {
			first = false;
			output.append(scope.type(targetType));
			output.append(" self");
		}
		if (header.typeParameters != null) {
			for (TypeParameter typeParameter : header.typeParameters) {
				if (first)
					first = false;
				else
					output.append(", ");

				output.append("Class<")
						.append(typeParameter.name)
						.append(">")
						.append(" ")
						.append("typeOf")
						.append(typeParameter.name);
			}
		}
		
		for (int i = 0; i < header.parameters.length; i++) {
			if (first)
				first = false;
			else
				output.append(", ");
			
			FunctionParameter parameter = header.parameters[i];
			output.append(scope.type(parameter.type));
			output.append(" ").append(parameter.name);
			if (parameter.variadic)
				output.append("...");
		}
		output.append(")");
	}
	
	private String getOperatorName(OperatorType operator) {
		switch (operator) {
			case NEG:
				return "negate";
			case NOT:
				return "invert";
			case ADD:
				return "add";
			case ADDASSIGN:
				return "addAssign";
			case SUB:
				return "subtract";
			case SUBASSIGN:
				return "subAssign";
			case CAT:
				return "concat";
			case CATASSIGN:
				return "append";
			case MUL:
				return "mul";
			case MULASSIGN:
				return "mulAssign";
			case DIV:
				return "div";
			case DIVASSIGN:
				return "divAssign";
			case MOD:
				return "mod";
			case MODASSIGN:
				return "modAssign";
			case AND:
				return "and";
			case ANDASSIGN:
				return "andAssign";
			case OR:
				return "or";
			case ORASSIGN:
				return "orAssign";
			case XOR:
				return "xor";
			case XORASSIGN:
				return "xorAssign";
			case SHL:
				return "shl";
			case SHLASSIGN:
				return "shlAssign";
			case SHR:
				return "shr";
			case SHRASSIGN:
				return "shrAssign";
			case USHR:
				return "ushr";
			case USHRASSIGN:
				return "ushrAssign";
			case INDEXGET:
				return "getAt";
			case INDEXSET:
				return "setAt";
			case INCREMENT:
				return "increment";
			case DECREMENT:
				return "decrement";
			case CONTAINS:
				return "contains";
			case EQUALS:
				return "equals";
			case COMPARE:
				return "compareTo";
			case RANGE:
				return "until";
			case CAST:
				return "cast";
			case CALL:
				return "call";
			case MEMBERGETTER:
				return "getMember";
			case MEMBERSETTER:
				return "setMember";
			default:
				throw new IllegalArgumentException("Invalid operator: " + operator);
		}
	}
}
