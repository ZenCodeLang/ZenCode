package org.openzen.zenscript.javabytecode.compiler.lambda;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaExpressionVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.lambda.capturing.JavaRedirectCapturesCapturedExpressionVisitor;
import org.openzen.zenscript.javabytecode.compiler.lambda.capturing.JavaLoadCapturesOnIndyCapturedExpressionVisitor;
import org.openzen.zenscript.javabytecode.compiler.lambda.capturing.JavaLoadThisOnIndyCapturedExpressionVisitor;
import org.openzen.zenscript.javabytecode.compiler.definitions.JavaMemberVisitor;
import org.openzen.zenscript.javashared.JavaClass;
import org.openzen.zenscript.javashared.JavaCompiledModule;
import org.openzen.zenscript.javashared.JavaNativeMethod;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;

import java.util.StringJoiner;

public final class LambdaIndyCompiler {
	private final JavaWriter writer;
	private final JavaMangler mangler;
	private final JavaBytecodeContext context;
	private final JavaCompiledModule module;
	private final JavaExpressionVisitor visitor;

	private LambdaIndyCompiler(final JavaWriter writer, final JavaMangler mangler, final JavaBytecodeContext context, final JavaCompiledModule module, final JavaExpressionVisitor visitor) {
		this.writer = writer;
		this.mangler = mangler;
		this.context = context;
		this.module = module;
		this.visitor = visitor;
	}

	public static LambdaIndyCompiler of(final JavaWriter writer, final JavaMangler mangler, final JavaBytecodeContext context, final JavaCompiledModule module, final JavaExpressionVisitor visitor) {
		return new LambdaIndyCompiler(writer, mangler, context, module, visitor);
	}

	public void compileFunctionExpressionViaIndy(final FunctionExpression expression, final String interfaceName, final JavaNativeMethod methodInfo) {
		final JavaClass owner = this.writer.method.class_;
		final LambdaClosureInfo closureInfo = LambdaClosureInfo.from(this.context, expression.closure, owner);

		final JavaNativeMethod lambdaMethod = this.createLambdaMethod(owner, interfaceName, closureInfo, expression);
		final JavaCompilingMethod lambdaMethodCompiling = JavaMemberVisitor.compileBridgeableMethodNoSideEffect(lambdaMethod, lambdaMethod.descriptor); // TODO("Restore signatures")

		this.generateIndyMethodCall(owner, lambdaMethodCompiling, expression, methodInfo, closureInfo, interfaceName);
		this.generateLambdaMethod(lambdaMethodCompiling, expression, closureInfo);
	}

	private JavaNativeMethod createLambdaMethod(
			final JavaClass owner,
			final String interfaceName,
			final LambdaClosureInfo closureInfo,
			final FunctionExpression lambdaExpression
	) {
		// TODO("Figure out why IntelliJ complains about NPE")
		final String lambdaName = this.mangler.mangleLambdaMethod(this.writer.method.compiled.name, interfaceName);
		final String lambdaDescriptor = this.computeLambdaDescriptor(closureInfo, lambdaExpression.header);
		return JavaNativeMethod.getStatic(owner, lambdaName, lambdaDescriptor, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
	}

	private String computeLambdaDescriptor(final LambdaClosureInfo closureInfo, final FunctionHeader header) {
		final StringJoiner joiner = new StringJoiner("");
		for (final CapturedExpression capture : closureInfo.closure().captures) {
			if (!(capture instanceof CapturedThisExpression)) {
				final String descriptor = context.getDescriptor(capture.type);
				joiner.add(descriptor);
			}
		}

		// TODO("Remove null check: the method should always return non-null")
		final String thisType = closureInfo.thisType() == null? "Ljava/lang/Void;" : context.getDescriptor(closureInfo.thisType());
		final StringBuilder builder = new StringBuilder(context.getMethodDescriptor(header));
		builder.insert(builder.indexOf("(") + 1, thisType);
		builder.insert(builder.lastIndexOf(")"), joiner);
		return builder.toString();
	}

	private void generateLambdaMethod(final JavaCompilingMethod compilingLambdaMethod, final FunctionExpression lambdaExpression, final LambdaClosureInfo closureInfo) {
		final JavaWriter lambdaWriter = new JavaWriter(this.context.logger, lambdaExpression.position, this.writer.clazzVisitor, compilingLambdaMethod, null);
		final CapturedExpressionVisitor<Void> lambdaCapturesVisitor = new JavaRedirectCapturesCapturedExpressionVisitor(lambdaWriter, lambdaExpression, closureInfo);
		final JavaExpressionVisitor lambdaExpressionVisitor = new JavaExpressionVisitor(this.context, this.module, lambdaWriter, this.mangler, lambdaCapturesVisitor);
		final JavaStatementVisitor lambdaStatementVisitor = new JavaStatementVisitor(this.context, lambdaExpressionVisitor, this.mangler);

		this.generateLambdaMethodBody(compilingLambdaMethod, lambdaWriter, lambdaStatementVisitor, lambdaExpression);
	}

	private void generateLambdaMethodBody(
			final JavaCompilingMethod method,
			final JavaWriter writer,
			final JavaStatementVisitor statementVisitor,
			final FunctionExpression lambdaExpression
	) {
		final Label begin = new Label();
		final Label end = new Label();

		writer.start();
		writer.label(begin);

		lambdaExpression.body.accept(statementVisitor);
		writer.ret();
		writer.label(end);

		this.loadLambdaVariables(writer, method, lambdaExpression, begin, end);

		writer.end();
	}

	private void loadLambdaVariables(
			final JavaWriter writer,
			final JavaCompilingMethod method,
			final FunctionExpression lambdaExpression,
			final Label begin,
			final Label end
	) {
		final Type[] methodArguments = Type.getArgumentTypes(method.compiled.descriptor);
		final FunctionParameter[] lambdaParameters = lambdaExpression.header.parameters;

		for (int i = 0, l = methodArguments.length, p = lambdaParameters.length + 1, localIndex = 0; i < l; ++i) {
			final Type type = methodArguments[i];

			final String name;
			if (i == 0) {
				name = "$this";
			} else if (i < p) {
				final String lambdaName = lambdaParameters[i - 1].name;
				name = lambdaName == null || lambdaName.isEmpty()? "$param" + (i - 1) : lambdaName;
			} else {
				// TODO("Maybe find the actual name of the variable among the captures?")
				name = "$capture$" + (i - p);
			}

			final JavaLocalVariableInfo info = new JavaLocalVariableInfo(type, localIndex, begin, name, end);
			writer.addVariableInfo(info);

			localIndex += type.getSize();
		}
	}

	private void generateIndyMethodCall(
			final JavaClass owner,
			final JavaCompilingMethod method,
			final FunctionExpression lambdaExpression,
			final JavaNativeMethod methodInfo,
			final LambdaClosureInfo closureInfo,
			final String interfaceType
	) {
		final JavaLoadThisOnIndyCapturedExpressionVisitor thisVisitor = new JavaLoadThisOnIndyCapturedExpressionVisitor(this.visitor);
		final JavaLoadCapturesOnIndyCapturedExpressionVisitor othersVisitor = new JavaLoadCapturesOnIndyCapturedExpressionVisitor(this.visitor);

		boolean hasLoadedThis = false;
		for (final CapturedExpression capture : lambdaExpression.closure.captures) {
			// Note: we want this to be |= to ensure NO short-circuiting behavior
			hasLoadedThis |= capture.accept(thisVisitor);
		}

		// If no this was loaded, then we don't care about its value; just load null
		if (!hasLoadedThis) {
			this.writer.aConstNull();
		}

		for (final CapturedExpression capture : lambdaExpression.closure.captures) {
			capture.accept(othersVisitor);
		}

		// TODO("Have this in JavaWriter")
		this.writer.getVisitor().visitInvokeDynamicInsn(
				methodInfo.name,
				this.computeIndyDescriptor(closureInfo, interfaceType),
				new org.objectweb.asm.Handle(
						Opcodes.H_INVOKESTATIC,
						Type.getInternalName(org.openzen.zenscript.javart.factory.LambdaFactory.class),
						"buildLambda",
						Type.getMethodDescriptor(
								Type.getType(java.lang.invoke.CallSite.class),
								Type.getType(java.lang.invoke.MethodHandles.Lookup.class),
								Type.getType(String.class),
								Type.getType(java.lang.invoke.MethodType.class),
								Type.getType(java.lang.invoke.MethodHandle.class),
								Type.getType(java.lang.invoke.MethodType.class),
								Type.INT_TYPE,
								Type.getType(java.lang.invoke.MethodType.class)
						),
						false
				),
				new org.objectweb.asm.Handle(
						Opcodes.H_INVOKESTATIC,
						owner.internalName,
						method.compiled.name,
						method.compiled.descriptor,
						false
				),
				Type.getMethodType(JavaMemberVisitor.compileBridgeableMethodNoSideEffect(methodInfo, context.getMethodDescriptor(lambdaExpression.header)).compiled.descriptor),
				org.openzen.zenscript.javart.factory.LambdaFactory.FLAG_GENERATE_BRIDGE,
				Type.getMethodType(methodInfo.descriptor)
		);
	}

	private String computeIndyDescriptor(final LambdaClosureInfo closureInfo, final String targetInterface) {
		final StringBuilder builder = new StringBuilder("(");

		// TODO("Remove null check as this method should never return null")
		if (closureInfo.thisType() == null) {
			builder.append("Ljava/lang/Void;");
		} else {
			builder.append(context.getDescriptor(closureInfo.thisType()));
		}

		final StringJoiner joiner = new StringJoiner("");
		for (final CapturedExpression capture : closureInfo.closure().captures) {
			if (!(capture instanceof CapturedThisExpression)) {
				final String descriptor = context.getDescriptor(capture.type);
				joiner.add(descriptor);
			}
		}

		builder.append(joiner).append(")L").append(targetInterface).append(';');
		return builder.toString();
	}

}
