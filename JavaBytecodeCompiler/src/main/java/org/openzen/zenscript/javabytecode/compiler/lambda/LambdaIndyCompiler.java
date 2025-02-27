package org.openzen.zenscript.javabytecode.compiler.lambda;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.FunctionParameter;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.FunctionExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpression;
import org.openzen.zenscript.codemodel.expression.captured.CapturedExpressionVisitor;
import org.openzen.zenscript.codemodel.expression.captured.CapturedThisExpression;
import org.openzen.zenscript.codemodel.type.FunctionTypeID;
import org.openzen.zenscript.javabytecode.JavaBytecodeContext;
import org.openzen.zenscript.javabytecode.JavaLocalVariableInfo;
import org.openzen.zenscript.javabytecode.JavaMangler;
import org.openzen.zenscript.javabytecode.compiler.JavaExpressionVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaStatementVisitor;
import org.openzen.zenscript.javabytecode.compiler.JavaWriter;
import org.openzen.zenscript.javabytecode.compiler.lambda.capturing.*;
import org.openzen.zenscript.javabytecode.compiler.definitions.JavaMemberVisitor;
import org.openzen.zenscript.javart.factory.LambdaFactory;
import org.openzen.zenscript.javashared.*;
import org.openzen.zenscript.javashared.compiling.JavaCompilingMethod;
import org.openzen.zenscript.javashared.expressions.JavaFunctionInterfaceCastExpression;
import org.openzen.zenscript.javashared.types.JavaFunctionalInterfaceTypeID;

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

		this.generateIndyMethodCall(lambdaMethodCompiling, expression, methodInfo, closureInfo, interfaceName);
		this.generateLambdaMethod(lambdaMethodCompiling, expression, closureInfo);
	}

	public void convertTypeOfFunctionExpressionViaIndy(final JavaFunctionInterfaceCastExpression expression) {
		final FunctionTypeID fromType = (FunctionTypeID) expression.value.type;
		final FunctionTypeID toType = expression.functionType;

		this.generateConversionInstructionsForLambdaType(expression.value, fromType, toType);
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
		return JavaNativeMethod.getStatic(owner, lambdaName, lambdaDescriptor, JavaModifiers.PRIVATE | JavaModifiers.STATIC);
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
		final CapturedExpressionVisitor<Void> lambdaCapturesVisitor = new JavaRedirectCapturesCapturedExpressionVisitor(lambdaWriter, lambdaExpression, closureInfo, this.context);
		final JavaExpressionVisitor lambdaExpressionVisitor = new JavaExpressionVisitor(this.context, this.module, lambdaWriter, this.mangler, lambdaCapturesVisitor);
		final JavaStatementVisitor lambdaStatementVisitor = new JavaStatementVisitor(this.context, lambdaExpressionVisitor, this.mangler);

		this.generateLambdaMethodBody(compilingLambdaMethod, lambdaWriter, lambdaStatementVisitor, lambdaExpression, closureInfo);
	}

	private void generateLambdaMethodBody(
			final JavaCompilingMethod method,
			final JavaWriter writer,
			final JavaStatementVisitor statementVisitor,
			final FunctionExpression lambdaExpression,
			final LambdaClosureInfo closureInfo
	) {
		final Label begin = new Label();
		final Label end = new Label();

		writer.start();
		writer.label(begin);

		lambdaExpression.body.accept(statementVisitor);
		writer.ret();
		writer.label(end);

		this.loadLambdaVariables(writer, method, lambdaExpression, closureInfo, begin, end);

		writer.end();
	}

	private void loadLambdaVariables(
			final JavaWriter writer,
			final JavaCompilingMethod method,
			final FunctionExpression lambdaExpression,
			final LambdaClosureInfo closureInfo,
			final Label begin,
			final Label end
	) {
		final JavaCaptureDataFinderCapturedExpressionVisitor captureDataFinder = new JavaCaptureDataFinderCapturedExpressionVisitor(lambdaExpression, closureInfo, this.context);
		final LambdaCaptureData[] captures = lambdaExpression.closure.captures.stream().map(it -> it.accept(captureDataFinder)).toArray(LambdaCaptureData[]::new);

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
				LambdaCaptureData data = null;
				for (final LambdaCaptureData d : captures) {
					if (d.position() == i) {
						data = d;
						break;
					}
				}

				name = "$capture$" + (data == null || data.name().isEmpty()? (i - p) : data.name());
			}

			final JavaLocalVariableInfo info = new JavaLocalVariableInfo(type, localIndex, begin, name, end);
			writer.addVariableInfo(info);

			localIndex += type.getSize();
		}
	}

	private void generateIndyMethodCall(
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

		this.writer.invokeDynamic(indy -> indy
				.callSite(methodInfo.name, this.computeIndyDescriptor(closureInfo, interfaceType))
				.bootstrapMethod(bsm -> bsm
						.method(JavaClass.fromJavaClass(LambdaFactory.class), "buildLambda")
						.arg(method)
						.arg(Type.getMethodType(JavaMemberVisitor.compileBridgeableMethodNoSideEffect(methodInfo, context.getMethodDescriptor(lambdaExpression.header)).compiled.descriptor))
						.arg(LambdaFactory.FLAG_GENERATE_BRIDGE)
						.arg(Type.getMethodType(methodInfo.descriptor))));
	}

	private Type computeIndyDescriptor(final LambdaClosureInfo closureInfo, final String targetInterface) {
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
		return Type.getType(builder.toString());
	}

	private void generateConversionInstructionsForLambdaType(final Expression body, final FunctionTypeID fromType, final FunctionTypeID toType) {
		// To do the above, we simply need to be able to "extract" this into a lambda form.
		// In other words, if we have to convert (OurThing -> Function), we can invoke LambdaFactory with the following
		// parameters:
		//   call site -> Function(OurThing)
		//   lambda method -> virtual invocation on OurThing (virtual means we'll automatically consume the "this" capture)
		//   interfaceSignature -> the signature of the method as specified by OurThing
		//   flags -> generate bridge
		//   bridgeInterfaceSignature -> the signature of the method as specified by Function
		//final JavaNativeMethod fromNativeDescription = this.findNativeMethodDescriptionOfMethod(fromType);
		final JavaNativeMethod fromNativeDescription = this.context.getFunctionalInterface(fromType);
		final JavaNativeMethod toNativeDescription = this.findNativeMethodDescriptionOfMethod(toType);

		body.accept(this.visitor);
		this.writer.invokeDynamic(indy -> indy
				.callSite(toNativeDescription.name, Type.getMethodType(this.context.getType(toType), this.context.getType(fromType)))
				.bootstrapMethod(bsm -> bsm
						.method(JavaClass.fromJavaClass(LambdaFactory.class), "buildLambda")
						.arg(fromNativeDescription)
						.arg(Type.getMethodType(fromNativeDescription.descriptor))
						.arg(LambdaFactory.FLAG_GENERATE_BRIDGE)
						.arg(Type.getMethodType(toNativeDescription.descriptor))));
	}

	private JavaNativeMethod findNativeMethodDescriptionOfMethod(final FunctionTypeID type) {
		if (type instanceof JavaFunctionalInterfaceTypeID) {
			final JavaFunctionalInterfaceTypeID functionalType = ((JavaFunctionalInterfaceTypeID) type);
			final String descriptor = Type.getMethodDescriptor(functionalType.functionalInterfaceMethod);
			return JavaNativeMethod.getVirtual(functionalType.method.cls, functionalType.method.name, descriptor, JavaModifiers.PUBLIC);
		}

		final JavaSynthesizedFunctionInstance function = this.context.getFunction(type);
		final String descriptor = this.context.getMethodDescriptor(type.header);
		return JavaNativeMethod.getVirtual(function.getCls(), function.getMethod(), descriptor, JavaModifiers.PUBLIC);
	}
}
