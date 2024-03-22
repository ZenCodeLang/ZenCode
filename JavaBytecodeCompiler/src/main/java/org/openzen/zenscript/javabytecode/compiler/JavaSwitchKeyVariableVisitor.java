package org.openzen.zenscript.javabytecode.compiler;

import org.objectweb.asm.Label;
import org.openzen.zenscript.codemodel.VariableDefinition;
import org.openzen.zenscript.codemodel.expression.switchvalue.*;
import org.openzen.zenscript.codemodel.statement.VarStatement;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaVariantOption;


public class JavaSwitchKeyVariableVisitor implements SwitchValueVisitor<Void> {


	private final JavaWriter javaWriter;
	private final JavaBytecodeContext context;
	private final Label caseStart;
	private final Label caseEnd;

	public JavaSwitchKeyVariableVisitor(JavaWriter javaWriter, JavaBytecodeContext context, Label caseStart, Label caseEnd) {
		this.javaWriter = javaWriter;
		this.context = context;
		this.caseStart = caseStart;
		this.caseEnd = caseEnd;
	}

	@Override
	public Void acceptInt(IntSwitchValue value) {
		return null;
	}

	@Override
	public Void acceptChar(CharSwitchValue value) {
		return null;
	}

	@Override
	public Void acceptString(StringSwitchValue value) {
		return null;
	}

	@Override
	public Void acceptEnumConstant(EnumConstantSwitchValue value) {
		return null;
	}

	@Override
	public Void acceptVariantOption(VariantOptionSwitchValue key) {
		final JavaVariantOption javaVariantOption = context.getJavaVariantOption(key.option);

		// If our variant does not have components we can skip all of this
		if (key.getBindings().isEmpty()) {
			return null;
		}

		// Cast to the actual class type
		// Then put all components into separate variables
		javaWriter.checkCast(javaVariantOption.variantOptionClass.internalName);

		int fieldNumber = 0;
		for (VariableDefinition binding : key.getBindings()) {
			javaWriter.dup();
			javaWriter.getField(
					javaVariantOption.variantOptionClass.internalName,
					"field" + fieldNumber++,
					context.getDescriptor(binding.type));

			final JavaLocalVariableInfo javaLocalVariableInfo = new JavaLocalVariableInfo(
					context.getType(binding.type),
					javaWriter.local(context.getType(binding.type)),
					caseStart,
					binding.name,
					caseEnd
			);
			javaWriter.setLocalVariable(binding.id, javaLocalVariableInfo);
			javaWriter.store(javaLocalVariableInfo);
		}
		return null;
	}

	@Override
	public Void acceptError(ErrorSwitchValue value) {
		return null;
	}
}
