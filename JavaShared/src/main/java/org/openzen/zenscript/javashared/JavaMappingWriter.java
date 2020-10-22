/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.definition.AliasDefinition;
import org.openzen.zenscript.codemodel.definition.ClassDefinition;
import org.openzen.zenscript.codemodel.definition.DefinitionVisitor;
import org.openzen.zenscript.codemodel.definition.EnumDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.definition.FunctionDefinition;
import org.openzen.zenscript.codemodel.definition.InterfaceDefinition;
import org.openzen.zenscript.codemodel.definition.StructDefinition;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.DestructorMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinitionMember;
import org.openzen.zenscript.codemodel.member.IteratorMember;
import org.openzen.zenscript.codemodel.member.MemberVisitor;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.member.StaticInitializerMember;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaMappingWriter implements DefinitionVisitor<Void> {
	private final JavaCompiledModule module;
	private final StringBuilder result = new StringBuilder();
	
	public JavaMappingWriter(JavaCompiledModule module) {
		this.module = module;
	}
	
	public String getOutput() {
		return result.toString();
	}

	@Override
	public Void visitClass(ClassDefinition definition) {
		result.append("class:");
		writeDefinitionDescriptor(definition);
		return null;
	}

	@Override
	public Void visitInterface(InterfaceDefinition definition) {
		result.append("interface:");
		writeDefinitionDescriptor(definition);
		return null;
	}

	@Override
	public Void visitEnum(EnumDefinition definition) {
		result.append("enum:");
		writeDefinitionDescriptor(definition);
		return null;
	}

	@Override
	public Void visitStruct(StructDefinition definition) {
		result.append("struct:");
		writeDefinitionDescriptor(definition);
		return null;
	}

	@Override
	public Void visitFunction(FunctionDefinition definition) {
		result.append("function:").append(definition.name);
		writeFunctionDescriptor(definition.header);
		return null;
	}

	@Override
	public Void visitExpansion(ExpansionDefinition definition) {
		result.append("expansion:");
		writeDefinitionDescriptor(definition);
		return null;
	}

	@Override
	public Void visitAlias(AliasDefinition definition) {
		// aliases don't have mappings
		return null;
	}

	@Override
	public Void visitVariant(VariantDefinition variant) {
		result.append("variant:");
		writeDefinitionDescriptor(variant);
		return null;
	}
	
	private void writeFunctionDescriptor(FunctionHeader header) {
		result.append(header.getCanonical());
	}
	
	private void writeDefinitionDescriptor(HighLevelDefinition definition) {
		if (definition.name != null)
			result.append(definition.name);
		if (definition.typeParameters.length > 0) {
			result.append('<');
			for (int i = 0; i < definition.typeParameters.length; i++) {
				if (i > 0)
					result.append(',');
				result.append(definition.typeParameters[i].getCanonical());
			}
			result.append('>');
		}
		
		JavaClass definitionClass = definition instanceof ExpansionDefinition
				? module.getExpansionClassInfo(definition)
				: module.getClassInfo(definition);
		if (definitionClass != null) {
			result.append('@').append(definitionClass.internalName);
		}
		result.append('\n');
		
		MemberMappingWriter memberWriter = new MemberMappingWriter(definitionClass);
		for (IDefinitionMember member : definition.members) {
			member.accept(memberWriter);
		}
	}
	
	private class MemberMappingWriter implements MemberVisitor<Void> {
		private final JavaClass definition;
		
		public MemberMappingWriter(JavaClass definition) {
			this.definition = definition;
		}

		@Override
		public Void visitConst(ConstMember member) {
			JavaField field = module.optFieldInfo(member);
			if (field == null)
				return null;
			
			result.append(":const:");
			result.append(member.name);
			result.append("=");
			result.append(field.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitField(FieldMember member) {
			JavaField field = module.optFieldInfo(member);
			if (field == null)
				return null;
			
			result.append(":field:");
			result.append(member.name);
			result.append("=");
			result.append(field.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitConstructor(ConstructorMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":constructor:");
			result.append(member.header.getCanonicalWithoutReturnType());
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitDestructor(DestructorMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":destructor=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitMethod(MethodMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":method:");
			result.append(member.name);
			result.append(member.header.getCanonical());
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitGetter(GetterMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":getter:");
			result.append(member.name);
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitSetter(SetterMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":setter:");
			result.append(member.name);
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitOperator(OperatorMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":operator:");
			result.append(member.operator.name().toLowerCase());
			result.append(member.header.getCanonical());
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitCaster(CasterMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":caster:");
			result.append(member.toType.toString());
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitCustomIterator(IteratorMember member) {
			return null;
		}

		@Override
		public Void visitCaller(CallerMember member) {
			JavaMethod method = module.optMethodInfo(member);
			if (method == null)
				return null;
			
			result.append(":caller:");
			result.append(member.header.getCanonical());
			result.append("=");
			result.append(method.getMapping(definition));
			result.append('\n');
			return null;
		}

		@Override
		public Void visitImplementation(ImplementationMember member) {
			JavaImplementation implementation = module.getImplementationInfo(member);
			return null;
		}

		@Override
		public Void visitInnerDefinition(InnerDefinitionMember member) {
			return null;
		}

		@Override
		public Void visitStaticInitializer(StaticInitializerMember member) {
			return null;
		}
	}
}
