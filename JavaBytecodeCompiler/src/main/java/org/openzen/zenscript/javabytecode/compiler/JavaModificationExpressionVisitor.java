package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.expression.modifiable.*;
import org.openzen.zenscript.codemodel.identifiers.instances.MethodInstance;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.TypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaNativeField;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.JavaParameterInfo;

/**
 * @author Hoofdgebruiker
 */
public class JavaModificationExpressionVisitor implements ModifiableExpressionVisitor<Void> {
	private final JavaBytecodeContext context;
	private final JavaCompiledModule module;
	private final JavaWriter javaWriter;
	private final JavaExpressionVisitor expressionVisitor;
	private final Runnable modification;
	private final PushOption push;

	public JavaModificationExpressionVisitor(
			JavaBytecodeContext context,
			JavaCompiledModule module,
			JavaWriter javaWriter,
			JavaExpressionVisitor expressionVisitor,
			Runnable modification,
			PushOption push) {
		this.context = context;
		this.module = module;
		this.javaWriter = javaWriter;
		this.expressionVisitor = expressionVisitor;
		this.modification = modification;
		this.push = push;
	}

	private void modify(TypeID type) {
		modify(CompilerUtils.isLarge(type));
	}

	private void modify(boolean large) {
		if (push == PushOption.BEFORE)
			javaWriter.dup(large);
		modification.run();
		if (push == PushOption.AFTER)
			javaWriter.dup(large);
	}

	private void modifyVirtualMethod(TypeID type) {
		modifyVirtualMethod(CompilerUtils.isLarge(type));
	}


	/**
	 * When we execute a prefix/postfix call on the member of some class (e.g. a virtual field),
	 * then {@link #modify(boolean)} makes it harder to get the callee that we need to store the modified value afterward.
	 * Therefore this implementation doesn't dup but dupX1/dupX2 so that the owner is higher on the stack than the result value
	 * <br/>
	 * On Stack:
	 * <ul>
	 *     <li>Before: [top, value_before, owner, ...rest_of_stack]</li>
	 *     <li>After: [top, value_modified, owner, result_value_if_present, ...rest_of_stack]</li>
	 * </ul>
	 *
	 * Therefore, after this call we can invoke virtual calls that consume (value_modified, owner) and return the result value.
	 **/
	private void modifyVirtualMethod(boolean large) {
		if(push == PushOption.BEFORE) {
			javaWriter.dupX1(false, large);
		}
		modification.run();
		if(push == PushOption.AFTER) {
			javaWriter.dupX1(false, large);
		}
	}

	@Override
	public Void visitLocalVariable(ModifiableLocalVariableExpression expression) {
		JavaLocalVariableInfo variable = javaWriter.getLocalVariable(expression.variable.id);
		javaWriter.load(variable);
		modify(expression.getType());
		javaWriter.store(variable);
		return null;
	}

	@Override
	public Void visitField(ModifiableFieldExpression expression) {
		JavaNativeField field = (JavaNativeField) context.getJavaField(expression.field);
		expression.target.accept(expressionVisitor);
		javaWriter.dup();
		javaWriter.getField(field);
		modifyVirtualMethod(expression.field.getType());
		javaWriter.putField(field);
		return null;
	}

	@Override
	public Void visitFunctionParameter(ModifiableFunctionParameterExpression expression) {
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.load(parameter);
		modify(expression.getType());
		javaWriter.store(parameter);
		return null;
	}

	@Override
	public Void visitInvalid(ModifiableInvalidExpression modifiableInvalidExpression) {
		// should be caught by the expression validator
		throw new UnsupportedOperationException("Cannot modify an invalid expression");
	}

	@Override
	public Void visitProperty(ModifiablePropertyExpression expression) {
		MethodInstance getter = expression.getter;
		MethodInstance setter = expression.setter;

		expression.instance.accept(expressionVisitor);
		javaWriter.dup(context.getType(expression.getType()));
		javaWriter.invokeVirtual((JavaNativeMethod) context.getJavaMethod(getter));
		modifyVirtualMethod(expression.getType());
		javaWriter.invokeVirtual((JavaNativeMethod) context.getJavaMethod(setter));
		return null;
	}

	@Override
	public Void visitStaticField(ModifiableStaticFieldExpression expression) {
		JavaNativeField field = (JavaNativeField) context.getJavaField(expression.field.field);
		javaWriter.getStaticField(field);
		modify(expression.getType());
		javaWriter.putStaticField(field);
		return null;
	}

	public enum PushOption {
		NONE, // don't push result
		BEFORE, // push result before modification (eg. i++)
		AFTER // push result after modification (eg. ++i)
	}
}
