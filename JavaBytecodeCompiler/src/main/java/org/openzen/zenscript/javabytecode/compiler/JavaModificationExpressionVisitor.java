package org.openzen.zenscript.javabytecode.compiler;

import org.openzen.zenscript.codemodel.expression.*;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.GetterMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
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
public class JavaModificationExpressionVisitor implements ExpressionVisitor<Void> {
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
			javaWriter.dupX1(true, large);
		}
	}

	@Override
	public Void visitAndAnd(AndAndExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: &&");
	}

	@Override
	public Void visitArray(ArrayExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: array");
	}

	@Override
	public Void visitCompare(CompareExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: comparison");
	}

	@Override
	public Void visitCall(CallExpression expression) {

		// ToDo: Scenario: We have a Getter and Setter on the type, how do we do a postfix call here?
		//         Currently this is reached for every instance field since we always generate an auto getter and setter
		if(expression.method.method instanceof GetterMember) {
			GetterMember getter = (GetterMember) expression.method.method;
			SetterMember setter = getter.getDefinition().members.stream()
					.map(m -> m instanceof FieldMember ? ((FieldMember) m).autoSetter : m)
					.filter(m -> m instanceof SetterMember && getter.name.equals(((SetterMember) m).name))
					.map(m -> (SetterMember) m)
					.findFirst()
					.orElseThrow(() -> new UnsupportedOperationException("No matching member for " + getter.name));

			expression.target.accept(expressionVisitor);
			javaWriter.dup(context.getType(expression.target.type));
			javaWriter.invokeVirtual((JavaNativeMethod) context.getJavaMethod(getter));
			modifyVirtualMethod(setter.type);
			javaWriter.invokeVirtual((JavaNativeMethod) context.getJavaMethod(setter));
			return null;
		}

		throw new UnsupportedOperationException("Invalid lvalue: call");
	}

	@Override
	public Void visitCallStatic(CallStaticExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: static call");
	}

	@Override
	public Void visitCallSuper(CallSuperExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: super call");
	}

	@Override
	public Void visitCapturedClosure(CapturedClosureExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: captured closure");
	}

	@Override
	public Void visitCapturedLocalVariable(CapturedLocalVariableExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: captured local variable cannot be modified");
	}

	@Override
	public Void visitCapturedParameter(CapturedParameterExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: captured parameter cannot be modified");
	}

	@Override
	public Void visitCapturedThis(CapturedThisExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: this");
	}

	@Override
	public Void visitCheckNull(CheckNullExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: null check");
	}

	@Override
	public Void visitCoalesce(CoalesceExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: coalesce operator");
	}

	@Override
	public Void visitConditional(ConditionalExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: conditional expression");
	}

	@Override
	public Void visitConstantBool(ConstantBoolExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant bool");
	}

	@Override
	public Void visitConstantByte(ConstantByteExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant byte");
	}

	@Override
	public Void visitConstantChar(ConstantCharExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant char");
	}

	@Override
	public Void visitConstantDouble(ConstantDoubleExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant double");
	}

	@Override
	public Void visitConstantFloat(ConstantFloatExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant float");
	}

	@Override
	public Void visitConstantInt(ConstantIntExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant int");
	}

	@Override
	public Void visitConstantLong(ConstantLongExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant long");
	}

	@Override
	public Void visitConstantSByte(ConstantSByteExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant sbyte");
	}

	@Override
	public Void visitConstantShort(ConstantShortExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant short");
	}

	@Override
	public Void visitConstantString(ConstantStringExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant string");
	}

	@Override
	public Void visitConstantUInt(ConstantUIntExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant uint");
	}

	@Override
	public Void visitConstantULong(ConstantULongExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant ulong");
	}

	@Override
	public Void visitConstantUShort(ConstantUShortExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant ushort");
	}

	@Override
	public Void visitConstantUSize(ConstantUSizeExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constant usize");
	}

	@Override
	public Void visitConstructorThisCall(ConstructorThisCallExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: constructor forwarding call");
	}

	@Override
	public Void visitConstructorSuperCall(ConstructorSuperCallExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: super forwarding call");
	}

	@Override
	public Void visitEnumConstant(EnumConstantExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: enum constant");
	}

	@Override
	public Void visitFunction(FunctionExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: function");
	}

	@Override
	public Void visitGetField(GetFieldExpression expression) {
		JavaNativeField field = (JavaNativeField) context.getJavaField(expression.field);
		expression.target.accept(expressionVisitor);
		javaWriter.dup();
		javaWriter.getField(field);
		modifyVirtualMethod(expression.field.getType());
		javaWriter.putField(field);
		return null;
	}

	@Override
	public Void visitGetFunctionParameter(GetFunctionParameterExpression expression) {
		JavaParameterInfo parameter = module.getParameterInfo(expression.parameter);
		javaWriter.load(parameter);
		modify(expression.type);
		javaWriter.store(parameter);
		return null;
	}

	@Override
	public Void visitGetLocalVariable(GetLocalVariableExpression expression) {
		JavaLocalVariableInfo variable = javaWriter.getLocalVariable(expression.variable.id);
		javaWriter.load(variable);
		modify(expression.type);
		javaWriter.store(variable);
		return null;
	}

	@Override
	public Void visitGetMatchingVariantField(GetMatchingVariantField expression) {
		throw new UnsupportedOperationException("Invalid lvalue: matching variant field");
	}

	@Override
	public Void visitGetStaticField(GetStaticFieldExpression expression) {
		JavaNativeField field = (JavaNativeField) context.getJavaField(expression.field.field);
		javaWriter.getStaticField(field);
		modify(expression.type);
		javaWriter.putStaticField(field);
		return null;
	}

	@Override
	public Void visitGlobal(GlobalExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: global");
	}

	@Override
	public Void visitGlobalCall(GlobalCallExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: global call");
	}

	@Override
	public Void visitInterfaceCast(InterfaceCastExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: cast");
	}

	@Override
	public Void visitIs(IsExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: is");
	}

	@Override
	public Void visitMakeConst(MakeConstExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: const");
	}

	@Override
	public Void visitMap(MapExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: map");
	}

	@Override
	public Void visitMatch(MatchExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: match");
	}

	@Override
	public Void visitNull(NullExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: null");
	}

	@Override
	public Void visitOrOr(OrOrExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: ||");
	}

	@Override
	public Void visitPanic(PanicExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: panic");
	}

	@Override
	public Void visitPlatformSpecific(Expression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: platform-specific expression");
	}

	@Override
	public Void visitPostCall(PostCallExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: post call");
	}

	@Override
	public Void visitRange(RangeExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: range");
	}

	@Override
	public Void visitSameObject(SameObjectExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: ===");
	}

	@Override
	public Void visitSetField(SetFieldExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: set field");
	}

	@Override
	public Void visitSetFunctionParameter(SetFunctionParameterExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: set function parameter");
	}

	@Override
	public Void visitSetLocalVariable(SetLocalVariableExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: set local variable");
	}

	@Override
	public Void visitSetStaticField(SetStaticFieldExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: set static field");
	}

	@Override
	public Void visitSupertypeCast(SupertypeCastExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: cast");
	}

	@Override
	public Void visitSubtypeCast(SubtypeCastExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: cast");
	}

	@Override
	public Void visitThis(ThisExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: this");
	}

	@Override
	public Void visitThrow(ThrowExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: throw");
	}

	@Override
	public Void visitTryConvert(TryConvertExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: try conversion");
	}

	@Override
	public Void visitTryRethrowAsException(TryRethrowAsExceptionExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: try rethrow");
	}

	@Override
	public Void visitTryRethrowAsResult(TryRethrowAsResultExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: try rethrow");
	}

	@Override
	public Void visitVariantValue(VariantValueExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: variant value");
	}

	@Override
	public Void visitWrapOptional(WrapOptionalExpression expression) {
		throw new UnsupportedOperationException("Invalid lvalue: wrap optional");
	}

	public enum PushOption {
		NONE, // don't push result
		BEFORE, // push result before modification (eg. i++)
		AFTER // push result after modification (eg. ++i)
	}
}
